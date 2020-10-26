package cloudflow
package commands

import caseapp._

sealed trait Command {}

sealed trait WithDefault {
  def isDefined(): Boolean
  def withDefault(value: String): Command
}

case class Version() extends Command {}

case class List() extends Command {}

case class Status(
    @HelpMessage("the Cloudflow Application")
    cloudflowApp: Option[String]
) extends Command
    with WithDefault {
  def isDefined(): Boolean = cloudflowApp.isDefined
  def withDefault(value: String) =
    copy(cloudflowApp = Some(value))
}

case class Deploy(
    @HelpMessage("the CR file to deploy")
    @ValueDescription("cr")
    cr: Option[String]
) extends Command
    with WithDefault {
  def isDefined(): Boolean = cr.isDefined
  def withDefault(value: String) =
    copy(cr = Some(value))
}
