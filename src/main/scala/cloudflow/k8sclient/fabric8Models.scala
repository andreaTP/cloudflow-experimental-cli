package cloudflow.k8sclient

import com.fasterxml.jackson.annotation.{
  JsonCreator,
  JsonIgnoreProperties,
  JsonProperty
}
import com.fasterxml.jackson.databind.{JsonDeserializer, PropertyName}
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api
import io.fabric8.kubernetes.api.model.{
  HasMetadata,
  KubernetesResource,
  Namespaced,
  ObjectMeta
}
import io.fabric8.kubernetes.client.utils.Serialization
import io.fabric8.kubernetes.client.{
  CustomResource,
  CustomResourceDoneable,
  CustomResourceList
}

import scala.collection.JavaConverters._

object fabric8Models {

  @JsonCreator
  case class Dummy(
      @JsonProperty("spec")
      spec: DummySpec,
      @JsonProperty("status")
      status: DummyStatus
  ) extends CustomResource
      with Namespaced {
//    @PropertyName("spec")
    // @JsonProperty("spec")
    // var spec: DummySpec = null
//    @PropertyName("status")
    // @JsonProperty("status")
    // var status: DummyStatus = null

    override def toString: String =
      "Dummy{" + "apiVersion='" + getApiVersion + '\'' + ", metadata=" + getMetadata + ", spec=" + spec + '}'

    // def getSpec: DummySpec = spec

    // def setSpec(spec: DummySpec): Unit = {
    //   this.spec = spec
    // }

    // def getStatus: DummyStatus = status

    // def setStatus(spec: DummyStatus): Unit = {
    //   this.status = status
    // }

    override def getMetadata: ObjectMeta = super.getMetadata
  }

  class DoneableDummy(
      val resource: Dummy,
      val function: api.builder.Function[Dummy, Dummy]
  ) extends CustomResourceDoneable[Dummy](resource, function) {}

  class DummyList extends CustomResourceList[Dummy] {}

  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  //  TODO when everything works remove this annotation!
  @JsonIgnoreProperties(ignoreUnknown = true)
  class DummySpec() extends KubernetesResource {
    var foo: String = null
    var bar: String = null
    override def toString: String =
      "DummySpec{" + "foo='" + foo + '\'' + ", bar='" + bar + '\'' + '}'

    def getFoo: String = foo

    def setFoo(foo: String): Unit = {
      this.foo = foo
    }

    def getBar: String = bar

    def setBar(bar: String): Unit = {
      this.bar = bar
    }
  }

  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  //  TODO when everything works remove this annotation!
  @JsonIgnoreProperties(ignoreUnknown = true)
  class DummyStatus() extends KubernetesResource {
    var foo: String = null
    var bar: String = null
//    @JsonProperty("wrapper")
//    var endpointStatuses: List[EndpointStatuses] = null
//    var streamlet_statuses: List[StreamletStatus] = null
    override def toString: String =
      "DummySpec{" + "foo='" + foo + '\'' + ", bar='" + bar + '\'' + '}'

    def getFoo: String = foo

    def setFoo(foo: String): Unit = {
      this.foo = foo
    }

    def getBar: String = bar

    def setBar(bar: String): Unit = {
      this.bar = bar
    }
  }

//  type StreamletStatus struct {
//    StreamletName string      `json:"streamlet_name"`
//    PodStatuses   []PodStatus `json:"pod_statuses"`
//  }
//
//  // EndpointStatus contains the status of the endpoint
//  type EndpointStatus struct {
//    StreamletName string `json:"streamlet_name"`
//    URL           string `json:"url"`
//  }

//  case class CloudflowApplication(
//      var spec: CloudflowApplicationSpec = null
//  ) extends CustomResource
//      with Namespaced
//      with models.CloudflowApplication {
//
//    override def toString: String =
//      "CloudflowApplication{" + "apiVersion='" + getApiVersion() + '\'' + ", metadata=" + getMetadata + ", spec=" + spec + '}'
//
//    def getSpec: CloudflowApplicationSpec = spec
//
//    def setSpec(spec: CloudflowApplicationSpec): Unit = {
//      this.spec = spec
//    }
//
//    def getStatus(): models.CloudflowApplicationStatus = ???
//
//    override def getMetadata: ObjectMeta = super.getMetadata
//  }
//
//  case class DoneableCloudflowApplication(
//      val resource: CloudflowApplication,
//      val function: api.builder.Function[
//        CloudflowApplication,
//        CloudflowApplication
//      ]
//  ) extends CustomResourceDoneable[CloudflowApplication](resource, function) {}
//
//  class CloudflowApplicationList[CloudflowApplication]
//      extends CustomResourceList[CloudflowApplication]
//      with models.CloudflowApplicationList[CloudflowApplication] {
//    def getScalaItems() = getItems.asScala.toList
//  }
//
//  @JsonDeserialize(using = classOf[JsonDeserializer.None])
//  case class CloudflowApplicationSpec(
//      var foo: String = null,
//      var bar: String = null
//  ) extends KubernetesResource
//      with models.CloudflowApplicationSpec {
//
//    def getAppId(): String = ???
//    def getAppVersion: String = ???
//    def getLibraryVersion: Option[String] = ???
//    def getVersion: Option[String] = ???
//
//    override def toString: String =
//      "DummySpec{" + "foo='" + foo + '\'' + ", bar='" + bar + '\'' + '}'
//
//    def getFoo: String = foo
//
//    def setFoo(foo: String): Unit = {
//      this.foo = foo
//    }
//
//    def getBar: String = bar
//
//    def setBar(bar: String): Unit = {
//      this.bar = bar
//    }
//  }

}
