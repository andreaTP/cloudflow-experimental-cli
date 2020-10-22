package cloudflow

sealed trait Result[T] {
  val content: Either[Throwable, T]

  def render(): String
}

final case class VersionResult(version: String) extends Result[String] {
  val content = Right(version)
  def render(): String = version
}

final case class ListResult(something: String) extends Result[String] {
  val content = Right(something)
  def render(): String = something
}
