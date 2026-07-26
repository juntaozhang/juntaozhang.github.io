# MySQL CDC Example

A complete Maven project demonstrating MySQL Change Data Capture (CDC) using mysql-binlog-connector-java library.

## What is CDC?

Change Data Capture (CDC) is a technique to track and capture changes in database tables in real-time. 
This example monitors MySQL binlog events for INSERT, UPDATE, and DELETE operations.

## Features

- Real-time monitoring of MySQL data changes
- Event-driven architecture support
- Configuration-based setup
- Comprehensive logging

## Prerequisites

- Java 17+
- Maven 3.6+
- MySQL 8.3.0 with binlog enabled

## Quick Start

1. Configure MySQL binlog:
```sql
-- Enable binlog in my.cnf
-- [mysqld]
-- log-bin=mysql-bin
-- binlog-format=ROW
-- server-id=1

-- Create CDC user
CREATE USER 'root'@'%' IDENTIFIED BY 'root123';
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'root'@'%';
```

2. Update `application.properties`:
```properties
mysql.host=localhost
mysql.port=3307
mysql.username=root
mysql.password=root123
```

3. Run the application:
```bash
mvn clean compile
mvn exec:java
```