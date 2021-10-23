package cloudflow

import java.net.URL

import cloudflow.commands.format
import cloudflow.k8sclient.models._
import play.api.libs.json.{JsPath, Json, Writes}

sealed trait Result[T] {
  val content: T

  def render(fmt: format.Format): String
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

// TODO: rewrite all of this properly ...
object FancyHelper {
  // Initially copy-pasted from: https://github.com/blinkfox/mini-table
  // License: Apache License 2.0

  object RowType extends Enumeration {
    type RowType = Value
    val TITLE, HEADER, DATA = Value
  }

  case class Row(rowType: RowType.Value, datas: java.util.List[String])

  object StrUtils {
    def center(initStr: String, size: Int, padChar: Char) = {
      var str = initStr
      if (str != null && size > 0) {
        val strLen = str.length
        val pads = size - strLen
        if (pads > 0) {
          str = leftPad(str, strLen + pads / 2, padChar)
          str = rightPad(str, size, padChar)
        }
      }
      str
    }
    def leftPad(str: String, size: Int, padChar: Char) = {
      val pads = size - str.length
      if (pads <= 0) str
      else repeat(padChar, pads).concat(str)
    }
    def rightPad(str: String, size: Int, padChar: Char) = {
      val pads = size - str.length
      if (pads <= 0) str
      else str.concat(repeat(padChar, pads))
    }

    private def repeat(ch: Char, repeat: Int) = {
      val buf = new Array[Char](repeat)
      for (i <- repeat - 1 to 0 by -1) {
        buf(i) = ch
      }
      new String(buf)
    }
  }

  final class MiniTable(var title: String = null) {
    import java.util.ArrayList
    import java.util.HashMap
    import java.util.List
    import scala.jdk.CollectionConverters._

    private var lastRowType: RowType.Value = null
    private val join = new StringBuilder
    private val rows: ArrayList[Row] = new ArrayList[Row]()
    private var formats: Seq[Boolean] = null
    private val maxColMap: HashMap[Integer, Integer] =
      new HashMap[Integer, Integer]()

    def addHeaders(headers: List[_]) =
      this.appendRows(RowType.HEADER, headers.toArray)

    def addHeaders(objects: Any*) = this.appendRows(RowType.HEADER, objects)

    def addDatas(datas: List[_]) = this.appendRows(RowType.DATA, datas.toArray)

    def addDatas(objects: Any*) =
      this.appendRows(RowType.DATA, objects)

    def addFormats(formats: Boolean*) = {
      this.formats = formats
      this
    }

    private def appendRows(rowType: RowType.Value, objects: Seq[Any]) = {
      if (objects != null && objects.length > 0) {
        val len = objects.length
        if (this.maxColMap.size > len) throw new IllegalArgumentException()
        val datas = new ArrayList[String]
        for (i <- 0 until len) {
          val o = objects(i)
          val value =
            if (o == null) "null"
            else o.toString
          datas.add(value)

          val maxColSize = this.maxColMap.get(i)
          if (maxColSize == null) {
            this.maxColMap.put(i, value.length)
          } else {
            if (value.length > maxColSize) this.maxColMap.put(i, value.length)
          }
        }
        this.rows.add(Row(rowType, datas))
      }
      this
    }

    private def buildTitle(): Unit = {
      if (this.title != null) {
        var maxTitleSize = 0

        this.maxColMap.values.asScala.foreach { maxColSize =>
          maxTitleSize += maxColSize
        }
        maxTitleSize += 3 * (this.maxColMap.size - 1)
        if (this.title.length > maxTitleSize)
          this.title = this.title.substring(0, maxTitleSize)

        this.join.append("+")
        for (i <- 0 until maxTitleSize + 2) {
          this.join.append("-")
        }
        this.join
          .append("+\n")
          .append("|")
          .append(StrUtils.center(this.title, maxTitleSize + 2, ' '))
          .append("|\n")
        this.lastRowType = RowType.TITLE
      }
    }

    private def buildTable(): Unit = {
      this.buildTitle()
      var headerProcessed = false
      var i = 0
      val len = this.rows.size
      while ({
        i < len
      }) {
        val row = this.rows.get(i)
        row.rowType match {
          case RowType.HEADER =>
            headerProcessed = true
            if (this.lastRowType ne RowType.HEADER)
              this.buildRowBorder(row.datas)
            this.buildRowData(row.datas)
            this.buildRowBorder(row.datas)

          case RowType.DATA =>
            if (!headerProcessed) {
              this.buildRowBorder(row.datas)
              headerProcessed = true
            }
            this.buildRowData(row.datas)
            if (i == len - 1) this.buildRowBorder(row.datas)

          case _ =>
        }

        i += 1
      }
    }

    private def buildRowBorder(datas: List[String]): Unit = {
      this.join.append("+")
      var i = 0
      val len = datas.size
      while ({
        i < len
      }) {
        for (j <- 0 until this.maxColMap.get(i) + 2) {
          this.join.append("-")
        }
        this.join.append("+")

        i += 1
      }
      this.join.append("\n")
    }

    private def buildRowData(datas: List[String]): Unit = {
      this.join.append("|")
      var i = 0
      val len = datas.size
      while ({
        i < len
      }) {
        if (formats != null) {
          if (formats(i))
            this.join
              .append(
                StrUtils
                  .center(datas.get(i), this.maxColMap.get(i) + 2, ' ')
              )
              .append("|")
          else
            this.join
              .append(
                ' ' + StrUtils
                  .rightPad(datas.get(i), this.maxColMap.get(i) + 1, ' ')
              )
              .append("|")

        } else {
          this.join
            .append(
              StrUtils
                .center(datas.get(i), this.maxColMap.get(i) + 2, ' ')
            )
            .append("|")
        }

        i += 1
      }
      this.join.append("\n")
    }

    def render() = {
      this.buildTable()
      this.join.toString
    }
  }

}

case class VersionResult(version: String) extends Result[String] {
  val content = version
  def render(fmt: format.Format): String = {
    fmt match {
      case format.Classic => version
      case format.Fancy => {
        val versionTable = new FancyHelper.MiniTable()
        versionTable.addDatas("VERSION", version)
        versionTable.render()
      }
      case format.Json => {
        Json.stringify(Json.toJsObject(this)(Json.writes[VersionResult]))
      }
    }
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
        val table = new FancyHelper.MiniTable()
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
        val appTable = new FancyHelper.MiniTable()
          .addFormats(false, true)
          .addDatas("Name:", status.summary.name)
          .addDatas("Namespace:", status.summary.namespace)
          .addDatas("Version:", status.summary.version)
          .addDatas("Created:", status.summary.creationTime)
          .addDatas("Status:", status.status)
          .render()

        val endpointsTable = {
          if (status.endpointsStatuses.nonEmpty) {
            val _endpointsTable = new FancyHelper.MiniTable()
              .addHeaders(endpointHeaders: _*)
              .addFormats(false, false)

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
            val _streamletsTable = new FancyHelper.MiniTable()
              .addHeaders(streamletHeaders: _*)
              .addFormats(false, false, true, true, true)

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
