package cloudflow
package k8sclient

import K8sClient._
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fabric8Models._
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import io.fabric8.kubernetes.client.utils.Serialization
import io.fabric8.kubernetes.client.{
  Config,
  DefaultKubernetesClient,
  KubernetesClient
}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.{Failure, Try}

class K8sClientFabric8(
    val config: Option[String],
    clientFactory: Config => KubernetesClient = new DefaultKubernetesClient(_)
)(
    implicit val logger: CliLogger
) extends K8sClient {

  Serialization.jsonMapper().registerModule(DefaultScalaModule)
  // doublecheck if needed
  Serialization
    .jsonMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)

  private lazy val cloudflowApplicationsClient = Try {
    // TODO: verify that the configuration is intialized at runtime
    val kubeConfig = {
      lazy val fromEnv = sys.env.get("KUBECONFIG").map(Config.fromKubeconfig)
      val fromStr = config.fold(fromEnv) { str =>
        Some(Config.fromKubeconfig(str))
      }

      fromStr.getOrElse(Config.autoConfigure(null))
    }

    // TODO: verify compatibility with OpenShift
    val client = clientFactory(kubeConfig)

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

    val cloudflowClient = client.customResources(
      CustomResourceDefinitionContext.fromCrd(crd),
      classOf[CloudflowApplication],
      classOf[CloudflowApplicationList],
      classOf[DoneableCloudflowApplication]
    )

    cloudflowClient
  }.recoverWith {
    case ex =>
      Failure(
        new Exception("Cannot find cloudflow, is the operator installed?", ex)
      )
  }

  def list() = {
    logger.trace("Running the Fabric8 list command")

    Future.fromTry {
      for {
        cloudflowApps <- cloudflowApplicationsClient
        res <- Try {
          val res = cloudflowApps
            .list()
            .getItems
            .asScala
            .map { app =>
              models.CRSummary(
                app.getMetadata.getName,
                app.getMetadata.getNamespace,
                app.spec.appVersion,
                app.getMetadata.getCreationTimestamp
              )
            }
            .toList
          logger.trace(s"Fabric8 list command successful")
          res
        }
      } yield {
        res
      }
    }
  }

  def status(appName: String): Future[String] = {
    Future.fromTry {
      for {
        cloudflowApps <- cloudflowApplicationsClient
        res <- Try {
          val app = cloudflowApps
            .list()
            .getItems()
            .asScala
            .find(_.getMetadata.getName == appName)
            .getOrElse(
              throw new Exception(
                s"Cloudflow application: ${appName} not found"
              )
            )
          println(app)
          // GO on from here
          //          models.ApplicationStatus()
          //          printAppStatus(applicationCR, applicationCR.Status.AppStatus)
          //          printEndpointStatuses(applicationCR)
          //          printStreamletStatuses(applicationCR)

          val res = "temp"
          logger.trace(s"Fabric8 status command successful")
          res
        }
      } yield {
        res
      }
    }
//    Future.successful("something")
  }

}
