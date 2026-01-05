import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

/**
 * 使用 sharedIndexInformerFor(apiTypeClass, resyncPeriod) 实现全集群 ConfigMap 监听
 */
public class ConfigMapGlobalInformerExample {

    private static final long RESYNC_PERIOD = 30 * 1000L;

    public static void main(String[] args) throws Exception {

        Config config = Config.fromKubeconfig("docker-desktop",
                IOUtils.toString(new FileInputStream("/Users/juntao/.kube/config")), null);
        config.setNamespace("default");
        try (KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build()) {
            System.out.println("成功连接集群：" + client.getMasterUrl());
            // 2. 获取 SharedInformerFactory
            SharedInformerFactory informerFactory = client.informers();

            // 3. 调用重载方法创建全集群 ConfigMap 的 SharedIndexInformer
            Lister<ConfigMap> configMapLister;
            try (SharedIndexInformer<ConfigMap> configMapInformer = informerFactory
                    .sharedIndexInformerFor(ConfigMap.class, RESYNC_PERIOD)) {

                // 4. 注册资源事件监听器（Add/Update/Delete）
                configMapInformer.addEventHandler(new ResourceEventHandler<>() {
                    @Override
                    public void onAdd(ConfigMap configMap) {
                        System.out.println("\n=== 【全集群】ConfigMap 新增 ===");
                        System.out.println("命名空间：" + configMap.getMetadata().getNamespace());
                        System.out.println("名称：" + configMap.getMetadata().getName());
                        System.out.println("数据：" + configMap.getData());
                    }

                    @Override
                    public void onUpdate(ConfigMap oldConfigMap, ConfigMap newConfigMap) {
                        System.out.println("\n=== 【全集群】ConfigMap 更新 ===");
                        System.out.println("命名空间：" + newConfigMap.getMetadata().getNamespace());
                        System.out.println("名称：" + newConfigMap.getMetadata().getName());
                        System.out.println("旧版本：" + oldConfigMap.getMetadata().getResourceVersion());
                        System.out.println("新版本：" + newConfigMap.getMetadata().getResourceVersion());
                    }

                    @Override
                    public void onDelete(ConfigMap configMap, boolean deletedFinalStateUnknown) {
                        System.out.println("\n=== 【全集群】ConfigMap 删除 ===");
                        System.out.println("命名空间：" + configMap.getMetadata().getNamespace());
                        System.out.println("名称：" + configMap.getMetadata().getName());
                        System.out.println("未知最终状态：" + deletedFinalStateUnknown);
                    }
                });

                // 5. 通过 Lister 从本地缓存查询全集群 ConfigMap
                configMapLister = new Lister<>(configMapInformer.getIndexer());
            }
            System.out.println("\n=== 初始缓存中全集群 ConfigMap 数量：" + configMapLister.list().size() + " ===");
            configMapLister.list().forEach(cm ->
                    System.out.printf("- %s/%s%n", cm.getMetadata().getNamespace(), cm.getMetadata().getName())
            );

            // 6. 启动所有注册的 Informer
            informerFactory.startAllRegisteredInformers();
            System.out.println("\n=== 全集群 ConfigMap 监听器已启动，等待事件... ===");

            TimeUnit.MINUTES.sleep(10);
        } catch (Exception e) {
            System.err.println("监听器运行失败：" + e.getMessage());
        }
    }
}