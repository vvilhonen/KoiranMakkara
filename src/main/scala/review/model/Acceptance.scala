package review.model

import net.liftweb.mapper._

object Acceptance extends Acceptance with LongKeyedMetaMapper[Acceptance]
class Acceptance extends LongKeyedMapper[Acceptance] with IdPK {
  def getSingleton = Acceptance
  object name extends MappedString(this, 256)
  object patch extends MappedLong(this)
}
