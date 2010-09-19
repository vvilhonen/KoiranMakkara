package review.model

import net.liftweb.mapper._
import net.liftweb.http.js.JE._
import review.util.PatchParser

object Review extends Review with LongKeyedMetaMapper[Review]
class Review extends LongKeyedMapper[Review] with IdPK {
  def getSingleton = Review
  object author extends MappedString(this, 256)
  object subject extends MappedString(this, 4096)
  object timestamp extends MappedDateTime(this)
  object diff extends MappedText(this)
  def acceptors: List[Acceptance] = Acceptance.findAll(By(Acceptance.patch, id))
  def addAcceptor(name: String) {
    val existing = Acceptance.find(By(Acceptance.patch, id), By(Acceptance.name, name))
    if (existing.isEmpty) {
      Acceptance.create.name(name).patch(id).save
    }
  }
  def removeAcceptor(name: String) {
    Acceptance.findAll(By(Acceptance.patch, id), By(Acceptance.name, name)).foreach(_.delete_!)
  }

  override def asJs = JsObj(
    "id" -> id.is, "author" -> author.is, "timestamp" -> timestamp.is.toString,
    "subject" -> subject.is, "diff" -> PatchParser.parse(diff.is),
    "acceptors" -> JsArray(acceptors.map(x => Str(x.name.is)): _*))
}
