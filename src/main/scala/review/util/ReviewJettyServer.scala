package review.util

import org.mortbay.jetty.Server
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.webapp._
import org.apache.log4j._
import _root_.net.liftweb.http.LiftFilter

object ReviewJettyServer {
  def main(args: Array[String]) {
    initLogging
    val s = new Server;
    val conn = new SelectChannelConnector
    conn.setPort(8080)
    s.addConnector(conn)
    val c = webappContext
    c.setServer(s)
    s.addHandler(c)
    s.start
  }

  def webappContext = {
    val c = new WebAppContext
    c.setContextPath("/")
    c.setWar(webappPath)
    c.setResourceBase(webappPath)
    c
  }

  def initLogging = {
    val logger = Logger.getLogger("org.mortbay.log")
    logger.setLevel(Level.INFO)
    logger.addAppender(new ConsoleAppender(new SimpleLayout))
    logger
  }

  val webappPath = runningFromJar match {
    case true => this.getClass.getResource("/src/main/webapp").toExternalForm
    case false => "src/main/webapp"
  }

  def runningFromJar = this.getClass.getProtectionDomain.
    getCodeSource.getLocation.toExternalForm.matches("jar:file:.*")
}

