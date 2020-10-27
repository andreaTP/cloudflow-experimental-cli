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

  case class ContainersReady(ready: Int, total: Int)
  case class PodStatus(
      name: String,
      ready: ContainersReady,
      status: String,
      restarts: Int
  )

  case class EndpointStatus(name: String, url: URL)
  case class StreamletStatus(
      name: String,
      podsStatuses: List[PodStatus]
  )

  case class ApplicationStatus(
      summary: CRSummary,
      status: String,
      endpointsStatuses: List[EndpointStatus],
      streamletsStatuses: List[StreamletStatus]
  )

}
