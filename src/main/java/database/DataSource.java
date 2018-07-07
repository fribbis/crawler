package database;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DataSource {

    private static BasicDataSource dataSource;

    public static BasicDataSource getDataSource() {
        Properties properties = new Properties();
        try (FileInputStream config = new FileInputStream("config.properties")) {
            properties.load(config);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String URL = properties.getProperty("mariadb.url");
        String USER = properties.getProperty("mariadb.user");
        String PASSWORD = properties.getProperty("mariadb.password");

        if (dataSource == null) {
            dataSource = new BasicDataSource();
            dataSource.setUrl(URL);
            dataSource.setUsername(USER);
            dataSource.setPassword(PASSWORD);
            dataSource.setMaxTotal(10);
            dataSource.setMinIdle(5);
            dataSource.setMaxIdle(10);
        }
        return dataSource;
    }
}
