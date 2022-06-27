//> using scala "2.13"
//> using lib "io.github.alexarchambault.mill::mill-native-image-upload:0.1.21"
//> using lib "com.lihaoyi::os-lib:0.8.1"

object Upload {
  private def create(scalaVersion: String, sparkVersion: String, sourceUrl: String, dest: os.Path): Unit =
    os.proc("scala-cli", "run", "src", "--", "--force", "--dest", dest, "--archive", "--scala", scalaVersion, "--spark", sparkVersion, sourceUrl)
      .call(stdin = os.Inherit, stdout = os.Inherit)
  private def versions = Seq(
    "2.12.15" -> "3.0.3",
    "2.12.15" -> "2.4.2"
  )
  def main(args: Array[String]): Unit = {
    val tag = os.proc("git", "tag", "--points-at", "HEAD").call().out.trim()
    val dummy = tag.isEmpty
    if (dummy)
      System.err.println("Not on a git tag, running in dummy mode")
    val token = Option(System.getenv("UPLOAD_GH_TOKEN")).getOrElse {
      if (dummy) ""
      else sys.error("UPLOAD_GH_TOKEN not set")
    }
    val files = versions.map {
      case (scalaVer, sparkVer) =>
        val url = s"https://archive.apache.org/dist/spark/spark-$sparkVer/spark-$sparkVer-bin-hadoop2.7.tgz"
        val sbv = scalaVer.split('.').take(2).mkString(".")
        val name = s"spark-$sparkVer-bin-hadoop2.7-scala$sbv"
        val dest = os.temp(prefix = name, suffix = ".tgz")
        create(scalaVer, sparkVer, url, dest)
        dest -> s"$name.tgz"
    }
    if (!dummy)
      io.github.alexarchambault.millnativeimage.upload.Upload.upload(
        ghOrg = "scala-cli",
        ghProj = "lightweight-spark-distrib",
        ghToken = token,
        tag = tag,
        dryRun = false,
        overwrite = true
      )(files: _*)
  }
}
