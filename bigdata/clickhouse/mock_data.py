# pip install clickhouse-driver
# https://hub.docker.com/r/yandex/clickhouse-server/
# $ docker run -d --name clickhouse -p 8123:8123 -p 9000:9000 --ulimit nofile=262144:262144 yandex/clickhouse-server


import datetime
import random

from clickhouse_driver import Client

client = Client(host='localhost', database='default')


def mock_data(num=1001):
    client.execute('''
        CREATE TABLE IF NOT EXISTS person (
            id UInt32,
            age UInt8,
            name String,
            value Float32,
            created_date Date
        ) ENGINE = MergeTree()
        PARTITION BY toYYYYMM(created_date)
        ORDER BY id
        SETTINGS index_granularity=100, index_granularity_bytes = 0;
    ''')

    # 生成数据
    data = []
    for i in range(1, num):
        id = i
        name = f'name_{random.randint(1, 10000)}'
        age = random.randint(20, 60)
        value = round(random.uniform(1.0, 100.0), 2)
        created_date = datetime.date(2023, 2, random.randint(1, 28))
        # created_date = datetime.date(2023, random.randint(1, 12), random.randint(1, 28))
        data.append((id, name, age, value, created_date))

    client.execute('INSERT INTO person (id, name, age, value, created_date) VALUES', data)
    print("数据插入完成")


def add_second_idx():
    # 添加二级索引
    client.execute('ALTER TABLE person ADD INDEX age_index age TYPE minmax GRANULARITY 3')
    # 强制进行表优化
    client.execute('OPTIMIZE TABLE person FINAL')


# mock_data()
# mock_data(501)
# mock_data(100001)
# add_second_idx()

count_result = client.execute('SELECT COUNT(*) FROM person')
print(count_result)

select_result = client.execute('select * from person limit 10')
for row in select_result:
    print(row)
