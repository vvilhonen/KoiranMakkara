package review.util

import scala.io.Source

class PatchParserDev
object PatchParserDev {
  def main(args: Array[String]) {
    for(path <- args) {
      println(PatchParser.parse(Source.fromFile(path).mkString))
    }
  }
}