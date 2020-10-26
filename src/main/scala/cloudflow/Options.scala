package cloudflow

import buildinfo.BuildInfo
import caseapp._

@AppName("Kubectl Cloudflow CLI")
@AppVersion(BuildInfo.version)
@ProgName(BuildInfo.name)
case class Options(
    @HelpMessage("the logging level")
    @ValueDescription("log-level")
    logLevel: Option[String] = None,
    @HelpMessage("the kubernetes configuration file")
    @ValueDescription("kube-config")
    kubeConfig: Option[String] = None
) {
  def loggingLevel = logLevel.getOrElse("error")
}
