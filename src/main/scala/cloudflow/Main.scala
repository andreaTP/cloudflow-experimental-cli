package cloudflow

import commands._
import caseapp._
import caseapp.core._
import caseapp.core.app._

import scala.concurrent._
import scala.concurrent.duration._

object Main extends CommandAppWithPreCommand[Options, Command] {
  val cliLogger = Promise[CliLogger]
  val k8sConfig = Promise[Option[String]]

  override def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      error(
        Error.Other(
          "No arguments provided, re-run with --help for available options."
        )
      )
    } else {
      super.main(args)
    }
  }
  def beforeCommand(options: Options, remainingArgs: Seq[String]) = {
    val logger = new CliLogger(options.loggingLevel)
    cliLogger.success(logger)
    k8sConfig.success(options.kubeConfig)

    logger.trace(
      s"beforeCommand options: $options remainingArgs: $remainingArgs"
    )

    if (remainingArgs.nonEmpty) {
      logger.warn(
        s"Remaining arguments ${remainingArgs.mkString(", ")}"
      )
    }
  }

  private def warningOnRemainingArgs(
      args: RemainingArgs,
      cliLogger: CliLogger
  ): Future[Unit] = {
    Future.successful({
      if (args.remaining.nonEmpty || args.unparsed.nonEmpty) {
        cliLogger.warn(
          s"Remaining arguments ${args.remaining.mkString(", ")}"
        )
        cliLogger.warn(
          s"Unparsed arguments ${args.unparsed.mkString(", ")}"
        )
      }
    })
  }

  def run(command: Command, args: RemainingArgs): Unit = {
    implicit val ec = ExecutionContext.global
    val res = (for {
      logger <- cliLogger.future
      config <- k8sConfig.future
      cli = new Cli(config)(ec, logger)
      _ <- warningOnRemainingArgs(args, logger)
      _ <- cli.run(command)
    } yield {
      logger.close()

      System.out.flush()
      System.out.close()

      System.err.flush()
      System.err.close()
      System.exit(0)
    }).recoverWith {
      case _ =>
        System.exit(1)
        throw new Exception("unreachable code")
    }
    Await.result(res, 30.seconds)
  }
}
