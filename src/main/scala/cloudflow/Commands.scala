package cloudflow
package commands

import caseapp._
import cloudflow.commands.format.Format

object format extends Enumeration {
  type Format = Value
  val Classic, Fancy, Json = Value
  val Default: Value = Fancy
}

sealed trait Command {
  def getOutput(): format.Format
}

sealed trait WithDefault {
  def isDefined(): Boolean
  def withDefault(value: String): Command
}

case class OutputFormat(
    @HelpMessage("the output format")
    @ValueDescription("output-format")
    @ExtraName("o")
    output: Option[String]
) {
  def getFormat() = {
    output.fold(format.Default) { fmt =>
      fmt match {
        case "c" | "classic" => format.Classic
        case "f" | "fancy"   => format.Fancy
        case "json"          => format.Json
      }
    }
  }
}

case class Version(
    @Recurse
    output: OutputFormat
) extends Command {
  def getOutput(): Format = output.getFormat()
}

case class List(
    @Recurse
    output: OutputFormat
) extends Command {
  def getOutput(): Format = output.getFormat()
}

case class Status(
    @HelpMessage("the Cloudflow Application")
    cloudflowApp: Option[String],
    @Recurse
    output: OutputFormat
) extends Command
    with WithDefault {
  def getOutput(): Format = output.getFormat()
  def isDefined(): Boolean = cloudflowApp.isDefined
  def withDefault(value: String) =
    copy(cloudflowApp = Some(value))
}

case class Deploy(
    @HelpMessage("the CR file to deploy")
    @ValueDescription("cr")
    cr: Option[String],
    @Recurse
    output: OutputFormat
) extends Command
    with WithDefault {
  def getOutput(): Format = output.getFormat()
  def isDefined(): Boolean = cr.isDefined
  def withDefault(value: String) =
    copy(cr = Some(value))
}
