package cloudflow

import java.net.HttpURLConnection

import cloudflow.k8sclient.K8sClientFabric8
import cloudflow.k8sclient.models.CRSummary
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest._
import matchers.should._
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource

import scala.io.Source

class Fabric8K8sClientSpec
    extends AsyncFlatSpec
    with Matchers
    with BeforeAndAfter {

  implicit val testingLogger = new CliLogger("trace")

  val server = new KubernetesServer()

  before { server.before() }
  after { server.after() }

  "The Fabric8K8sClient" should "do something against the server" in {
    // Arrange
    server.expect.get
      .withPath(
        "/apis/apiextensions.k8s.io/v1beta1/customresourcedefinitions"
      )
      .andReturn(
        HttpURLConnection.HTTP_OK,
        Source
          .fromResource("swiss-knife-crd.json")
          .getLines()
          .mkString("\n")
      )
      .once

    server.expect.get
      .withPath(
        "/apis/cloudflow.lightbend.com/v1/namespaces/test/cloudflowapplications"
      )
      .andReturn(
        HttpURLConnection.HTTP_OK,
        Source
          .fromResource("swiss-knife-cr.json")
          .getLines()
          .mkString("\n")
      )
      .once

    // Act
    val cloudflowApps =
      new K8sClientFabric8(None, (_) => server.getClient).list()

    // Assert
    cloudflowApps.map { apps =>
      apps.size shouldBe 1
      apps.head shouldBe (CRSummary(
        "swiss-knife",
        "swiss-knife",
        "2.0.11",
        "2020-10-26T17:26:18Z"
      ))
    }
  }

}
