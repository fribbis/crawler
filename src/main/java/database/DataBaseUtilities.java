package database;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import http.DateTimeConverter;
import http.HttpPageHandler;
import http.PageParser;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bson.Document;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataBaseUtilities {

    private BasicDataSource dataSource;
    private HttpPageHandler httpPageHandler;
    private final String updateRequest = "update pages set lastScanDate = current_timestamp() where id = ?;";
    private final String insertRequest = "insert ignore into pages (url, siteID, foundDateTime) values (?, ?, ?);";
    private int count = 0;

    public DataBaseUtilities() {
        dataSource = DataSource.getDataSource();
        httpPageHandler = new HttpPageHandler();
    }

    public void addRobotsTxt() {
        Set<Integer> siteIds = getSiteIdWithOneRowInPages();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(updateRequest);
             PreparedStatement insertStatement = connection.prepareStatement(insertRequest)) {
            for (Integer siteId : siteIds) {
                try (ResultSet resultSet = getIdUrlFromPagesBySideId(connection, siteId)) {
                    while (resultSet.next()) {
                        String url = resultSet.getString("URL") + "/robots.txt";
                        int pageId = resultSet.getInt("ID");
                        if (httpPageHandler.getHttpUtilities().siteAvailable(url)) {
                            insertRowToPages(insertStatement, siteId, url, new Date());
                        }
                        updateLastScanDateFromPages(updateStatement, pageId);
                    }
                }
            }
            updateStatement.executeBatch();
            insertStatement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ResultSet getIdUrlFromPagesBySideId(Connection connection, int siteId) throws SQLException {
        final String selectRequest = "select ID, URL from pages where siteID = ? and lastScanDate is NULL;";
        PreparedStatement selectStatement = connection.prepareStatement(selectRequest);
        selectStatement.setInt(1, siteId);
        return selectStatement.executeQuery();
    }

    private void insertRowToPages(PreparedStatement preparedStatement, int siteId, String url, Date date) throws SQLException {
        preparedStatement.setString(1, url);
        preparedStatement.setInt(2, siteId);
        preparedStatement.setString(3, DateTimeConverter.convertDateToString(date));
        preparedStatement.addBatch();
    }

    private void updateLastScanDateFromPages(PreparedStatement preparedStatement, int pageId) throws SQLException {
        preparedStatement.setInt(1, pageId);
        preparedStatement.addBatch();
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
                    insertRowToPages(insertStatement, siteId, sitemap, new Date());
                }
                updateLastScanDateFromPages(updateStatement, pageId);
            }
            updateStatement.executeBatch();
            insertStatement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addSitemapsAndArticles() {
        final String selectRequest = "select ID, siteID, URL from pages where URL like \"%sitemap%\" and lastScanDate is NULL;";
        try (Connection connection1 = dataSource.getConnection();
             Statement statement = connection1.createStatement()) {
            count = 0;
            ResultSet resultSet;
            ExecutorService service = Executors.newFixedThreadPool(10);
            do {
                resultSet = statement.executeQuery(selectRequest);
                resultSet.last();
                CountDownLatch latch = new CountDownLatch(resultSet.getRow());
                resultSet.beforeFirst();
                while (resultSet.next()) {
                    String url = resultSet.getString("URL");
                    int pageId = resultSet.getInt("ID");
                    int siteId = resultSet.getInt("siteID");
                    service.submit(() -> {
                        System.out.println(Thread.currentThread().getName());
                        try (Connection connection2 = dataSource.getConnection();
                             PreparedStatement updateStatement = connection2.prepareStatement(updateRequest);
                             PreparedStatement insertStatement = connection2.prepareStatement(insertRequest)) {
                            Map<String, Date> ulrs = httpPageHandler.wrapper(url);
                            for (Map.Entry<String, Date> urlDate : ulrs.entrySet()) {
                                insertRowToPages(insertStatement, siteId, urlDate.getKey(), urlDate.getValue());
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
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            latch.countDown();
                        }
                    });
                }
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (resultSet.isAfterLast());
            service.shutdown();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    ResultSet getIdAndUrlFromPages(Connection connection, int start, int count) throws SQLException {
        final String selectRequest = "select v1.id, v1.url from pages as v1 inner join (select * from pages " +
                "where url not like \"%sitemap%\" and url not like \"%robots.txt%\" LIMIT ?, ?) as v2 on " +
                "v1.id = v2.id and v1.lastScanDate is NULL;";
        PreparedStatement selectStatement = connection.prepareStatement(selectRequest);
        selectStatement.setInt(1, start);
        selectStatement.setInt(2, count);
        return selectStatement.executeQuery();
    }

    public void fillMongoDb() {
        int countOfPage = getCountOfPages();
        MongoClient mongoClient = new MongoClient("localhost");
        MongoDatabase database = mongoClient.getDatabase("urlwordsrate");
        MongoCollection<Document> collection = database.getCollection("ratingTest");
        ExecutorService service = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(countOfPage / 10000);
        PageParser pageParser = new PageParser();
        for (count = 0; count <= countOfPage; count += 10000) {
            final int ttt = count;
            service.submit(() -> {
                System.out.println(Thread.currentThread().getName());
                System.out.println(Thread.activeCount());
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement updateStatement = connection.prepareStatement(updateRequest);
                     ResultSet resultSet = getIdAndUrlFromPages(connection, ttt, 10000)) {
                    while (resultSet.next()) {
                        int pageId = resultSet.getInt("ID");
                        String url = resultSet.getString("URL");
                        try {
                            Map<String, Integer> map = pageParser.countWords(url);
                            //System.out.printf("%d - %s - %b\n", pageId, url, !map.isEmpty());
                            Document doc = new Document("_id", pageId).append("words", new Document());
                            map.forEach((key, value) -> doc.get("words", new Document()).append(key, value));
                            try {
                                collection.insertOne(doc);
                                System.out.printf("%d %s - added\n", pageId, url);
                            } catch (MongoException e) {
                                System.out.printf("%d %s - mongodb exception\n", pageId, url);
                                //e.printStackTrace();
                            }
                            updateLastScanDateFromPages(updateStatement, pageId);
                        } catch (IOException e) {
                            System.out.printf("%d %s is unavailable\n", pageId, url);
                        }

                    }
                    updateStatement.executeBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.shutdown();
    }

    public int getCountOfPages() {
        final String selectRequest = "select count(*) from pages where url not like \"%sitemap%\" and url not like \"%robots.txt%\";";
        int count = 0;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectRequest)) {
            while (resultSet.next())
                count = resultSet.getInt("count(*)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
}