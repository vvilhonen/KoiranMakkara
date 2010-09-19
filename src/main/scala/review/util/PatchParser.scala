package review.util

case class Diff(files: List[FileDiff]) {
  def asXml = files.map(_.asXml)
}
case class FileDiff(path: String, chunks: List[Chunk]) {
  def asXml = <div class="fileDiff"><span class="path">{path}</span>{chunks.map(_.asXml)}</div>
}
case class Chunk(lines: List[Line]) {
  def asXml = lines.map(_.asXml)
}
sealed abstract class Line(data: String) {
  def asXml = <span class={cssClass}>{data}</span>
  def cssClass: String
}
case class ContextLine(data: String) extends Line(data) {
  def cssClass = "context"
}
case class AddLine(data: String) extends Line(data) {
  def cssClass = "addition"
}
case class RemLine(data: String) extends Line(data) {
  def cssClass = "remove"
}

object PatchParser {
  def parseDiff(data: List[String]) = {
    val cleaned = removeCrap(data)
    Diff(fileDiffs(cleaned)) 
  }

  def fileDiffs(data: List[String]) = {
    var fileDiffs:List[FileDiff] = Nil
    var current = data
    while(current.nonEmpty) {
      val (fileDiff, newCurrent) = parseFileDiff(current)
      fileDiffs ++= List(fileDiff)
      current = newCurrent
    }
    fileDiffs
  }

  def parseFileDiff(data: List[String]) = {
    var current = data.dropWhile(!_.startsWith("--- "))
    val filename = current.drop(1).head.drop(4)
    var chunks: List[Chunk] = Nil
    current = current.drop(2)
    while(current.nonEmpty && current.head.startsWith("@@")) {
      current = current.drop(1)
      val lines = current.takeWhile(isDiffLine(_)).map(toDiffLine(_))
      current = current.drop(lines.length)
      chunks ++= List(Chunk(lines))
    }
    (FileDiff(filename, chunks), current)
  }

  def isDiffLine(line: String) = {
    List("+", "-", " ").map(line.startsWith(_)).contains(true)
  }

  def toDiffLine(line: String) = {
    val start = line.take(1)
    val rest = line.drop(1)
    start match {
      case "+" => AddLine(rest)
      case "-" => RemLine(rest)
      case " " => ContextLine(rest)
    }
  }

  def removeCrap(data: List[String]) = {
    data.reverse.dropWhile(!_.startsWith("--")).drop(1).reverse.dropWhile(!_.startsWith("--- "))
  }

  def parse(data: String) = parseDiff(data.split("\n").toList).asXml.mkString
}
