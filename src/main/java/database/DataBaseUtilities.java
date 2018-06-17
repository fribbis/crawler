package database;

import http.DateTimeConverter;
import http.HttpPageHandler;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataBaseUtilities {

    BasicDataSource dataSource;
    HttpPageHandler httpPageHandler;
    final String updateRequest = "update pages set lastScanDate = current_timestamp() where id = ?;";
    final String insertRequest = "insert ignore into pages (url, siteID, foundDateTime) values (?, ?, ?);";

    public DataBaseUtilities() {
        dataSource = DataSource.getDataSource();
        httpPageHandler = new HttpPageHandler();
    }

    public void addRobotsTxt() {
        Set<Integer> siteIds = getSiteIdWithOneRowInPages();
        final String selectRequest = "select ID, URL from pages where siteID = ? and lastScanDate is NULL;";
        //final String selectRequest = "select pages.ID, pages.siteID, pages.URL from sites, pages where sites.id = pages.siteID and pages.lastScanDate is NULL;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(selectRequest);
             PreparedStatement updateStatement = connection.prepareStatement(updateRequest);
             PreparedStatement insertStatement = connection.prepareStatement(insertRequest)) {
            ResultSet resultSet;
            for (Integer siteId : siteIds) {
                selectStatement.setInt(1, siteId);
                resultSet = selectStatement.executeQuery();
                while (resultSet.next()) {
                    String url = resultSet.getString("URL") + "/robots.txt";
                    int pageId = resultSet.getInt("ID");
                    //int siteId = resultSet.getInt("siteID");
                    if (httpPageHandler.getHttpUtilities().siteAvailable(url)) {
                        insertStatement.setString(1, url);
                        insertStatement.setInt(2, siteId);
                        insertStatement.setString(3, DateTimeConverter.convertDateToString(new Date()));
                        insertStatement.addBatch();
                    }
                    updateStatement.setInt(1, pageId);
                    updateStatement.addBatch();
                }
            }
            updateStatement.executeBatch();
            insertStatement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Set<Integer> getSiteIdWithOneRowInPages() {
        final String countRequest = "select sites.ID, count(*) from sites, pages where pages.siteID = sites.ID group by sites.ID having count(*) = 1;";
        Set<Integer> siteIDs = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(countRequest)) {
            while (resultSet.next()) {
                siteIDs.add(resultSet.getInt("ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return siteIDs;
    }

    public void addRootSitemaps() {
        final String selectRequest = "select ID, siteID, URL from pages where URL like \"%robots.txt\" and lastScanDate is NULL";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             PreparedStatement updateStatement = connection.prepareStatement(updateRequest);
             PreparedStatement insertStatement = connection.prepareStatement(insertRequest);
             ResultSet resultSet = statement.executeQuery(selectRequest)) {
            while (resultSet.next()) {
                String url = resultSet.getString("URL");
                int pageId = resultSet.getInt("ID");
                int siteId = resultSet.getInt("siteID");
                Set<String> rootSitemaps = httpPageHandler.getRootSitemaps(url);
                for (String sitemap : rootSitemaps) {
                    insertStatement.setString(1, sitemap);
                    insertStatement.setInt(2, siteId);
                    insertStatement.setString(3, DateTimeConverter.convertDateToString(new Date()));
                    insertStatement.addBatch();
                }
                updateStatement.setInt(1, pageId);
                updateStatement.addBatch();
            }
            updateStatement.executeBatch();
            insertStatement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addSitemaps() {
        final String selectRequest = "select ID, siteID, URL from pages where URL like \"%sitemap%\" and lastScanDate is NULL;";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             PreparedStatement updateStatement = connection.prepareStatement(updateRequest);
             PreparedStatement insertStatement = connection.prepareStatement(insertRequest)) {
            ResultSet resultSet;
            int count = 0;
            //int countUpdate = 0;
            do {
                resultSet = statement.executeQuery(selectRequest);
                while (resultSet.next()) {
                    String url = resultSet.getString("URL");
                    int pageId = resultSet.getInt("ID");
                    int siteId = resultSet.getInt("siteID");
                    Map<String, Date> ulrs = httpPageHandler.wrapper(url);
                    for (Map.Entry<String, Date> urlDate : ulrs.entrySet()) {
                        insertStatement.setString(1, urlDate.getKey());
                        insertStatement.setInt(2, siteId);
                        insertStatement.setString(3, DateTimeConverter.convertDateToString(urlDate.getValue()));
                        insertStatement.addBatch();
                        count++;
                        if (count == 1000) {
                            insertStatement.executeBatch();
                            insertStatement.clearBatch();
                            count = 0;
                        }
                    }
                    if (count > 0) insertStatement.executeBatch();
                    updateStatement.setInt(1, pageId);
                    updateStatement.execute();
                }
            } while (resultSet.isAfterLast());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
