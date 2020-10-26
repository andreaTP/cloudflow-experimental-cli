package cloudflow

import buildinfo.BuildInfo
import cloudflow.k8sclient.{K8sClient, models}
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest._
import matchers.should._

import scala.concurrent.Future

class CliWrokflowSpec extends AsyncFlatSpec with Matchers {

  val testingCRSummary = models.CRSummary("foo", "bar", "0.0.1", "now")

  implicit val testingLogger = new CliLogger("error")
  val testingK8sClient = new K8sClient {
    def list(): Future[List[models.CRSummary]] =
      Future.successful(List(testingCRSummary))
  }
  val testingRender = (result: Result[_]) => ()

  "The Cli" should "return the current version" in {
    // Arrange
    val cli = new Cli(
      None,
      (config: Option[String], logger: CliLogger) => testingK8sClient
    )

    // Act
    val res = cli.run(commands.Version(): commands.Command, identity)

    // Assert
    res.map { r =>
      r shouldBe a[VersionResult]
      r match {
        case vr: VersionResult =>
          vr.version shouldBe BuildInfo.version
      }
    }
  }

  it should "list mocked CRs" in {
    // Arrange
    val cli = new Cli(
      None,
      (config: Option[String], logger: CliLogger) => testingK8sClient
    )

    // Act
    val res = cli.run(commands.List(): commands.Command, identity)

    // Assert
    res.map { r =>
      r shouldBe a[ListResult]
      r match {
        case lr: ListResult =>
          lr.summaries.head shouldBe testingCRSummary
      }
    }
  }

}
