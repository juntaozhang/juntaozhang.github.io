/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.paimon;

import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.CatalogContext;
import org.apache.paimon.catalog.CatalogFactory;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.data.InternalRow;
import org.apache.paimon.options.Options;
import org.apache.paimon.reader.RecordReader;
import org.apache.paimon.table.Table;
import org.apache.paimon.table.source.ReadBuilder;
import org.apache.paimon.table.source.TableRead;
import org.apache.paimon.table.source.TableScan;
import org.apache.paimon.types.DataField;

import java.util.HashMap;
import java.util.List;

/** Example class demonstrating how to read data from Paimon tables. */
public class PaimonReadExample {
    public static void main(String[] args) throws Exception {
        System.setProperty("aws.java.v1.disableDeprecationAnnouncement", "true");
        String warehouse = "s3a://warehouse/paimon";
        Options opts = new Options(new HashMap<>());
        opts.set("warehouse", warehouse);
        CatalogContext ctx = CatalogContext.create(opts);
        Catalog catalog = CatalogFactory.createCatalog(ctx);
        Table table = catalog.getTable(Identifier.create("ods", "order_fact"));
        List<DataField> fields = table.rowType().getFields();
        ReadBuilder readBuilder = table.newReadBuilder();
        TableScan.Plan plan = readBuilder.newScan().plan();
        TableRead read = readBuilder.newRead();
        fields.forEach(f -> System.out.print(f + "\t"));
        System.out.println();
        try (RecordReader<InternalRow> recordReader = read.createReader(plan)) {
            recordReader.forEachRemaining(
                    internalRow -> {
                        System.out.print(internalRow.getRowKind());
                        System.out.print("\t" + internalRow.getLong(0));
                        System.out.print("\t" + internalRow.getLong(1));
                        System.out.print("\t" + internalRow.getString(2));
                        System.out.print("\t" + internalRow.getInt(3));
                        System.out.print("\t" + internalRow.getDecimal(4, 10, 2));
                        System.out.print("\t" + internalRow.getTimestamp(5, 3));
                        System.out.print("\t" + internalRow.getString(6));
                        System.out.println("\t" + internalRow.getString(7));
                    });
        }

        catalog.close();
    }
}
