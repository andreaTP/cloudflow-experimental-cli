package cloudflow
package k8sclient

import io.fabric8.kubernetes.client.{Config, DefaultKubernetesClient}

import scala.collection.JavaConverters._
import scala.concurrent.Future

class K8sClientFabric8(val config: Option[String]) extends K8sClient {

  private val kubeConfig = {
    lazy val fromEnv = sys.env.get("KUBECONFIG").map(Config.fromKubeconfig)
    val fromStr = config.fold(fromEnv) { str =>
      Some(Config.fromKubeconfig(str))
    }

    fromStr.getOrElse(Config.autoConfigure(null))
  }

  // TODO: verify compatibility with OpenShift
  private lazy val client = new DefaultKubernetesClient(kubeConfig)

  def list() = {
    val namespaces = client
      .namespaces()
      .list()
      .getItems()
      .asScala
      .map(_.getMetadata().getName())
      .mkString("\n")
    Future.successful(namespaces)
  }

}
