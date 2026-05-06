# Module Dependencies

| Module | Purpose |
|--------|---------|
| `paimon-api` | Public API, manifest, and catalog interfaces |
| `paimon-common` | Shared data structures (`InternalRow`, `BinaryRow`), types, predicates |
| `paimon-format` | File format implementations (Parquet, ORC, Avro) |
| `paimon-arrow` | Apache Arrow integration for vectorized reads |
| `paimon-codegen` | Runtime code generation for hot paths (Scala compiler) |
| `paimon-codegen-loader` | Loader that bundles `paimon-codegen` into core at runtime |
| `paimon-core` | Core write/read/compaction, manifest, snapshot, table ops |
| `paimon-service-client` | Network service client (Netty-based) |
| `paimon-service-runtime` | Network service runtime server |
| `paimon-hive-common` | Hive-specific common utilities and serde |
| `paimon-hive-catalog` | Hive metastore catalog implementation |
| `paimon-bundle` | Uber module bundling core + format + codegen + hive-catalog |
| `paimon-lance` | Lance format support (depends on `paimon-arrow`) |
| `paimon-lumina` | Lumina JNI index support |
| `paimon-vortex` | Vortex format (JNI + format sub-modules) |
| `paimon-tantivy` | Tantivy index (JNI + index sub-modules) |
| `paimon-vfs` | Virtual filesystem abstraction (`paimon-vfs-common`, `paimon-vfs-hadoop`) |
| `paimon-filesystems` | Cloud filesystem implementations (S3, OSS, GCS, Azure, OBS, COSN, Jindo) |
| `paimon-flink-*` | Flink integration (source, sink, catalog, CDC) for Flink 1.16–1.20 / 2.0–2.2 |
| `paimon-spark-*` | Spark integration (datasource, procedures, catalog) for Spark 3.2–3.5 / 4.0 |
| `paimon-hive-connector-*` | Hive connector for Hive 2.1 / 2.2 / 2.3 / 3.1 |
| `paimon-test-utils` | Shared test utilities (JUnit, AssertJ, Testcontainers) |
| `paimon-docs` | Documentation generation module |
| `paimon-e2e-tests` | End-to-end integration tests |
| `paimon-benchmark` | Cluster and micro benchmarks |
| `paimon-ci-tools` | CI license-check tooling |

> Derived from `mvn dependency:tree -Dincludes=org.apache.paimon:*`
```
Foundation
├── paimon-api
└── paimon-test-utils

Common Layer
├── paimon-common (→ api)
│   ├── paimon-format (→ common, provided)
│   ├── paimon-arrow (→ common, provided)
│   ├── paimon-codegen (→ common)
│   └── paimon-hive-common (→ common, provided)
│
├── paimon-codegen-loader (→ common provided, codegen runtime optional)
│
├── paimon-jindo (→ common provided)
├── paimon-vfs-common (→ common provided)
└── paimon-gs-impl (→ common provided)

Core Layer
├── paimon-core (→ common, codegen-loader, format provided)
│   ├── paimon-service-client (→ common provided, core provided)
│   ├── paimon-hive-catalog (→ core provided, format provided, hive-common)
│   └── paimon-lance (→ arrow compile, common provided, core provided)
│
└── paimon-bundle (→ api, common, core, codegen-loader, format, hive-catalog)

Service & Extensions
├── paimon-service-runtime (→ common provided, core provided, service-client provided)
├── paimon-vortex-format (→ vortex-jni, arrow compile, common provided, core provided)
├── paimon-tantivy-index (→ tantivy-jni, common provided)
└── paimon-lumina (→ common provided)

Flink Integration
├── paimon-flink1-common (→ bundle, service-client, service-runtime)
├── paimon-flink-common (→ flink1-common, bundle, service-client, service-runtime)
├── paimon-flink-action (→ flink-common provided, bundle, service-client, service-runtime)
├── paimon-flink-cdc (→ flink-common provided, flink1-common, bundle, service-client, service-runtime)
└── paimon-flink-1.16 / 1.17 / 1.18 / 1.19 / 1.20
    (→ flink-common, flink1-common, flink-cdc, bundle, service-client, service-runtime)

Spark Integration
├── paimon-spark-common (→ bundle, hive-common)
├── paimon-spark3-common (→ spark-common, bundle, hive-common)
├── paimon-spark-ut (→ format, spark3-common, bundle, hive-common)
└── paimon-spark-3.2 / 3.3 / 3.4 / 3.5 (→ format, spark3-common, bundle, hive-common)

Hive Integration
├── paimon-hive-connector-common (→ hive-common, bundle)
├── paimon-hive-connector-2.1 / 2.1-cdh-6.3 / 2.2 (→ hive-connector-common)
├── paimon-hive-connector-2.3 (→ hive-connector-common)
└── paimon-hive-connector-3.1 (→ core, hive-connector-common)

FileSystems
├── paimon-hadoop-shaded (leaf)
├── paimon-hadoop-shaded-3.4 (leaf)
├── paimon-hadoop-uber (→ hadoop-shaded, common provided)
├── paimon-s3-impl (→ hadoop-shaded-3.4, common provided) → paimon-s3 (runtime)
├── paimon-oss-impl (→ hadoop-shaded, common provided) → paimon-oss (runtime)
├── paimon-cosn-impl (→ hadoop-shaded, common provided) → paimon-cosn (runtime)
├── paimon-azure-impl (→ hadoop-shaded-3.4, common provided) → paimon-azure (runtime)
├── paimon-obs-impl (→ hadoop-shaded, common provided) → paimon-obs (runtime)
├── paimon-jindodls (→ jindo provided, common provided)
├── paimon-gs (→ common compile, gs-impl runtime)
└── paimon-vfs-hadoop (→ vfs-common compile, common provided)

Test / Docs / Benchmark
├── paimon-e2e-tests (→ flink-1.20 runtime, flink-action runtime, hive-connector-common runtime, spark-3.3 runtime)
├── paimon-docs (→ core provided, hive-connector-common provided, flink-common provided, flink-cdc provided, spark-common provided)
├── paimon-micro-benchmarks (→ bundle, arrow)
└── paimon-cluster-benchmark (→ common)
```