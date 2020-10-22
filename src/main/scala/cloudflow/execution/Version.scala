package cloudflow
package execution

import buildinfo.BuildInfo
import cloudflow.k8sclient.K8sClient

import scala.concurrent.{ExecutionContext, Future}

final case class VersionExecution(v: commands.Version) extends Execution {
  def run() =
    Future.successful(VersionResult(BuildInfo.version))
}
