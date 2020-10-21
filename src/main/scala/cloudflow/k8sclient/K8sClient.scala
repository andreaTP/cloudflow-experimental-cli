package cloudflow
package k8sclient

import scala.concurrent.Future

trait K8sClient {

  val config: Option[String]

  def list(): Future[String]

}
