package cloudflow

import buildinfo.BuildInfo
import cloudflow.commands.OutputFormat
import cloudflow.k8sclient.{K8sClient, models}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest._
import matchers.should._

import scala.util.{Success, Try}

class CliWrokflowSpec extends AnyFlatSpec with Matchers {

  val testingCRSummary = models.CRSummary("foo", "bar", "0.0.1", "now")

  implicit val testingLogger = new CliLogger("error")
  val testingK8sClient = new K8sClient {
    def list(): Try[List[models.CRSummary]] =
      Success(List(testingCRSummary))
    def status(app: String) = ???
  }
  val testingRender = (result: Result[_]) => ()

  val outputFormat = OutputFormat(None)

  "The Cli" should "return the current version" in {
    // Arrange
    val cli = new Cli(
      None,
      (config: Option[String], logger: CliLogger) => testingK8sClient
    )

    // Act
    val res =
      cli.run(commands.Version(outputFormat): commands.Command, (_, res) => res)
    // Assert
    res.isSuccess shouldBe true
    res.get shouldBe a[VersionResult]
    res.get match {
      case vr: VersionResult =>
        vr.version shouldBe BuildInfo.version
    }
  }

  it should "list mocked CRs" in {
    // Arrange
    val cli = new Cli(
      None,
      (config: Option[String], logger: CliLogger) => testingK8sClient
    )

    // Act
    val res =
      cli.run(commands.List(outputFormat): commands.Command, (_, res) => res)

    // Assert
    res.isSuccess shouldBe true
    res.get shouldBe a[ListResult]
    res.get match {
      case lr: ListResult =>
        lr.summaries.head shouldBe testingCRSummary
    }
  }

}
