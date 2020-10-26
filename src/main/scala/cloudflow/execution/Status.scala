package cloudflow
package execution

import k8sclient.K8sClient

import scala.concurrent.{ExecutionContext, Future}

final case class StatusExecution(s: commands.Status)(
    implicit client: K8sClient,
    ec: ExecutionContext,
    logger: CliLogger
) extends Execution {
  def run() = {
    logger.trace("Executing command Status")
    s.cloudflowApp.fold(
      Future.successful(
        StatusResult(Left(new Exception("Target application not provided")))
      )
    ) { app =>
      for {
        res <- client.status(app)
      } yield {
        StatusResult(res)
      }
    }
  }
}
