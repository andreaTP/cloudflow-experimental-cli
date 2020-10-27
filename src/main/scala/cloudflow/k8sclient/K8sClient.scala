package cloudflow
package k8sclient

import scala.util.Try

object K8sClient {

  val CLOUDFLOW_RESOURCE = "cloudflowapplications.cloudflow.lightbend.com"

}

trait K8sClient {

  def list(): Try[List[models.CRSummary]]

  def status(app: String): Try[models.ApplicationStatus]

}
