import sbt._

class MyWebProject(info: ProjectInfo) extends DefaultWebProject(info) with ProguardProject with IdeaProject
{
  val scalatoolsSnapshot = 
    "Scala Tools Snapshot" at "http://scala-tools.org/repo-snapshots/"
  val liftVersion = "2.1-SNAPSHOT"
  val liftWebkit = "net.liftweb" %% "lift-webkit" % liftVersion % "compile"
  val liftMapper = "net.liftweb" %% "lift-mapper" % liftVersion % "compile"
  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.22" % "compile;test"
  val h2 = "com.h2database" % "h2" % "1.2.134" % "runtime"
  val specs = "org.scala-tools.testing" % "specs" % "1.6.1" % "test"
  val javaxMail = "javax.mail" % "mail" % "1.4.1"
  val apacheCommons = "commons-io" % "commons-io" % "1.4"

  // required because Ivy doesn't pull repositories from poms
  val smackRepo = "m2-repository-smack" at "http://maven.reucon.com/public"

  override def mainClass = Some("review.util.ReviewJettyServer")
  override protected def packageAction = packageTask(packagePaths, jarPath, packageOptions).dependsOn(compile)

  override def rawJarPath = jarPath
  override def outputJar = outputPath / ("koiranmakkara-" + version + ".jar")
  override def rawPackage = `package`
  override def keepClasses = List(
    "bootstrap.**", 
    "review.**", 
    "javax.servlet.Filter", 
    "org.h2.**", 
    "org.mortbay.**",
    "net.liftweb.http.LiftFilter",
    "com.sun.mail.imap.IMAPSSLStore",
    "org.apache.log4j.ConsoleAppender",
    "org.apache.log4j.SimpleLayout")
}
