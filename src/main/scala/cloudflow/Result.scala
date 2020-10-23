package cloudflow

import cloudflow.k8sclient.models.CRSummary
import k8sclient.models

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
    summaries.mkString("\n")
  }
}
