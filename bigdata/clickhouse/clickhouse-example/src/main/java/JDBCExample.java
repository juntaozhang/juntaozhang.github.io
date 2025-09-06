import ru.yandex.clickhouse.ClickHouseConnection;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.ClickHouseStatement;

import java.sql.ResultSet;
import java.util.Properties;

public class JDBCExample {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.setProperty("password", "");
        ClickHouseDataSource dataSource = new ClickHouseDataSource("jdbc:clickhouse://localhost:8123", props);
        ClickHouseConnection connection = dataSource.getConnection();
        ClickHouseStatement sth = connection.createStatement();
        ResultSet rs = sth.executeQuery("select version()");
        rs.next();
        System.out.println(rs.getString(1));


        rs = sth.executeQuery("select count(1) from person");
        rs.next();
        System.out.println(rs.getString(1));
        rs.close();
    }
}
