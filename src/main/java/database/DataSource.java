package database;

import org.apache.commons.dbcp2.BasicDataSource;

public class DataSource {
    public final static String URL = "jdbc:mariadb://localhost/searchandratewords";
    public final static String USER = "root";
    public final static String PASSWORD = null;

    private static BasicDataSource dataSource;

    public static BasicDataSource getDataSource() {

        if (dataSource == null) {
            dataSource = new BasicDataSource();
            dataSource.setUrl(URL);
            dataSource.setUsername(USER);
            dataSource.setPassword(PASSWORD);
            dataSource.setMinIdle(5);
            dataSource.setMaxIdle(10);
        }
        return dataSource;
    }
}
