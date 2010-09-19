package sbt

import java.io.File

object ProguardProject
{
	val ProguardDescription = "Produces the final compacted jar that contains only the minimum classes needed using proguard."
	val WriteProguardDescription = "Creates the configuration file to use with proguard."
}
import ProguardProject._
trait ProguardProject extends BasicScalaProject
{
	def rawJarPath: Path
	def rawPackage: Task

	def proguardConfigurationPath: Path = outputPath / "proguard.pro"
	def outputJar: Path
	def rootProjectDirectory = rootProject.info.projectPath

	val toolsConfig = config("tools")
	val proguardJar = "net.sf.proguard" % "proguard" % "4.3" % "tools->default"

  lazy val aproguard = aproguardAction
  def aproguardAction = proguardAddResourcesTask dependsOn(proguard)
	lazy val proguard = proguardAction
	def proguardAction = proguardTask dependsOn(writeProguardConfiguration) describedAs(ProguardDescription)
	lazy val writeProguardConfiguration = writeProguardConfigurationAction
	def writeProguardConfigurationAction = writeProguardConfigurationTask dependsOn rawPackage describedAs WriteProguardDescription

	def basicOptions: Seq[String] =
		Seq(
				"-dontoptimize",
				"-dontobfuscate",
				"-dontnote",
				"-dontwarn",
				 "-ignorewarnings")
	def keepClasses: Seq[String] = Nil

	def mapInJars(inJars: Seq[File]): Seq[String] = inJars.map(f => "-injars " + mkpath(f))
	def mapLibraryJars(libraryJars: Seq[File]): Seq[String] = libraryJars.map(f => "-libraryjars " + mkpath(f))
	def mapOutJar(outJar: File) = "-outjars " + mkpath(outJar)

	def template(inJars: Seq[File], libraryJars: Seq[File], outJar: File, options: Seq[String], mainClass: Option[String], keepClasses: Seq[String]) =
	{
		val keepMain =
			"""-keep public class %s {
				|    public static void main(java.lang.String[]);
				|}"""

		val lines =
			options ++
			keepClasses.map("-keep public class " + _  + " {\n public * ;\n}") ++
			Seq("-injars " + mkpath(rawJarPath.asFile)) ++
			mapInJars(inJars) ++
			mapLibraryJars(libraryJars) ++
		  Seq(mapOutJar(outJar)) ++
			mainClass.map(main => keepMain.stripMargin.format(main)).toList
		lines.mkString("\n")
	}

	def mkpath(f: File) = '\"' + f.getAbsolutePath + '\"'

  private def proguardAddResourcesTask = task {
    val status = Process("jar", List("uvf", outputJar.toString, "src/main/webapp")) ! log
    if (status == 0) None else Some("Got status "+status)
  }
  
	private def proguardTask =
		task
		{
			FileUtilities.clean(outputJar :: Nil, log)
			val proguardClasspathString = Path.makeString(managedClasspath(toolsConfig).get)
			val configFile = proguardConfigurationPath.asFile.getAbsolutePath
			val exitValue = Process("java", List("-Xmx1024M", "-cp", proguardClasspathString, "proguard.ProGuard", "@" + configFile)) ! log
			if(exitValue == 0) None else Some("Proguard failed with nonzero exit code (" + exitValue + ")")
		}
	private def writeProguardConfigurationTask =
		task
		{
			val dependencies = mainDependencies.snapshot
			log.debug("proguard configuration, all dependencies:\n\t" + dependencies.all.mkString("\n\t"))
			val externalJars = dependencies.external// mainDependencies.map(_.getAbsoluteFile).filter(_.getName.endsWith(".jar"))
			log.debug("proguard configuration external dependencies: \n\t" + externalJars.mkString("\n\t"))
			// partition jars from the external jar dependencies of this project by whether they are located in the project directory
			// if they are, they are specified with -injars, otherwise they are specified with -libraryjars
			val libraryJars = dependenciesPaths ++ dependencies.scalaJars//toList.partition(jar => Path.relativize(rootProjectDirectory, jar).isDefined)
			log.debug("proguard configuration library jars locations:\n\t" + libraryJars.mkString("\n\t"))

			val proguardConfiguration = template(libraryJars, externalJars, outputJar.asFile, basicOptions, getMainClass(false), keepClasses)
			log.debug("Proguard configuration written to " + proguardConfigurationPath)
			FileUtilities.write(proguardConfigurationPath.asFile, proguardConfiguration, log)
		}

  private def dependenciesPaths = compileClasspath.filter(!_.relativePath.startsWith("target")).getFiles.toSeq
}
