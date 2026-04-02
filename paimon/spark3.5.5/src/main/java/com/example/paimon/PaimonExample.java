package com.example.paimon;

import java.util.List;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.FileStore;
import org.apache.paimon.Snapshot;
import org.apache.paimon.catalog.*;
import org.apache.paimon.manifest.ManifestEntry;
import org.apache.paimon.manifest.ManifestFile;
import org.apache.paimon.manifest.ManifestFileMeta;
import org.apache.paimon.manifest.ManifestList;
import org.apache.paimon.options.Options;
import org.apache.paimon.shade.guava30.com.google.common.collect.Streams;
import org.apache.paimon.table.FileStoreTable;

public class PaimonExample {
    public static void main(String[] args) throws Exception {
        Options options = Options.fromMap(Map.of(
                "warehouse", "s3://warehouse/paimon",
                "s3.path.style.access", "true",
                "s3.access-key", "test",
                "s3.secret-key", "11111111",
                "s3.endpoint", "http://localhost:32000"
        ));
        CatalogContext catalogContext = CatalogContext.create(options, new Configuration());
        try (Catalog catalog = CatalogFactory.createCatalog(catalogContext)) {
            FileStoreTable fileStoreTable = (FileStoreTable)catalog.getTable(Identifier.create("ods", "postpone_bucket_table"));
            FileStore<?> store = fileStoreTable.store();
            List<Snapshot> compactSnapshots =
                    Streams.stream(store.snapshotManager().snapshots())
                            .filter(s -> s.commitKind() == Snapshot.CommitKind.COMPACT)
                            .toList();
            Snapshot s = compactSnapshots.get(0);
            ManifestFile manifestFile = store.manifestFileFactory().create();
            ManifestList manifestList = store.manifestListFactory().create();
            List<ManifestFileMeta> manifestFileMetas = manifestList.readDeltaManifests(s);
            for (ManifestFileMeta manifestFileMeta : manifestFileMetas) {
                List<ManifestEntry> compactManifestEntries =
                        manifestFile.read(manifestFileMeta.fileName());
                System.out.println(compactManifestEntries.get(7).file().creationTimeEpochMillis());
                System.out.println(compactManifestEntries.get(7).file().creationTime().getMillisecond());
            }
            System.out.println(fileStoreTable);
        }
    }
}
