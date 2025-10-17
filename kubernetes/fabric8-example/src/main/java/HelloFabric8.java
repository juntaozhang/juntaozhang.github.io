import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import io.fabric8.kubernetes.api.model.autoscaling.v2.*;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetrics;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetricsList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HelloFabric8 {
    static KubernetesClient client = new DefaultKubernetesClient();
    static String namespace = "default";
    static String deploymentName = "helloworld";
    static String serviceName = "helloworld-svc";
    static Map<String, String> podLabels = new HashMap<>();

    static {
        podLabels.put("app", "helloworld");
    }

    public static void main(String[] args) throws Exception {
//        createDeployment();
//        scaleUpDeployment(1);
//        createSvc();
//        updateSvc();
        createHPA();
//        metrics();
    }

    public static void updateSvc() {
        Service service = client.services().inNamespace(namespace).withName(serviceName).get();
        if (service != null) {
            ServicePort servicePort = new ServicePort();
            servicePort.setPort(18004);
            servicePort.setTargetPort(new IntOrString(80));
            ServiceSpec serviceSpec = new ServiceSpec();
            serviceSpec.setType("LoadBalancer");
            serviceSpec.setPorts(Collections.singletonList(servicePort));
            serviceSpec.setSelector(podLabels);
            service.setSpec(serviceSpec);
            client.services().inNamespace(namespace)
                    .resource(service)
                    .createOr(NonDeletingOperation::update);
            System.out.println("Service type updated to LoadBalancer successfully!");
        } else {
            System.out.println("Service not found!");
        }
    }

    public static void createSvc() {
        ServicePort servicePort = new ServicePort();
        servicePort.setPort(18002);
        servicePort.setTargetPort(new IntOrString(80));
        ServiceSpec serviceSpec = new ServiceSpec();
        serviceSpec.setPorts(Collections.singletonList(servicePort));
        serviceSpec.setSelector(podLabels);
        Service service = new Service();
        service.setApiVersion("v1");
        service.setKind("Service");
        ObjectMeta serviceMetadata = new ObjectMeta();
        serviceMetadata.setName(serviceName);
        service.setMetadata(serviceMetadata);
        service.setSpec(serviceSpec);
        client.services().inNamespace(namespace)
                .resource(service)
                .createOr(NonDeletingOperation::update);
        System.out.println("Service created successfully!");
    }

    public static void createDeployment() {
        ContainerPort containerPort = new ContainerPort();
        containerPort.setContainerPort(80);

        ResourceRequirements requirements = new ResourceRequirements();
        requirements.setLimits(Collections.singletonMap("cpu", new Quantity("100m")));
        requirements.setRequests(Collections.singletonMap("cpu", new Quantity("100m")));

        Container container = new Container();
        container.setName("nginx");
        container.setImage("nginx:latest");
        container.setPorts(Collections.singletonList(containerPort));
        container.setResources(requirements);

        PodSpec podSpec = new PodSpec();
        podSpec.setContainers(Collections.singletonList(container));

        PodTemplateSpec podTemplateSpec = new PodTemplateSpec();
        podTemplateSpec.setMetadata(new ObjectMeta());
        podTemplateSpec.getMetadata().setLabels(podLabels);
        podTemplateSpec.setSpec(podSpec);

        DeploymentSpec deploymentSpec = new DeploymentSpec();
        deploymentSpec.setReplicas(1);
        deploymentSpec.setTemplate(podTemplateSpec);
        deploymentSpec.setSelector(new LabelSelector());
        deploymentSpec.getSelector().setMatchLabels(podLabels);

        Deployment deployment = new Deployment();
        deployment.setApiVersion("apps/v1");
        deployment.setKind("Deployment");
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(deploymentName);
        deployment.setMetadata(metadata);
        deployment.setSpec(deploymentSpec);

        client.apps().deployments().inNamespace(namespace)
                .resource(deployment)
                .createOr(NonDeletingOperation::update);
        System.out.println("Deployment created successfully!");
    }

    public static void scaleUpDeployment(int scale) {
        client.apps().deployments().inNamespace(namespace).withName(deploymentName).scale(scale);
    }

    /**
     * Kubernetes Horizontal Pod Autoscaler (HPA)
     * (base) ➜  DLS git:(master) ✗ k autoscale deployment.apps/helloworld --cpu-percent=10 --min=1 --max=3 -n test
     */
    public static void createHPA() {
        MetricTarget metricTarget = new MetricTargetBuilder()
                .withType("Utilization")
                .withAverageUtilization(8)
                .build();

        ResourceMetricSource resourceMetricSource = new ResourceMetricSourceBuilder()
                .withName("cpu")
                .withTarget(metricTarget)
                .build();

        MetricSpec metricSpec = new MetricSpecBuilder()
                .withType("Resource")
                .withResource(resourceMetricSource)
                .build();

        HorizontalPodAutoscaler hpa = new HorizontalPodAutoscalerBuilder()
                .withNewMetadata()
                    .withName(deploymentName)
                .endMetadata()
                .withNewSpec()
                    .withNewScaleTargetRef()
                    .withApiVersion("apps/v1")
                    .withKind("Deployment")
                    .withName(deploymentName)
                .endScaleTargetRef()
                .withMinReplicas(1)
                .withMaxReplicas(3)
                .withMetrics(metricSpec)
                .endSpec()
                .build();

        client.autoscaling().v2().horizontalPodAutoscalers().inNamespace(namespace)
                .resource(hpa).createOr(NonDeletingOperation::update);
        System.out.println("HPA created successfully!");
    }

    public static void metrics() {
        PodMetricsList podMetricsList = client.top().pods().metrics(namespace);
        for (PodMetrics podMetrics : podMetricsList.getItems()) {
            System.out.println("Pod: " + podMetrics.getMetadata().getName());
            podMetrics.getContainers().forEach(container -> {
                System.out.println("  Container: " + container.getName());
                System.out.println("    CPU usage: " + container.getUsage().get("cpu"));
                System.out.println("    Memory usage: " + container.getUsage().get("memory"));
            });
        }
    }
}
