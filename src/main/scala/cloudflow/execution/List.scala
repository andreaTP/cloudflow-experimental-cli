package cloudflow
package execution

import k8sclient.K8sClient

import scala.util.Try

final case class ListExecution(l: commands.List)(
    implicit client: K8sClient,
    logger: CliLogger
) extends Execution {
  def run(): Try[ListResult] = {
    logger.trace("Executing command List")
    for {
      res <- client.list()
    } yield {
      ListResult(res)
    }
  }
}
