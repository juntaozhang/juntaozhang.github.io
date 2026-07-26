from clickhouse_driver import Client

client = Client(host='localhost', database='default')


# client.execute('''
#     CREATE TABLE agg_table_basic(
#         id String,
#         city String,
#         code String,
#         value UInt32
#     ) ENGINE = MergeTree()
#     PARTITION BY city
#     ORDER BY (id, city)
#     SETTINGS index_granularity=3, index_granularity_bytes=0;
# ''')
#
# client.execute('''
#     CREATE MATERIALIZED VIEW agg_view
#     ENGINE = AggregatingMergeTree()
#     PARTITION BY city
#     ORDER BY (id, city)
#     AS
#     SELECT
#         id,
#         city,
#         uniqState(code) AS code,
#         sumState(value) AS value
#     FROM agg_table_basic
#     GROUP BY id, city
# ''')
#
# client.execute('''
#     INSERT INTO TABLE agg_table_basic
#     VALUES
#         ('A000','wuhan','code1',100),
#         ('A000','wuhan','code2',200),
#         ('A000','zhuhai', 'code1',200)
# ''')

def mock_data():
    import random
    # Step 1: Prepare the data
    cities = ['wuhan', 'zhuhai', 'shanghai', 'beijing', 'guangzhou']
    codes = ['code1', 'code2', 'code3', 'code4', 'code5']
    data_to_insert = []

    for i in range(1, 1000001):
        id = f'A{i:04d}'
        city = random.choice(cities)
        code = random.choice(codes)
        value = random.randint(100, 500)
        data_to_insert.append((id, city, code, value))

    # Step 2: Execute batch insert
    client.execute('INSERT INTO agg_table_basic (id, city, code, value) VALUES', data_to_insert)
    print("10,000 rows of data inserted successfully into agg_table_basic.")

mock_data()

# select_result = client.execute(
#     'SELECT * FROM agg_table_basic')
# for row in select_result:
#     print(row)
# print("=========================")
#
# select_result = client.execute(
#     'SELECT id, city, sumMerge(value), uniqMerge(code) FROM agg_view GROUP BY id, city')
# for row in select_result:
#     print(row)
#
# print("=========================")
# select_result = client.execute(
#     'SELECT id, city, uniq(code), sum(value) AS value FROM agg_table_basic GROUP BY id, city')
# for row in select_result:
#     print(row)


