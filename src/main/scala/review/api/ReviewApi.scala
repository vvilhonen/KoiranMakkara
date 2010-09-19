package review.api

import net.liftweb.http._
import net.liftweb.http.js.JE._
import net.liftweb.common._
import review.model.Review
import review.actor.{PatchServer, PatchAccepted}

object ReviewApi {
  def dispatch: LiftRules.DispatchPF = {
    case Req("api" :: "list" :: Nil, _, GetRequest) => () => Full(listDiffs)
    case r@Req("api" :: "approve" :: Nil, _, PostRequest) => () => Full(approve(r))
    case r@Req("api" :: "disapprove" :: Nil, _, PostRequest) => () => Full(disapprove(r))
  }

  def listDiffs = JsonResponse(JsArray(Review.findAll.map(_.asJs): _*))
  val approve = doWithReview((review, who) =>
    review.addAcceptor(who)
  ) _

  val disapprove = doWithReview((review, who) =>
    review.removeAcceptor(who)
  ) _

  def doWithReview(func: (Review, String) => Any)(r: Req) = {
    (r.param("id"), r.param("who")) match {
      case (Full(id), Full(who)) => 
        Review.findByKey(id.toLong) match {
          case Full(review) =>
            func(review, who)
            PatchServer ! PatchAccepted(id, review.acceptors.map(_.name.is))
            PlainTextResponse("ok")
          case _ => PlainTextResponse("can't find review")
        }
      case _ => PlainTextResponse("need id and who")
    }
  }
}

