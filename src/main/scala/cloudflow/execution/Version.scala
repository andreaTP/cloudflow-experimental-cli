package cloudflow
package execution

import buildinfo.BuildInfo

import scala.util.{Success, Try}

final case class VersionExecution(v: commands.Version) extends Execution {
  def run(): Try[VersionResult] = {
    Success(VersionResult(BuildInfo.version))
  }
}
