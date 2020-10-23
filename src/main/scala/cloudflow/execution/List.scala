package cloudflow
package execution

import k8sclient.K8sClient

import scala.concurrent.ExecutionContext

final case class ListExecution(l: commands.List)(
    implicit client: K8sClient,
    ec: ExecutionContext,
    logger: CliLogger
) extends Execution {
  def run() = {
    logger.trace("Executing command List")
    for {
      res <- client.list()
    } yield {
      ListResult(res)
    }
  }
}
