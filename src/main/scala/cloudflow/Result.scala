package cloudflow

import k8sclient.models

import com.blinkfox.minitable.MiniTable

sealed trait Result[T] {
  val content: T

  def render(): String
}

case class VersionResult(version: String) extends Result[String] {
  val content = version
  def render(): String = version
}

case class ListResult(summaries: List[models.CRSummary])
    extends Result[List[models.CRSummary]] {
  val content = summaries

  def render(): String = {
    val table = new MiniTable()
      .addHeaders("NAME", "NAMESPACE", "VERSION", "CREATION-TIME")

    summaries.foreach { s =>
      table.addDatas(s.name, s.namespace, s.version, s.creationTime)
    }

    table.render()
  }
}

case class StatusResult(status: models.ApplicationStatus)
    extends Result[models.ApplicationStatus] {
  val content: models.ApplicationStatus = status

  def render(): String = {
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
          .addHeaders("STREAMLET", "ENDPOINT")

        status.endpointsStatuses.foreach { endpointStatus =>
          _endpointsTable.addDatas(
            endpointStatus.name,
            endpointStatus.url.toString
          )
        }

        _endpointsTable.render()
      } else {
        ""
      }
    }

    val streamletsTable = {
      if (status.streamletsStatuses.nonEmpty) {
        val _streamletsTable = new MiniTable()
          .addHeaders("STREAMLET", "POD", "READY", "STATUS", "RESTARTS")

        status.streamletsStatuses.foreach { streamletStatus =>
          if (streamletStatus.podsStatuses.isEmpty) {
            _streamletsTable.addDatas(
              streamletStatus.name,
              "",
              "0/0",
              "Missing",
              "0"
            )
          } else {
            streamletStatus.podsStatuses.foreach { podStatus =>
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
        ""
      }

    }

    appTable + endpointsTable + streamletsTable
  }
}
