package cloudflow

import scala.concurrent.Future

trait Execution {
  def run(): Future[_ <: Result[_]]
}
