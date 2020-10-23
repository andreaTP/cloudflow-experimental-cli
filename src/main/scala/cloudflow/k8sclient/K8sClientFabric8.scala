package cloudflow
package k8sclient

import K8sClient._
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fabric8Models._
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import io.fabric8.kubernetes.client.utils.Serialization
import io.fabric8.kubernetes.client.{Config, DefaultKubernetesClient}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.Try

class K8sClientFabric8(val config: Option[String])(
    implicit logger: CliLogger
) extends K8sClient {

  Serialization.jsonMapper().registerModule(DefaultScalaModule)
  // doublecheck if needed
  Serialization
    .jsonMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)

  private val kubeConfig = {
    lazy val fromEnv = sys.env.get("KUBECONFIG").map(Config.fromKubeconfig)
    val fromStr = config.fold(fromEnv) { str =>
      Some(Config.fromKubeconfig(str))
    }

    fromStr.getOrElse(Config.autoConfigure(null))
  }

  // TODO: verify compatibility with OpenShift
  // TODO verify that is intialized at runtime
  private lazy val client = new DefaultKubernetesClient(kubeConfig)

  private lazy val cloudflowApplications = {
    val crd =
      client
        .customResourceDefinitions()
        .list()
        .getItems()
        .asScala
        .filter { crd =>
          val name = crd.getMetadata.getName
          logger.trace(s"Scanning Custom Resources found: ${name}")
          name == CLOUDFLOW_RESOURCE
        }
        .headOption
        .getOrElse(throw new Exception("Cloudflow not found in the cluster"))

    val cloudflowApplicationsClient = client.customResources(
      CustomResourceDefinitionContext.fromCrd(crd),
      classOf[CloudflowApplication],
      classOf[CloudflowApplicationList],
      classOf[DoneableCloudflowApplication]
    )

    cloudflowApplicationsClient.list().getItems.asScala
  }

  def list() = {
    logger.trace("Running the Fabric8 list command")

    Future.fromTry {
      Try {
        val res = cloudflowApplications.map { app =>
          models.CRSummary(
            app.getMetadata.getName,
            app.getMetadata.getNamespace,
            app.spec.appVersion,
            app.getMetadata.getCreationTimestamp
          )
        }.toList
        logger.trace(s"Fabric8 list command successful")
        res
      }
    }
  }

}
