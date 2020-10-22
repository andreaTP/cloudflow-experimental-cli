package cloudflow
package commands

import caseapp._

sealed trait Command {}

// case class CommonOptions(
//     @HelpMessage("the logging level")
//     @ValueDescription("log-level")
//     logLevel: Option[String] = Some("info")
// )

case class Version(
    // @Recurse
    // common: CommonOptions
) extends Command {}

case class List() extends Command {}

case class Deploy(
    @HelpMessage("the CR file to deploy")
    @ValueDescription("cr")
    cr: String
    // @Recurse
    // common: CommonOptions
) extends Command {}
