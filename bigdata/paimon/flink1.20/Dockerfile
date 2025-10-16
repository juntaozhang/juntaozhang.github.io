FROM flink:1.20.1-scala_2.12
COPY ./paimon-flink-*.jar /opt/flink/lib/
COPY ./paimon-flink-action-*.jar /opt/flink/lib/
COPY ./flink-shaded-hadoop-2-uber-2.8.3-10.0.jar /opt/flink/lib/
COPY ./flink-sql-connector-*-cdc-*.jar /opt/flink/lib/
COPY ./mysql-connector-*.jar /opt/flink/lib/
COPY ./flink-conf.yaml /opt/flink/conf/
RUN  mkdir /opt/flink/plugins/s3-fs-hadoop
COPY ./flink-s3-fs-hadoop-*.jar /opt/flink/plugins/s3-fs-hadoop/
