package cloudflow

import scala.concurrent.Future

trait Action {
  def run(): Future[Unit]
}