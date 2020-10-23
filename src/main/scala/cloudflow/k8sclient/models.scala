package cloudflow
package k8sclient

object models {

  final case class CRSummary(
      name: String,
      namespace: String,
      version: String,
      creationTime: String
  )

}
