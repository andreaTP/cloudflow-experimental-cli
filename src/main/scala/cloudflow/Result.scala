package cloudflow

import java.net.URL

import cloudflow.commands.format
import cloudflow.k8sclient.models._
import com.blinkfox.minitable.MiniTable
import play.api.libs.json.{JsPath, Json, Writes}

sealed trait Result[T] {
  val content: T

  def render(fmt: format.Format): String
}

case class VersionResult(version: String) extends Result[String] {
  val content = version
  def render(fmt: format.Format): String = {
    fmt match {
      case format.Classic => version
      case format.Fancy => {
        val versionTable = new MiniTable()
        versionTable.addDatas("VERSION", version)
        versionTable.render()
      }
      case format.Json => {
        Json.stringify(Json.toJsObject(this)(Json.writes[VersionResult]))
      }
    }
  }
}

object ClassicHelper {

  // NOTE: this method comes with no checks on boundaries
  protected[cloudflow] def format(rows: Seq[Seq[String]]): String = {
    val columns =
      for (i <- 0.until(rows.head.size)) yield {
        rows.map(_(i))
      }

    val formattedColumns = columns.map(formatColumn)

    val res = for (i <- 0.until(columns.head.size)) yield {
      formattedColumns.map(_(i))
    }

    res
      .map(_.mkString("", "", ""))
      .mkString("", "\n", "")
  }

  private def formatColumn(elems: Seq[String]) = {
    val maxStringSize = elems.map(_.size).sorted.lastOption.getOrElse(0)

    val padValue =
      if (maxStringSize > 18) maxStringSize + 1
      else 18

    elems.map(_.padTo(padValue, ' '))
  }
}

case class ListResult(summaries: List[CRSummary])
    extends Result[List[CRSummary]] {
  val content = summaries

  def render(fmt: format.Format): String = {
    val headers = Seq("NAME", "NAMESPACE", "VERSION", "CREATION-TIME")
    fmt match {
      case format.Classic => {
        val body: Seq[Seq[String]] = summaries.map { s =>
          Seq(s.name, s.namespace, s.version, s.creationTime)
        }
        ClassicHelper.format(headers +: body)
      }
      case format.Fancy => {
        val table = new MiniTable()
          .addHeaders(headers: _*)

        summaries.foreach { s =>
          table.addDatas(s.name, s.namespace, s.version, s.creationTime)
        }

        table.render()
      }
      case format.Json => {
        implicit val crSummaryWrites = Json.writes[CRSummary]
        implicit val thisWrites = Json.writes[ListResult]
        Json.stringify(Json.toJsObject(this))
      }
    }

  }
}

case class StatusResult(status: ApplicationStatus)
    extends Result[ApplicationStatus] {
  val content: ApplicationStatus = status

  def render(fmt: format.Format): String = {
    val endpointHeaders = Seq("STREAMLET", "ENDPOINT")
    val streamletHeaders =
      Seq("STREAMLET", "POD", "READY", "STATUS", "RESTARTS")

    fmt match {
      case format.Classic => {
        val summary = {
          Seq(
            s"Name:             ${status.summary.name}",
            s"Namespace:        ${status.summary.namespace}",
            s"Version:          ${status.summary.version}",
            s"Created:          ${status.summary.creationTime}",
            s"Status:           ${status.status}"
          ).mkString("", "\n", "")
        }

        val endpointsList = {
          if (status.endpointsStatuses.nonEmpty) {
            val body: Seq[Seq[String]] =
              status.endpointsStatuses.sortBy(_.name).map { endpointStatus =>
                Seq(endpointStatus.name, endpointStatus.url.toString)
              }

            ClassicHelper.format((endpointHeaders +: body))
          } else {
            "\n"
          }
        }

        val streamletList = {
          if (status.streamletsStatuses.nonEmpty) {
            val body: Seq[Seq[String]] = status.streamletsStatuses
              .sortBy(_.name)
              .map { streamletStatus =>
                if (streamletStatus.podsStatuses.isEmpty) {
                  Seq(Seq(streamletStatus.name, "", "0/0", "Missing", "0"))
                } else {
                  streamletStatus.podsStatuses.map { podStatus =>
                    Seq(
                      streamletStatus.name,
                      podStatus.name,
                      s"${podStatus.ready.ready}/${podStatus.ready.total}",
                      podStatus.status,
                      s"${podStatus.restarts}"
                    )
                  }
                }
              }
              .flatten

            ClassicHelper.format(streamletHeaders +: body)
          } else {
            "\n"
          }
        }

        summary + "\n" + endpointsList + "\n" + streamletList
      }
      case format.Fancy => {
        val appTable = new MiniTable()
          .addDatas("Name:", status.summary.name)
          .addDatas("Namespace:", status.summary.namespace)
          .addDatas("Version:", status.summary.version)
          .addDatas("Created:", status.summary.creationTime)
          .addDatas("Status:", status.status)
          .render()

        val endpointsTable = {
          if (status.endpointsStatuses.nonEmpty) {
            val _endpointsTable = new MiniTable()
              .addHeaders(endpointHeaders: _*)

            status.endpointsStatuses.sortBy(_.name).foreach { endpointStatus =>
              _endpointsTable.addDatas(
                endpointStatus.name,
                endpointStatus.url.toString
              )
            }

            _endpointsTable.render()
          } else {
            "\n"
          }
        }

        val streamletsTable = {
          if (status.streamletsStatuses.nonEmpty) {
            val _streamletsTable = new MiniTable()
              .addHeaders(streamletHeaders: _*)

            status.streamletsStatuses.sortBy(_.name).foreach {
              streamletStatus =>
                if (streamletStatus.podsStatuses.isEmpty) {
                  _streamletsTable.addDatas(
                    streamletStatus.name,
                    "",
                    "0/0",
                    "Missing",
                    "0"
                  )
                } else {
                  streamletStatus.podsStatuses.sortBy(_.name).foreach {
                    podStatus =>
                      _streamletsTable.addDatas(
                        streamletStatus.name,
                        podStatus.name,
                        s"${podStatus.ready.ready}/${podStatus.ready.total}",
                        podStatus.status,
                        s"${podStatus.restarts}"
                      )
                  }
                }
            }

            _streamletsTable.render()
          } else {
            "\n"
          }

        }

        appTable + endpointsTable + streamletsTable
      }
      case format.Json =>
        implicit val urlJsonWrites: Writes[URL] =
          ((JsPath \ "url").write[String].contramap { url: URL =>
            url.toString
          })

        implicit val crSummaryWrites = Json.writes[CRSummary]
        implicit val containersReadyWrites = Json.writes[ContainersReady]
        implicit val endpointStatusWrites = Json.writes[EndpointStatus]
        implicit val podStatusWrites = Json.writes[PodStatus]
        implicit val streamletStatusWrites = Json.writes[StreamletStatus]
        implicit val appStatusResult = Json.writes[ApplicationStatus]
        implicit val thisWrites = Json.writes[StatusResult]

        Json.stringify(Json.toJsObject(this))
    }
  }
}
