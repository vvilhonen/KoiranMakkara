package review.comet

import net.liftweb.http.{CometActor, CometListener}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import review.actor.{PatchServer, PatchAccepted, NewPatch}
import review.model.Review

class PatchComet extends CometActor with CometListener {
  def render = Noop
  override def lowPriority = {
    case PatchAccepted(id, acceptors) =>
      partialUpdate(Call("Review.updateAcceptors", id, JsArray(acceptors.map(a => Str(a)): _*)))
    case NewPatch(review) =>
      partialUpdate(Call("Review.addPatch", review.asJs))
  }
  override def registerWith = PatchServer
}

