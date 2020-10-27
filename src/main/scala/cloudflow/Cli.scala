package cloudflow

import cloudflow.Cli.defaultK8sClient
import commands._
import k8sclient._

import scala.util.{Failure, Try}

object Cli {

  def defaultK8sClient(config: Option[String], logger: CliLogger) =
    new K8sClientFabric8(config)(logger)

}

class Cli(
    k8sConfig: Option[String],
    k8sClientFactory: (Option[String], CliLogger) => K8sClient =
      defaultK8sClient(_, _)
)(implicit logger: CliLogger) {
  private implicit lazy val k8sClient = k8sClientFactory(k8sConfig, logger)

  def defaultRender(cmd: Command, res: Result[_]): Unit = {
    renderResult(res, cmd.getOutput())
  }

  def run[T](
      command: Command,
      render: (Command, Result[_]) => T = defaultRender(_, _)
  ): Try[T] = {

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
      render(command, res)
    }).recoverWith {
      case ex =>
        logger.warn("Failure", ex)
        Console.err.println("Error:")
        Console.err.println(ex.getMessage())
        Failure(ex)
    }
  }

  def renderResult(result: Result[_], fmt: format.Format): Unit = {
    logger.trace(s"Action executed successfully, result: $result")
    println(result.render(fmt))
    ()
  }

}
