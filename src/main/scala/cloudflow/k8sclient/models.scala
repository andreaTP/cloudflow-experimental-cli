package cloudflow.k8sclient

object models {

  trait CloudflowApplication {
    def getSpec(): CloudflowApplicationSpec

    def getStatus(): CloudflowApplicationStatus

  }

  trait CloudflowApplicationList[+T <: CloudflowApplication] {
    def getScalaItems(): List[T]
  }

  trait CloudflowApplicationStatus {
    def getAppId(): String

    def getAppVersion(): String

    def getAppStatus(): String
  }

  trait CloudflowApplicationSpec {
    def getAppId(): String

    def getAppVersion: String

    def getVersion: Option[String]

    def getLibraryVersion: Option[String]
  }

}
