import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.Informable;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

public class ConfigMapInformerExample {
    public static void main(String[] args) throws Exception {
        String namespace = "default";
        String name = "my-test";
        String key = "%s/%s".formatted(namespace, name);
        Config config = Config.fromKubeconfig("docker-desktop",
                IOUtils.toString(new FileInputStream("/Users/juntao/.kube/config")), null);
        config.setNamespace(namespace);
        try (KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build()) {
            System.out.println("成功连接到 Kubernetes 集群：" + client.getMasterUrl());
            Informable<ConfigMap> informable = client.configMaps().withName(name);
            try (SharedIndexInformer<ConfigMap> sharedIndexInformer = informable.inform(new ResourceEventHandler<>() {
                @Override
                public void onAdd(ConfigMap configMap) {
                    System.out.println("\n=== ConfigMap 新增 ===");
                    System.out.println("命名空间：" + configMap.getMetadata().getNamespace());
                    System.out.println("名称：" + configMap.getMetadata().getName());
                    System.out.println("数据：" + configMap.getData());
                }

                @Override
                public void onUpdate(ConfigMap oldConfigMap, ConfigMap newConfigMap) {
                    System.out.println("\n=== ConfigMap 更新 ===");
                    System.out.println("命名空间：" + newConfigMap.getMetadata().getNamespace());
                    System.out.println("名称：" + newConfigMap.getMetadata().getName());
                    System.out.println("旧版本：" + oldConfigMap.getMetadata().getResourceVersion() + " " + oldConfigMap.getData());
                    System.out.println("新版本：" + newConfigMap.getMetadata().getResourceVersion() + " " + newConfigMap.getData());
                }

                @Override
                public void onDelete(ConfigMap configMap, boolean deletedFinalStateUnknown) {
                    System.out.println("\n=== ConfigMap 删除 ===");
                    System.out.println("命名空间：" + configMap.getMetadata().getNamespace());
                    System.out.println("名称：" + configMap.getMetadata().getName());
                    System.out.println("未知最终状态：" + deletedFinalStateUnknown);
                }
            })) {
                ConfigMap map = sharedIndexInformer.getIndexer().getByKey(key);
                System.out.println("sharedIndexInformer: " + sharedIndexInformer + "\n" + map);
                TimeUnit.MINUTES.sleep(1);
            }
            System.out.println("====== stop =======");
        }
    }
}
