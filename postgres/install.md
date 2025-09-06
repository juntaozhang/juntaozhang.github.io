
# PostgreSQL Kubernetes Installation Guide

This guide provides step-by-step instructions for installing PostgreSQL on Kubernetes using Helm and the Bitnami chart.

## Installation

### 1. Add Bitnami Repository

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
```

### 2. Deploy PostgreSQL

Install PostgreSQL with custom configuration:

```bash
helm install postgresql bitnami/postgresql -f values.yaml
```

> ⏳ **Note:** Deployment typically takes 2-3 minutes. Monitor progress with `kubectl get pods`

## Access and Connection

### Service Endpoints

PostgreSQL is accessible within the cluster at:
- **Service:** `postgresql.default.svc.cluster.local:5432` or `postgresql:5432`

### Database Connection

#### Method 1: Temporary Client Pod

```bash
# Get database password
export POSTGRES_PASSWORD=$(kubectl get secret --namespace default postgresql -o jsonpath="{.data.postgres-password}" | base64 -d)

# Connect using temporary pod
kubectl run postgresql-client --rm --tty -i --restart='Never' \
  --namespace default \
  --image docker.io/bitnami/postgresql:17.6.0-debian-12-r4 \
  --env="PGPASSWORD=$POSTGRES_PASSWORD" \
  --command -- psql --host postgresql -U postgres -d postgres -p 5432
```

#### Method 2: Port Forward (for external access)

```bash
kubectl port-forward svc/postgresql 5432:5432
# Connect from local machine: psql -h localhost -U postgres -d postgres
```

## CDC Configuration

For Change Data Capture (CDC) functionality, configure Write-Ahead Logging:

### 1. Verify Current Settings

```sql
SHOW wal_level;               -- Must be 'logical'
SHOW max_replication_slots;   -- ≥ required replication slots
SHOW max_wal_senders;         -- ≥ max_replication_slots
```

### 2. Update Configuration

Modify `/opt/bitnami/postgresql/conf/postgresql.conf`:
```ini
wal_level = logical
```

### 3. Apply Changes

```bash
# Upgrade with new configuration
helm upgrade postgresql bitnami/postgresql -f values.yaml

```

### 4. Configure Table Replica Identity

```sql
-- Set table to include all columns in CDC
ALTER TABLE public.orders REPLICA IDENTITY FULL;

-- Verify setting (f = FULL, d = DEFAULT)
SELECT relreplident FROM pg_class WHERE oid = 'public.orders'::regclass;
```