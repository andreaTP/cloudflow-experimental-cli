package cloudflow

import scala.util.Try

trait Execution {
  def run(): Try[_ <: Result[_]]
}
