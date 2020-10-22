package cloudflow

import commands._
import k8sclient._
import scala.concurrent.{ExecutionContext, Future}

class Cli(logger: CliLogger, k8sConfig: Option[String])(
    implicit ec: ExecutionContext
) {
  private implicit val log = logger

  private implicit lazy val k8sClient = new K8sClientFabric8(k8sConfig)

  def run(command: Command): Future[Unit] = {

    logger.trace(s"Cli run command: $command")

    val exec = command match {
      case cmd: Version => execution.VersionExecution(cmd)
      case cmd: List    => execution.ListExecution(cmd)
      case _ =>
        val msg = "No action defined for the command."
        logger.error(msg)
        throw new Exception(msg)
    }

    logger.trace(s"Cli going to execute: $exec")

    (for {
      res <- exec.run()
    } yield {
      logger.trace(s"Action executed successfully, result: $res")
      res.content match {
        case Right(_) => println(res.render())
        case Left(error) =>
          Console.err.println("Error:")
          error.printStackTrace
      }
      ()
    }).recoverWith {
      case ex =>
        logger.error("Unexpected failure", ex)
        Future.failed(ex)
    }
  }

}
