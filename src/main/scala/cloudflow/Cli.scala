package cloudflow

import cloudflow.Cli.defaultK8sClient
import commands._
import k8sclient._

import scala.concurrent.{ExecutionContext, Future}

object Cli {

  def defaultK8sClient(config: Option[String], logger: CliLogger) =
    new K8sClientFabric8(config)(logger)
}

class Cli(
    k8sConfig: Option[String],
    k8sClientFactory: (Option[String], CliLogger) => K8sClient =
      defaultK8sClient(_, _)
)(
    implicit ec: ExecutionContext,
    logger: CliLogger
) {
  private implicit lazy val k8sClient = k8sClientFactory(k8sConfig, logger)

  def run[T](
      command: Command,
      render: Result[_] => T = renderResult(_)
  ): Future[T] = {

    logger.trace(s"Cli run command: $command")

    val exec = command match {
      case cmd: Version => execution.VersionExecution(cmd)
      case cmd: List    => execution.ListExecution(cmd)
      case cmd: Status  => execution.StatusExecution(cmd)
      case _ =>
        val msg = "No execution defined for the command."
        logger.error(msg)
        throw new Exception(msg)
    }

    logger.trace(s"Cli going to execute: $exec")

    (for {
      res <- exec.run()
    } yield {
      render(res)
    }).recoverWith {
      case ex =>
        logger.warn("Failure", ex)
        Console.err.println("Error:")
        Console.err.println(ex.getMessage())
        Future.failed(ex)
    }
  }

  def renderResult(result: Result[_]): Unit = {
    logger.trace(s"Action executed successfully, result: $result")
    println(result.render())
    ()
  }

}
