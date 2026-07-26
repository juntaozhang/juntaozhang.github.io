import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.extended.leaderelection.LeaderCallbacks;
import io.fabric8.kubernetes.client.extended.leaderelection.LeaderElectionConfig;
import io.fabric8.kubernetes.client.extended.leaderelection.LeaderElectionConfigBuilder;
import io.fabric8.kubernetes.client.extended.leaderelection.LeaderElector;
import io.fabric8.kubernetes.client.extended.leaderelection.resourcelock.ConfigMapLock;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KubernetesLeaderElectorExample {
    public static class KubernetesLeaderElector {
        private final KubernetesClient client;
        private final String namespace;
        private final String lockIdentity;
        private final String configMapName;
        private final String id;
        private final LeaderElectionConfig leaderElectionConfig;
        private volatile boolean isLeader = false;

        public KubernetesLeaderElector(String namespace, String configMapName, KubernetesClient client) {
            this.lockIdentity = UUID.randomUUID().toString();
            this.client = client;
            this.configMapName = configMapName;
            this.id = configMapName + "-" + lockIdentity;
            this.namespace = namespace;

            // 2. 组装 LeaderElectionConfig
            this.leaderElectionConfig = new LeaderElectionConfigBuilder()
                    .withName(configMapName)
                    .withLeaseDuration(Duration.ofSeconds(15))
                    .withLock(new ConfigMapLock(
                            new ObjectMetaBuilder()
                                    .withNamespace(namespace)
                                    .withName(configMapName)
                                    // Labels will be used to clean up the ha related
                                    // ConfigMaps.
                                    .withLabels(Map.of(
                                            "configmap-type", "high-availability",
                                            "type", "flink-native-kubernetes",
                                            "app", id
                                    ))
                                    .build(),
                            lockIdentity))
                    .withRenewDeadline(Duration.ofSeconds(15))   // 租约长度
                    .withRetryPeriod(Duration.ofSeconds(2))
                    .withReleaseOnCancel(true)
                    .withLeaderCallbacks(new LeaderCallbacks(
                            this::onStartLeading,
                            this::onStopLeading,
                            (newLeader) -> {
                                System.out.printf("New leader elected %s for %s.%n", newLeader, configMapName);
                            }))
                    .build();


        }

        public void start() {
            LeaderElector elector = new LeaderElector(client, leaderElectionConfig, Executors.newSingleThreadExecutor());
            elector.start();
            System.out.printf(
                    "Triggered leader election on lock %s.%n", leaderElectionConfig.getLock().describe());
        }

        public void onStartLeading() {
            System.out.println("===== start =====");
            System.out.println(">>> [" + id + "] I am the leader now!");
            // TODO do some business logic

            ConfigMap updated = client.configMaps().inNamespace(namespace).withName(configMapName).get();
            updated.getData().put("id", id);
            updated.getData().put("time", new Date().toString());
            client.resource(updated).update();
            this.isLeader = true;
        }

        public void onStopLeading() {
            System.out.println(">>> [" + id + "] leadership lost.");
            System.out.println("===== end =====");
            // TODO do some business logic
            this.isLeader = false;
        }

        public void keepProgramRunning() throws InterruptedException {
            while (true) {
                if (isLeader) {
                    System.out.printf("进程 %s（MASTER）：心跳正常%n", lockIdentity);
                } else {
                    System.out.printf("进程 %s（SLAVE）：等待主节点故障，准备接管...%n", lockIdentity);
                }
                TimeUnit.SECONDS.sleep(5);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String namespace = "default";
        Config config = Config.fromKubeconfig("docker-desktop",
                IOUtils.toString(new FileInputStream("/Users/juntao/.kube/config")), null);
        config.setNamespace(namespace);
        try (KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build()) {
            KubernetesLeaderElector elector = new KubernetesLeaderElector(namespace, "ha-test", client);
            elector.start();
            elector.keepProgramRunning();
        }
    }
}