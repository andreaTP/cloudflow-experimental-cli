package cloudflow
package k8sclient

import K8sClient._
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fabric8Models._
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import io.fabric8.kubernetes.client.utils.Serialization
import io.fabric8.kubernetes.client.{Config, DefaultKubernetesClient}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class K8sClientFabric8(val config: Option[String])(
    implicit ec: ExecutionContext,
    logger: CliLogger
) extends K8sClient {

  Serialization.jsonMapper().registerModule(DefaultScalaModule)
//  if needed
  //  Serialization.jsonMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  private val kubeConfig = {
    lazy val fromEnv = sys.env.get("KUBECONFIG").map(Config.fromKubeconfig)
    val fromStr = config.fold(fromEnv) { str =>
      Some(Config.fromKubeconfig(str))
    }

    fromStr.getOrElse(Config.autoConfigure(null))
  }

  // TODO: verify compatibility with OpenShift
  private lazy val client = new DefaultKubernetesClient(kubeConfig)

  private lazy val cloudflowCr = {
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

    val tmp = client
      .customResource(CustomResourceDefinitionContext.fromCrd(crd))
      .list()
      .asScala

    val dummyClient = client.customResources(
      CustomResourceDefinitionContext.fromCrd(crd),
      classOf[Dummy],
      classOf[DummyList],
      classOf[DoneableDummy]
    )

//    val cloudflowClient = client.customResources(
//      CustomResourceDefinitionContext.fromCrd(crd),
//      classOf[CloudflowApplication],
//      classOf[CloudflowApplicationList[CloudflowApplication]],
//      classOf[DoneableCloudflowApplication]
//    )

//    val res = cloudflowClient.list().getItems.asScala.headOption

    val res = dummyClient.list().getItems.asScala.head

    println(res)

//    println(tmp.size)
//    println(tmp.keys.mkString(" - "))
//    println(tmp.values.map(_.getClass).mkString(" - "))
//
//    import com.fasterxml.jackson.databind.ObjectMapper
//    val objectMapper = new ObjectMapper

//    println(objectMapper.writeValueAsString(tmp))
  }

  def list() = {
    logger.trace("Running the Fabric8 list command")
// listOfCRs, err := cloudflowApplicationClient.List()
// 	if err != nil {
// 		printutil.LogAndExit("Failed to get a list of deployed applications, %s", err.Error())
// 	}

// 	w := new(tabwriter.Writer)
// 	w.Init(os.Stdout, 18, 0, 1, ' ', 0)
// 	fmt.Fprintln(w, "NAME\tNAMESPACE\tVERSION\tCREATION-TIME\t")

// 	for _, v := range listOfCRs.Items {
// 		fmt.Fprintf(w, "%s\t%s\t%s\t%s\n", v.Name, v.Namespace, v.Spec.AppVersion, v.ObjectMeta.CreationTimestamp.String())
// 	}

//   c.restClient.
// 		Get().
// 		Resource("cloudflowapplications").
// 		Do().
// 		Into(&result)

    Future.fromTry {
      Try {
        cloudflowCr
//        println(cloudflowCdr.getSpec)
        logger.trace(s"Fabric8 list command successful")
        ""
      }
    }
  }

}
