package cloudflow
package k8sclient

import scala.concurrent.Future

object K8sClient {

  val CLOUDFLOW_RESOURCE = "cloudflowapplications.cloudflow.lightbend.com"

}

trait K8sClient {

  def list(): Future[List[models.CRSummary]]

}
