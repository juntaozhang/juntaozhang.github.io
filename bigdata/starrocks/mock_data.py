import random

import mysql.connector

conn = mysql.connector.connect(
    host="localhost",
    user="root",
    password="",
    database="",
    port=9030
)
cursor = conn.cursor()


def create_table():
    cursor.execute("CREATE DATABASE IF NOT EXISTS company")
    cursor.execute("USE company")
    cursor.execute("""
        ADMIN SET FRONTEND CONFIG ("expr_children_limit"="100000")
    """)
    cursor.execute("""
        CREATE TABLE employees (
            id INT,
            name VARCHAR(50),
            salary DECIMAL(10, 2)
        )
        PRIMARY KEY (id)
        DISTRIBUTED BY HASH (id)
        BUCKETS 2
        PROPERTIES ("replication_num"="1")
    """)
    cursor.execute("""
        CREATE TABLE employees2 (
            id INT,
            name VARCHAR(50),
            salary DECIMAL(10, 2),
            INDEX employees2_name_idx (`name`) USING GIN("parser" = "english") COMMENT '' -- 全文倒排索引
        )
        DUPLICATE KEY (id)
        DISTRIBUTED BY HASH (id)
        BUCKETS 2
        PROPERTIES (
            "replicated_storage" = "false",
            "replication_num"="1"
        )
    """)


def insert_data(size, start=1):
    cursor.execute("USE company")

    first_names = ["John", "Jane", "Robert", "Emily", "Michael", "Mary", "William", "Jessica",
                   "Harry", "Linda"]
    last_names = ["Doe", "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
                  "Davis", "Wilson"]

    def generate_employee_data(num_records):
        employees = []
        for i in range(start, num_records + start):
            first_name = random.choice(first_names)
            last_name = random.choice(last_names)
            salary = random.randint(4000, 8000)
            employees.append((i, f'{first_name} {last_name}', salary))
        return employees

    sql = "INSERT INTO employees2 (id, name, salary) VALUES (%s, %s, %s)"
    values = generate_employee_data(size)
    cursor.executemany(sql, values)
    conn.commit()
    print(cursor.rowcount, "records inserted.")


def update():
    salary = random.randint(4000, 8000)
    cursor.execute("UPDATE employees SET salary=%s WHERE id = 1", (salary,))
    conn.commit()


def delete():
    cursor.execute("DELETE from employees WHERE id = 2")
    conn.commit()

# ADMIN SET FRONTEND CONFIG ("catalog_trash_expire_second" = "10");
# ADMIN SHOW FRONTEND CONFIG LIKE "default_replication_num";
# ALTER TABLE employees ADD INDEX employees_name_idx(name) USING GIN('parser' = 'english');

# ALTER TABLE employees SET ("replicated_storage" = "false");
# ALTER TABLE employees ADD INDEX employees_name_idx(name) USING GIN('parser' = 'english');
# CREATE INDEX employees_name_idx ON employees (name) USING GIN('parser' = 'english');

# create_table()
insert_data(100000, 0)
# update()
# delete()

cursor.close()
conn.close()
