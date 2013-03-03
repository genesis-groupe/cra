import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "cra"
  val appVersion = "1.3-SNAPSHOT"

  val appDependencies = Seq(
    // Security
    "be.objectify" %% "deadbolt-2" % "1.1.2",
    // Mail
    "com.typesafe" %% "play-plugins-mailer" % "2.0.4",
    // PDF
	"com.itextpdf" % "itextpdf" % "5.3.4",
    //"pdf" % "pdf_2.9.1" % "0.3",
    //"org.xhtmlrenderer" % "core-renderer" % "R8",
    //"net.sf.jtidy" % "jtidy" % "r938",
    // Morphia & MongoDB
    "leodagdag" %% "play2-morphia-plugin" % "0.0.13",
    // Validator
    "commons-validator" % "commons-validator" % "1.4.0"
    // JasperReport
    //("net.sf.jasperreports" % "jasperreports" % "5.0.0" notTransitive())
    //  .exclude("com.lowagie","itext"),
    //"com.lowagie" % "itext" % "4.2.0"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA)
  .settings(
    // http://www.lunatech-research.fr/archives/2012/04/16/jpa-queries-playframework-20
    javacOptions ++= Seq("-Xlint:unchecked")
  )
  .settings(
    routesImport += "helpers.binder.ObjectIdW"
  )
  .settings(
    ebeanEnabled := false
  )
  .settings(
    lessEntryPoints <<= baseDirectory(customLessEntryPoints)
  )
  .settings(
    // Security
    resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
    // Morphia & MongoDB
    resolvers += Resolver.url("LeoDagDag repository", url("http://leodagdag.github.com/repository/"))(Resolver.ivyStylePatterns),
    //resolvers += Resolver.file("Dropbox Repository", Path.userHome / "Dropbox" / "Public" / "repository" asFile)(Resolver.ivyStylePatterns),
    //resolvers += "Morphia repository" at "http://morphia.googlecode.com/svn/mavenrepo/",
    // PDF
    //resolvers += Resolver.url("Play 2.0 PDF module", url("http://www.joergviola.de/releases/"))(Resolver.ivyStylePatterns),
    checksums := Nil // To prevent proxyToys downloding fails https://github.com/leodagdag/play2-morphia-plugin/issues/11
  )

  // Only compile the bootstrap bootstrap.less file and any other *.less file in the stylesheets directory
  def customLessEntryPoints(base: File): PathFinder = (
    (base / "app"  / "assets" / "stylesheets" / "bootstrap-2.1.1" ** "bootstrap.less") +++
    (base / "app" / "assets" / "stylesheets" / "bootstrap-2.1.1" ** "responsive.less") +++
    (base / "app" / "assets" / "stylesheets" / "vendors" ** "bootstrap.dataTables-1.9.3.less") +++
    (base / "app" / "assets" / "stylesheets" / "vendors" ** "datepicker-20120724.less") +++
    (base / "app" / "assets" / "stylesheets" ** "main.less")
  )

}
