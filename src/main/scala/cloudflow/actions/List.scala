package cloudflow
package actions

import k8sclient.K8sClient
import scala.concurrent.ExecutionContext

case class List(client: K8sClient, logger: CliLogger)(
    implicit ec: ExecutionContext
) extends Action {

  def run() = {
    for {
      _ <- client.list()
    } yield { () }
  }
}
