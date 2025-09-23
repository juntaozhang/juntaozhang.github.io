# Spark 3.5.5 on Kubernetes

## Prerequisites

### Kubernetes Cluster Information
```bash
kubectl cluster-info
```

Expected output:
```
Kubernetes control plane is running at https://kubernetes.docker.internal:6443
CoreDNS is running at https://kubernetes.docker.internal:6443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
```

### Service Account Setup
Ensure the `spark-operator-spark` service account has the necessary permissions to create and manage pods.

## Deploy Modes

### 1. Cluster Mode

In cluster mode, the Spark driver runs inside the Kubernetes cluster as a pod.

```bash
./bin/spark-submit \
    --master k8s://https://kubernetes.docker.internal:6443 \
    --deploy-mode cluster \
    --name spark-pi \
    --class org.apache.spark.examples.SparkPi \
    --conf spark.executor.instances=2 \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark-operator-spark \
    --conf spark.kubernetes.appKillPodDeletionGracePeriod=300 \
    --conf spark.kubernetes.container.image=spark:3.5.5 \
    local:///opt/spark/examples/jars/spark-examples.jar 10000
```

#### Monitoring Cluster Mode Applications

Access the Spark UI by port-forwarding to the driver pod:

```bash
# Port forward to access Spark UI
kubectl port-forward <driver-pod-name> 4040:4040
```

Then access the UI at: http://localhost:4040

### 2. Client Mode

In client mode, the Spark driver runs on the local machine and connects to the Kubernetes cluster for executors.

```bash
# Get the local IP address for driver connectivity
export DRIVER_HOST=$(ifconfig | awk '/inet / && $2!="127.0.0.1"{print $2; exit}')

# Submit Spark application
./bin/spark-submit \
    --master k8s://https://kubernetes.docker.internal:6443 \
    --deploy-mode client \
    --name spark-pi \
    --conf spark.driver.host=$DRIVER_HOST \
    --class org.apache.spark.examples.SparkPi \
    --conf spark.executor.instances=2 \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark-operator-spark \
    --conf spark.kubernetes.appKillPodDeletionGracePeriod=300 \
    --conf spark.kubernetes.container.image=spark:3.5.5 \
    local:///opt/spark/examples/jars/spark-examples.jar 100
```

## Spark SQL on Kubernetes

Run Spark SQL in client mode:

```bash
./bin/spark-sql \
    --master k8s://https://kubernetes.docker.internal:6443 \
    --deploy-mode client \
    --name spark-sql \
    --conf spark.driver.host=$DRIVER_HOST \
    --conf spark.executor.instances=2 \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark-operator-spark \
    --conf spark.kubernetes.appKillPodDeletionGracePeriod=300 \
    --conf spark.kubernetes.container.image=spark:3.5.5
```

### Example SQL Queries

```sql
-- Create a temporary view
CREATE TEMPORARY VIEW t_users AS
VALUES (1, 'Alice'), (2, 'Bob'), (3, 'Charlie') AS t(id, name);

-- Query the data
SELECT count(1) FROM t_users WHERE id > 1;
```


## References

- [Spark on Kubernetes Official Documentation](https://spark.apache.org/docs/3.5.5/running-on-kubernetes.html)
- OneNote: 250506 Spark on K8S