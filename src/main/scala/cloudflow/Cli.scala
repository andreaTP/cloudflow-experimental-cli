package cloudflow

import commands._
import k8sclient._
import scala.concurrent.{ExecutionContext, Future}

class Cli(command: Command, logger: CliLogger, k8sConfig: Option[String])(
    implicit ec: ExecutionContext
) {

  def run(): Future[Unit] = {
    logger.trace(s"Cli run command: $command")

    lazy val k8sClient = new K8sClientFabric8(k8sConfig)

    val action = command match {
      case _: Version => actions.Version
      case _: List    => actions.List(k8sClient, logger)
      case _ =>
        val msg = "No action defined for the command."
        logger.error(msg)
        throw new Exception(msg)
    }

    logger.trace(s"Cli action: $action")

    action.run()
  }

}
