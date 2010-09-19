package review.actor

import net.liftweb.actor.LiftActor
import net.liftweb.common.SimpleActor
import net.liftweb.http.{AddAListener, RemoveAListener}
import review.model.Review

case class PatchAccepted(id: String, acceptors: List[String])
case class NewPatch(r: Review)
object PatchServer extends LiftActor {
  var listeners: List[SimpleActor[Any]] = Nil
  override def messageHandler = {
    case AddAListener(who, _) => listeners ::= who
    case RemoveAListener(who) => listeners = listeners.filterNot(_ == who)
    case p@PatchAccepted(id, acceptors) => listeners.foreach(_ ! p)
    case n@NewPatch(_) => listeners.foreach(_ ! n)
  }
}
