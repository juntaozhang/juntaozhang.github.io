# [SPARK-54725: Add inferring transitive join conditions in CostBasedJoinReorder](https://issues.apache.org/jira/browse/SPARK-54725)
TPCDSTableStats：

| 表名          | 表大小    | 行数          | JOIN KEY        | distinctCount |
|-------------|--------|-------------|-----------------|---------------|
| store_sales | 42.6GB | 288,040,399 | ss_sold_date_sk | 1,781         |
|             |        |             | ss_store_sk     | 199           |
|             |        |             | ss_item_sk      | 206,807       |
| item        | 94MB   | 204,000     | i_item_sk       | 204,000       |
| date_dim    | 19.3MB | 73,049      | d_date_sk       | 73,049        |
| store       | 207KB  | 402         | s_store_sk      | 399           |


sql/core/src/test/resources/tpcds-modifiedQueries/q65.sql
```
(store_sales JOIN date_dim) sc[key(ss_store_sk, ss_item_sk)]
JOIN (store_sales JOIN date_dim) sb[key(ss_store_sk)]
JOIN store 
JOIN item

after optimise, the plan is ==>

((store_sales JOIN date_dim) sb[key(ss_store_sk)] JOIN store)
JOIN ((store_sales JOIN date_dim) sc[key(ss_store_sk, ss_item_sk)] JOIN item)
```

sql/core/src/test/resources/tpcds-v2.7.0/q24.sql

| 表名               | 表大小    | 行数          | JOIN KEY          | distinctCount |
|------------------|--------|-------------|-------------------|---------------|
| store_sales      | 42.6GB | 287,997,024 | ss_item_sk        | 206,807       |
|                  |        |             | ss_customer_sk    | 1,903,054     |
|                  |        |             | ss_store_sk       | 199           |
|                  |        |             | ss_ticket_number  | 24,596,280    |
| store_returns    | 4.8GB  | 28,795,080  | sr_item_sk        | 197,284       |
|                  |        |             | sr_ticket_number  | 15,853,105    |
| customer         | 500MB  | 2,000,000   | c_customer_sk     | 1,903,054     |
|                  |        |             | c_current_addr_sk | 824,389       |
| customer_address | 245MB  | 1,000,000   | ca_address_sk     | 943,039       |
| item             | 94MB   | 204,000     | i_item_sk         | 204,000       |
| store            | 207KB  | 402         | s_store_sk        | 399           |

```text
(store JOIN customer_address JOIN customer) 
JOIN store_sales
JOIN item
JOIN store_returns
==>
(store JOIN customer_address JOIN customer)
JOIN (store_sales FILTER BY item bloom_filter)
JOIN (store_returns JOIN item)
```