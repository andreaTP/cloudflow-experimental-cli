package cloudflow
package actions

import k8sclient.K8sClient
import scala.concurrent.ExecutionContext

case class List(client: K8sClient)(
    implicit ec: ExecutionContext
) extends Action {

  def run() = {
    for {
      res <- client.list()
    } yield { ListResult(res) }
  }
}
