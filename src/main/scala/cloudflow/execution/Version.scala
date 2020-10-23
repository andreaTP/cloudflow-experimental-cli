package cloudflow
package execution

import buildinfo.BuildInfo

import scala.concurrent.Future

final case class VersionExecution(v: commands.Version) extends Execution {
  def run() =
    Future.successful(VersionResult(BuildInfo.version))
}
