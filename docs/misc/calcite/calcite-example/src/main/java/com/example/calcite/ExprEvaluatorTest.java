/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.calcite;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.calcite.DataContext;
import org.apache.calcite.DataContexts;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexExecutable;
import org.apache.calcite.rex.RexExecutorImpl;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.util.SqlBasicVisitor;
import org.apache.calcite.sql.util.SqlOperatorTables;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

public class ExprEvaluatorTest {
    public static void main(String[] args) throws Exception {
        ExprEvaluator evaluator = new ExprEvaluator("test_table", new TestTable(), new HashMap<>() {{
            put("my_udf1", Udf1.class);
            put("my_udf2", Udf2.class);
            put("my_udf3", Udf3.class);
            put("my_udf4", Udf4.class);
        }});
        testCase1(evaluator);
        testCase2(evaluator);
        testCase3(evaluator);
    }

    private static void testCase1(ExprEvaluator evaluator) throws SqlParseException {
        String expressionStr =
                "CASE "
                        + "  WHEN my_udf1(a, b, d.i) > 20 THEN 1 "
                        + "  WHEN my_udf2(a, b, d.m['a']) > 10 THEN 2 "
                        + "  ELSE 3 "
                        + "END";
        evaluator.compile("metric1", expressionStr);
        System.out.println(evaluator.evaluate("metric1", new Object[]{
                10, 5, 2,
                new Object[]{
                        new HashMap<String, Integer>() {
                            {
                                put("a", 4);
                            }
                        },
                        null,
                        10
                }
        }));
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1_000_000; i++) {
            int finalI = i;
            evaluator.evaluate("metric1", new Object[]{
                    10 + finalI,
                    5 + finalI,
                    2 + finalI,
                    new Object[]{
                            new HashMap<String, Integer>() {
                                {
                                    put("a", 4 + finalI);
                                }
                            },
                            null,
                            10 + finalI
                    }});
        }
        System.out.println("Performance test[1M loop] cost: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    private static void testCase2(ExprEvaluator evaluator) throws SqlParseException {
        String expressionStr =
                "my_udf3(a, d.i) + my_udf3(b, d.m['a'])";
        evaluator.compile("metric2", expressionStr);
        System.out.println(evaluator.evaluate("metric2", new Object[]{
                10, 5, null,
                new Object[]{
                        new HashMap<String, Integer>() {
                            {
                                put("a", 4);
                            }
                        },
                        null,
                        10
                }
        }));
    }

    private static void testCase3(ExprEvaluator evaluator) throws SqlParseException {
        evaluator.compile("metric3", "my_udf4(d.arr)");
        System.out.println(evaluator.evaluate("metric3", new Object[]{
                null, null, null,
                new Object[]{
                        null,
                        Lists.newArrayList(1, 2, 3, 4, 5),
//                        new Integer[]{1, 2, 3, 4, 5},
                        null
                }
        }));
    }

    public static class ExprEvaluator {
        private final String tableName;
        private final AbstractTable table;
        private final Map<String, RexExecutable> compiledExpressions = new HashMap<>();
        private final RelDataTypeFactory typeFactory;
        private final SqlValidator validator;
        private final SqlToRelConverter converter;
        private final RexBuilder rexBuilder;

        public ExprEvaluator(String tableName, AbstractTable table, Map<String, Class<?>> UDFs) {
            this.tableName = tableName;
            this.table = table;
            CalciteSchema calciteSchema = CalciteSchema.createRootSchema(false, true);
            calciteSchema.add(tableName, table);
            SchemaPlus schemaPlus = calciteSchema.plus();
            for (Map.Entry<String, Class<?>> entry : UDFs.entrySet()) {
                schemaPlus.add(
                        entry.getKey(),
                        Objects.requireNonNull(ScalarFunctionImpl.create(entry.getValue(), "eval")));
            }
            FrameworkConfig config = Frameworks.newConfigBuilder().defaultSchema(schemaPlus).build();

            CalciteConnectionConfigImpl connectionConfig =
                    new CalciteConnectionConfigImpl(
                            new java.util.Properties() {
                                {
                                    put(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), "false");
                                }
                            });

            typeFactory = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
            rexBuilder = new RexBuilder(typeFactory);

            CalciteCatalogReader catalogReader =
                    new CalciteCatalogReader(
                            calciteSchema.root(), calciteSchema.path(null), typeFactory, connectionConfig);

            validator =
                    SqlValidatorUtil.newValidator(
                            SqlOperatorTables.chain(SqlStdOperatorTable.instance(), catalogReader),
                            catalogReader,
                            typeFactory,
                            config.getSqlValidatorConfig());

            RelOptPlanner planner = new VolcanoPlanner(config.getCostFactory(), config.getContext());
            RelOptCluster cluster = RelOptCluster.create(planner, rexBuilder);

            converter =
                    new SqlToRelConverter(
                            null,
                            validator,
                            catalogReader,
                            cluster,
                            config.getConvertletTable(),
                            SqlToRelConverter.config());
        }

