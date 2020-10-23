package cloudflow.k8sclient

import java.net.URL

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}
import com.fasterxml.jackson.databind.{JsonDeserializer, JsonNode}
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api
import io.fabric8.kubernetes.api.model.{
  KubernetesResource,
  Namespaced,
  ObjectMeta
}
import io.fabric8.kubernetes.client.{
  CustomResource,
  CustomResourceDoneable,
  CustomResourceList
}

protected[k8sclient] object fabric8Models {

  @JsonCreator
  case class CloudflowApplication(
      @JsonProperty("spec")
      spec: CloudflowApplicationSpec,
      @JsonProperty("status")
      status: CloudflowApplicationStatus
  ) extends CustomResource
      with Namespaced {
    override def getMetadata: ObjectMeta = super.getMetadata
  }

  @JsonCreator
  class DoneableCloudflowApplication(
      val resource: CloudflowApplication,
      val function: api.builder.Function[
        CloudflowApplication,
        CloudflowApplication
      ]
  ) extends CustomResourceDoneable[CloudflowApplication](resource, function) {}

  @JsonCreator
  class CloudflowApplicationList
      extends CustomResourceList[CloudflowApplication] {}

  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  @JsonCreator
  case class Attribute(
      @JsonProperty("attribute_name")
      attributeName: String,
      @JsonProperty("config_path")
      configPath: String
  ) extends KubernetesResource {}

  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  @JsonCreator
  case class InOutletSchema(
      @JsonProperty("fingerprint")
      fingerprint: String,
      @JsonProperty("schema")
      schema: String,
      @JsonProperty("name")
      name: String,
      @JsonProperty("format")
      format: String
  ) extends KubernetesResource {}

  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  @JsonCreator
  case class InOutlet(
      @JsonProperty("name")
      name: String,
      @JsonProperty("schema")
      schema: InOutletSchema
  ) extends KubernetesResource {}

  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  @JsonCreator
  case class ConfigParameterDescriptor(
      @JsonProperty("key")
      key: String,
      @JsonProperty("description")
      description: String,
      @JsonProperty("validation_type")
      validationType: String,
      @JsonProperty("validation_pattern")
      validationPattern: String,
      @JsonProperty("default_value")
      defaultValue: String
  ) extends KubernetesResource {}

  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  @JsonCreator
  case class Descriptor(
      @JsonProperty("attributes")
      attributes: List[Attribute],
      @JsonProperty("class_name")
      className: String,
      @JsonProperty("config_parameters")
      configParameters: List[ConfigParameterDescriptor],
      @JsonProperty("volume_mounts")
      volumeMounts: List[VolumeMountDescriptor],
      @JsonProperty("inlets")
      inlets: List[InOutlet],
      @JsonProperty("labels")
      labels: List[String],
      @JsonProperty("outlets")
      outlets: List[InOutlet],
      @JsonProperty("runtime")
      string: String,
      @JsonProperty("description")
      Description: String
  ) extends KubernetesResource {}
  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  @JsonCreator
  case class Streamlet(
      @JsonProperty("name")
      name: String,
      @JsonProperty("descriptor")
      descriptor: Descriptor
  ) extends KubernetesResource {}

  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  @JsonCreator
  case class Endpoint(
      @JsonProperty("app_id")
      appId: Option[String],
      @JsonProperty("streamlet")
      streamlet: Option[String],
      @JsonProperty("container_port")
      containerPort: Option[Int]
  ) extends KubernetesResource {}

  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  @JsonCreator
  case class VolumeMountDescriptor(
      @JsonProperty("app_id")
      appId: String,
      @JsonProperty("path")
      path: String,
      @JsonProperty("access_mode")
      accessMode: String,
      @JsonProperty("pvc_name")
      pvcName: Option[String]
  ) extends KubernetesResource {}

  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  @JsonCreator
  case class PortMapping(
      @JsonProperty("id")
      id: String,
      @JsonProperty("config")
      config: JsonNode,
      @JsonProperty("cluster")
      path: Option[String]
  ) extends KubernetesResource {}

  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  @JsonCreator
  case class Deployment(
      @JsonProperty("class_name")
      className: String,
      @JsonProperty("config")
      config: JsonNode,
      @JsonProperty("image")
      image: String,
      @JsonProperty("name")
      name: String,
      @JsonProperty("port_mappings")
      portMappings: Map[String, PortMapping],
      @JsonProperty("volume_mounts")
      volumeMounts: List[VolumeMountDescriptor],
      @JsonProperty("runtime")
      runtime: String,
      @JsonProperty("streamlet_name")
      streamletName: String,
      @JsonProperty("secret_name")
      secretName: String,
      @JsonProperty("endpoint")
      endpoint: Option[Endpoint],
      @JsonProperty("replicas")
      replicas: Int
  ) extends KubernetesResource {}

  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  @JsonCreator
  case class CloudflowApplicationSpec(
      @JsonProperty("app_id")
      appId: String,
      @JsonProperty("app_version")
      appVersion: String,
      @JsonProperty("deployments")
      deployments: List[Deployment],
      @JsonProperty("streamlets")
      streamlets: List[Streamlet],
      @JsonProperty("agent_paths")
      agentPaths: Map[String, String],
      @JsonProperty("version")
      version: Option[String],
      @JsonProperty("library_version")
      libraryVersion: Option[String]
  ) extends KubernetesResource {}
  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  @JsonCreator
  case class EndpointStatus(
      @JsonProperty("streamlet_name")
      streamletName: String,
      @JsonProperty("url")
      url: URL // check if works...
  ) extends KubernetesResource {}

  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  @JsonCreator
  case class PodStatus(
      @JsonProperty("name")
      name: String,
      @JsonProperty("ready")
      ready: String,
      @JsonProperty("nr_of_containers_ready")
      nrOfContainersReady: String,
      @JsonProperty("nr_of_containers")
      nrOfContainers: String,
      @JsonProperty("restarts")
      restarts: Int,
      @JsonProperty("status")
      status: String
  ) extends KubernetesResource {}

  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  @JsonCreator
  case class StreamletStatus(
      @JsonProperty("streamlet_name")
      streamletName: String,
      @JsonProperty("expected_pod_count")
      expectedPodCount: Option[Int],
      @JsonProperty("pod_statuses")
      podStatuses: List[PodStatus]
  ) extends KubernetesResource {}

  @JsonDeserialize(using = classOf[JsonDeserializer.None])
  @JsonCreator
  case class CloudflowApplicationStatus(
      @JsonProperty("app_id")
      appId: String,
      @JsonProperty("app_version")
      appVersion: String,
      @JsonProperty("app_status")
      appStatus: String,
      @JsonProperty("endpoint_statuses")
      endpointStatuses: List[EndpointStatus],
      @JsonProperty("streamlet_statuses")
      streamletStatuses: List[StreamletStatus]
  ) extends KubernetesResource {}

}
