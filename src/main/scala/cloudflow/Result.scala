package cloudflow

import k8sclient.models

import com.blinkfox.minitable.MiniTable

sealed trait Result[T] {
  val content: Either[Throwable, T]

  def render(): String
}

final case class VersionResult(version: String) extends Result[String] {
  val content = Right(version)
  def render(): String = version
}

//TODO: format the result
//fmt.Fprintln(w, "NAME\tNAMESPACE\tVERSION\tCREATION-TIME\t")
final case class ListResult(summaries: List[models.CRSummary])
    extends Result[List[models.CRSummary]] {
  val content = Right(summaries)

  def render(): String = {
    val table = new MiniTable()
      .addHeaders("NAME", "NAMESPACE", "VERSION", "CREATION-TIME")

    summaries.foreach { s =>
      table.addDatas(s.name, s.namespace, s.version, s.creationTime)
    }

    table.render()
  }
}