        public void compile(String name, String expressionStr) throws SqlParseException {
            RelDataType tableRowType = table.getRowType(typeFactory);
            String selectExpr = "SELECT " + expressionStr + " FROM " + tableName;
            SqlParser selectParser = SqlParser.create(selectExpr);
            SqlSelect selectSqlNode = (SqlSelect) selectParser.parseQuery();
            SqlNodeList modifiedSqlNodeListNode =
                    (SqlNodeList) new SqlNodeAliasVisitor(tableName).visit(selectSqlNode.getSelectList());
            selectSqlNode.setSelectList(modifiedSqlNodeListNode);
            SqlNode validatedSelectSqlNode = validator.validate(selectSqlNode);
            SqlSelect validatedSelect = (SqlSelect) validatedSelectSqlNode;

            RelNode relNode = converter.convertSelect(validatedSelect, true);
            RexExecutable executable =
                    RexExecutorImpl.getExecutable(rexBuilder, ((Project) relNode).getProjects(), tableRowType);
            compiledExpressions.put(name, executable);
        }

        public Object evaluate(String exprName, Object[] row) {
            RexExecutable executable = compiledExpressions.get(exprName);
            Preconditions.checkArgument(executable != null, exprName + " should be compiled first");
            DataContext dataContext =
                    DataContexts.of(
                            name -> {
                                if (name.equals("inputRecord")) {
                                    return row;
                                } else {
                                    throw new IllegalArgumentException("Unknown variable: " + name);
                                }
                            });
            executable.setDataContext(dataContext);
            Object[] result = executable.execute();
            assert result != null;
            return result[0];
        }
    }

    static class SqlNodeAliasVisitor extends SqlBasicVisitor<SqlNode> {
        private final String tableAlias;

        public SqlNodeAliasVisitor(String tableAlias) {
            this.tableAlias = tableAlias;
        }

        @Override
        public SqlNode visit(SqlIdentifier id) {
            if (id.isSimple()) {
                List<String> names = new ArrayList<>();
                names.add(tableAlias);
                names.addAll(id.names);
                return new SqlIdentifier(names, id.getParserPosition());
            } else {
                List<String> names = new ArrayList<>();
                names.add(tableAlias);
                names.addAll(id.names);
                return new SqlIdentifier(names, id.getParserPosition());
            }
        }

        @Override
        public SqlNode visit(SqlCall call) {
            List<SqlNode> newOperands = new ArrayList<>();
            for (SqlNode operand : call.getOperandList()) {
                if (operand == null) {
                    newOperands.add(null);
                    continue;
                }
                newOperands.add(operand.accept(this));
            }
            return call.getOperator()
                    .createCall(call.getParserPosition(), newOperands.toArray(SqlNode.EMPTY_ARRAY));
        }

        @Override
        public SqlNode visit(SqlNodeList nodeList) {
            boolean changed = false;
            List<SqlNode> newNodes = new ArrayList<>();

            for (SqlNode node : nodeList) {
                SqlNode newNode = node.accept(this);
                newNodes.add(newNode);
                if (newNode != node) {
                    changed = true;
                }
            }

            if (changed) {
                return new SqlNodeList(newNodes, nodeList.getParserPosition());
            }
            return nodeList;
        }

        @Override
        public SqlNode visit(@NonNull SqlLiteral literal) {
            return literal;
        }

        @Override
        public SqlNode visit(@NonNull SqlIntervalQualifier node) {
            return node;
        }

        @Override
        public SqlNode visit(@NonNull SqlDynamicParam param) {
            return param;
        }

        @Override
        public SqlNode visit(@NonNull SqlDataTypeSpec type) {
            return type;
        }
    }

    public static class Udf1 {
        public static Integer eval(Integer a, Integer b, Integer c) {
            if (a == null || b == null || c == null) {
                return 0;
            }
            return a + b + c;
        }
    }

    public static class Udf2 {
        public static Integer eval(Integer a, Integer b, Integer c) {
            if (a == null || b == null || c == null) {
                return 0;
            }
            return a + b - c;
        }
    }

    public static class Udf3 {
        public static Integer eval(Integer a, Integer b) {
            if (a == null || b == null) {
                return 0;
            }
            return a + b;
        }
    }

    public static class Udf4 {
//        public static Integer eval(Integer... a) {
//            if (a == null) {
//                return 0;
//            }
//            return Arrays.stream(a).reduce(0, Integer::sum);
//        }
        public static Integer eval(List<Integer> a) {
            if (a == null) {
                return 0;
            }
            return a.stream().reduce(0, Integer::sum);
        }
    }

    static class TestTable extends AbstractTable {
        @Override
        public RelDataType getRowType(RelDataTypeFactory typeFactory) {
            RelDataType mapType =
                    typeFactory.createMapType(
                            typeFactory.createSqlType(SqlTypeName.VARCHAR, 100),
                            typeFactory.createSqlType(SqlTypeName.INTEGER));
            RelDataType structType =
                    typeFactory
                            .builder()
                            .add("m", mapType)
                            .add(
                                    "arr",
                                    typeFactory.createArrayType(typeFactory.createSqlType(SqlTypeName.INTEGER), -1L))
                            .add("i", typeFactory.createSqlType(SqlTypeName.INTEGER))
                            .build();
            return typeFactory
                    .builder()
                    .add("a", typeFactory.createSqlType(SqlTypeName.INTEGER))
                    .add("b", typeFactory.createSqlType(SqlTypeName.INTEGER))
                    .add("c", typeFactory.createSqlType(SqlTypeName.INTEGER))
                    .add("d", structType)
                    .build();
        }
    }
}
