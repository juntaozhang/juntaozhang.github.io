
sqlContext.sql("SELECT name, age FROM people WHERE age >= 13 AND age <= 19")

SparkSQLParser.parse