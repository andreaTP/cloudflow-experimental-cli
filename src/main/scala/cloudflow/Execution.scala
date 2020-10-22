package cloudflow

import scala.concurrent.Future

trait Execution {
  def run(): Future[Result[_]]
}
