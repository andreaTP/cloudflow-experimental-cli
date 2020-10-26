package cloudflow
package k8sclient

import java.net.URL

object models {

  case class CRSummary(
      name: String,
      namespace: String,
      version: String,
      creationTime: String
  )

  case class EndpointStatus(name: String, url: URL)
  case class StreamletStatus(
      name: String,
      pod: String,
      ready: (Int, Int),
      status: String,
      restarts: Int
  )

  case class ApplicationStatus(
      summary: CRSummary,
      status: String,
      endpointStatuses: List[EndpointStatus],
      streamletStatuses: List[StreamletStatus]
  )

}
