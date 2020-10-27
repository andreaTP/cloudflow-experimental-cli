package cloudflow
package execution

import k8sclient.K8sClient

import scala.util.Try

final case class StatusExecution(s: commands.Status)(
    implicit client: K8sClient,
    logger: CliLogger
) extends Execution {
  def run(): Try[StatusResult] = {
    logger.trace("Executing command Status")
    s.cloudflowApp.fold(
      throw new Exception("Target application not provided")
    ) { app =>
      for {
        res <- client.status(app)
      } yield {
        StatusResult(res)
      }
    }
  }
}
