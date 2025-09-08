# Paimon Changelog and Audit Log Demo

This document demonstrates how to work with Paimon changelog tables and audit log queries for tracking data changes.

## Overview

Paimon changelog tables enable tracking of all data modifications (INSERT, UPDATE, DELETE) with full audit capabilities. This demo shows:

- Creating changelog-enabled tables
- Performing CRUD operations
- Querying audit logs with different scan modes
- Cleanup operations

## Setup

```sql
-- Set execution mode to batch for consistent results
SET 'execution.runtime-mode' = 'batch';
SET 'sql-client.execution.result-mode' = 'tableau';
```

## Create Changelog Table

```sql
CREATE TABLE order_fact_changelog (
  order_id BIGINT,
  amount   INT,
  PRIMARY KEY (order_id) NOT ENFORCED
) WITH (
  'bucket' = '1',
  'merge-engine' = 'deduplicate',
  'changelog-producer' = 'lookup'   -- Enable changelog output (options: lookup, full-compaction)
);
```

### Configuration Parameters

- `bucket = '1'`: Single bucket for simplicity
- `merge-engine = 'deduplicate'`: Deduplication strategy for primary key conflicts
- `changelog-producer = 'lookup'`: Enables changelog generation for audit tracking

## Data Operations

### Insert Operations
```sql
-- Insert initial record
INSERT INTO order_fact_changelog VALUES (1, 100);
```

### Update Operations
```sql
-- Insert duplicate key (will trigger deduplication)
INSERT INTO order_fact_changelog VALUES (1, 200);

-- Update existing record
UPDATE order_fact_changelog SET amount = 200 WHERE order_id = 1;
```

### Delete Operations
```sql
-- Delete record
DELETE FROM order_fact_changelog WHERE order_id = 1;
```

## Audit Log Queries

### Delta Scan Mode
Query changes between specific snapshots:

```sql
SELECT * FROM order_fact_changelog$audit_log
/*+ OPTIONS('incremental-between'='1,6',
            'incremental-between-scan-mode'='DELTA') */;
```

### Changelog Scan Mode
Query detailed change events between snapshots:

```sql
SELECT * FROM order_fact_changelog$audit_log
/*+ OPTIONS('incremental-between'='1,2',
            'incremental-between-scan-mode'='CHANGELOG') */;
```

### Latest Snapshot
Query the current state:

```sql
SELECT * FROM order_fact_changelog$audit_log 
/*+ OPTIONS('scan.mode'='latest') */;
```

## Scan Mode Comparison

| Scan Mode | Purpose | Output |
|-----------|---------|---------|
| `DELTA` | Show net changes between snapshots | Final state differences |
| `CHANGELOG` | Show all change events | Detailed change log with operations |
| `latest` | Show current snapshot | Current table state |

## Cleanup

### Drop Table
```sql
DROP TABLE order_fact_changelog;
```

### Remove Storage Files
```bash
# Force remove the table directory from MinIO
mc rb --force localminio/warehouse/paimon/ods.db/order_fact_changelog
```

## References

- [Paimon Changelog Documentation](https://paimon.apache.org/docs/master/concepts/changelog/)
- [Paimon Audit Log Queries](https://paimon.apache.org/docs/master/flink/sql-query/#audit-log)
