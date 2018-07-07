package database;

import http.DateTimeConverter;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class PageDAOImp implements PageDAO {

    private BasicDataSource dataSource;

    public PageDAOImp() {
        dataSource = DataSource.getDataSource();
    }

    @Override
    public int countBySiteId(int siteId) {
        final String request = String.format("select count(*) from pages where siteID = %d;", siteId);
        int count = 0;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(request)) {
            resultSet.next();
            count = resultSet.getInt("count(*)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public LinkedList<Page> findBySiteId(int siteId) {
        LinkedList<Page> pages = new LinkedList<>();
        final String request = String.format("select * from pages where siteID = %d;", siteId);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(request)) {
            while(resultSet.next()) {
                Page page = new Page();
                page.setPageId(resultSet.getInt("id"));
                page.setUrl(resultSet.getString("url"));
                page.setSiteId(resultSet.getInt("siteId"));
                try {
                    page.setFoundDateTime(DateTimeConverter.convertStringToDate(resultSet.getString("foundDateTime")));
                    page.setLastScanDateTime(DateTimeConverter.convertStringToDate(resultSet.getString("lastScanDate")));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                pages.add(page);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pages;
    }

    @Override
    public LinkedList<Page> findByUrlAndLastScanDateTime(String url, String lastScanDateTime) {
        LinkedList<Page> pages = new LinkedList<>();
        final String request = String.format("select * from pages where URL like \"%s\" " +
//                "and lastScanDate is %s limit 1000;", url, lastScanDateTime);
                "and (lastScanDate < %s or lastScanDate is null) limit 1000;", url, lastScanDateTime);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(request)) {
            while (resultSet.next()) {
                Page page = new Page();
                page.setPageId(resultSet.getInt("id"));
                page.setUrl(resultSet.getString("url"));
                page.setSiteId(resultSet.getInt("siteId"));
                try {
                    page.setFoundDateTime(DateTimeConverter.convertStringToDate(resultSet.getString("foundDateTime")));
                    //page.setLastScanDateTime(DateTimeConverter.convertStringToDate(resultSet.getString("lastScanDate")));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                pages.add(page);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pages;
    }

    public LinkedList<Page> findByNotMatchUrlsAndMatchLastScanDateTime(String url1, String url2, String lastScanDateTime, int skip) {
        LinkedList<Page> pages = new LinkedList<>();
        final String request = String.format("select * from pages where url not like \"%s\" and " +
                "url not like \"%s\" and lastScanDate is %s limit %d, 1000;", url1, url2, lastScanDateTime, skip);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(request)) {
            while (resultSet.next()) {
                Page page = new Page();
                page.setPageId(resultSet.getInt("id"));
                page.setUrl(resultSet.getString("url"));
                page.setSiteId(resultSet.getInt("siteId"));
                try {
                    page.setFoundDateTime(DateTimeConverter.convertStringToDate(resultSet.getString("foundDateTime")));
                    //page.setLastScanDateTime(DateTimeConverter.convertStringToDate(resultSet.getString("lastScanDate")));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                pages.add(page);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pages;
    }

    @Override
    public void updateLastScanDateTime(int pageId) {
        final String request = "update pages set lastScanDate = current_timestamp() where id = ?;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(request)) {
            preparedStatement.setInt(1, pageId);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateLastScanDateTime(List<Integer> pagesId) {
        final String request = "update pages set lastScanDate = current_timestamp() where id = ?;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(request)) {
            for (Integer pageId : pagesId) {
                preparedStatement.setInt(1, pageId);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insert(Page page) throws SQLException {
        final String request = "insert ignore into pages (url, siteID, foundDateTime) values (?, ?, ?);";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(request)) {
            preparedStatement.setString(1, page.getUrl());
            preparedStatement.setInt(2, page.getSiteId());
            preparedStatement.setString(3, DateTimeConverter.convertDateToString(page.getFoundDateTime()));
            preparedStatement.execute();
        }
    }

    @Override
    public void insertNew(Page page) throws SQLException {
        final String request = "insert into pages (URL, siteID, foundDateTime) values (?, ?, ?) " +
                "on duplicate key update " +
                "lastScanDate = case when foundDateTime < STR_TO_DATE(?, \"%Y-%m-%d %H:%i:%s\")" +
                "then null else current_timestamp() end, " +
                "foundDateTime = case when foundDateTime < STR_TO_DATE(?, \"%Y-%m-%d %H:%i:%s\") " +
                "then ? else foundDateTime end;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(request)) {
            String date = DateTimeConverter.convertDateToString(page.getFoundDateTime());
            preparedStatement.setString(1, page.getUrl());
            preparedStatement.setInt(2, page.getSiteId());
            preparedStatement.setString(3, date);
            preparedStatement.setString(4, date);
            preparedStatement.setString(5, date);
            preparedStatement.setString(6, date);
            preparedStatement.execute();
        }
    }

    @Override
    public void insert(List<Page> pages) throws SQLException {
        final String request = "insert ignore into pages (url, siteID, foundDateTime) values (?, ?, ?);";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(request)) {
            for (Page page : pages) {
                preparedStatement.setString(1, page.getUrl());
                preparedStatement.setInt(2, page.getSiteId());
                preparedStatement.setString(3, DateTimeConverter.convertDateToString(page.getFoundDateTime()));
                preparedStatement.addBatch();
                System.out.printf("Page %s is added\n", page.getUrl());
            }
            preparedStatement.executeBatch();
        }
    }

    @Override
    public void insertNew(List<Page> pages) throws SQLException {
        final String request = "insert into pages (URL, siteID, foundDateTime) values (?, ?, ?) " +
                "on duplicate key update " +
                "lastScanDate = case when foundDateTime < STR_TO_DATE(?, \"%Y-%m-%d %H:%i:%s\")" +
                "then null else current_timestamp() end, " +
                "foundDateTime = case when foundDateTime < STR_TO_DATE(?, \"%Y-%m-%d %H:%i:%s\") " +
                "then ? else foundDateTime end;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(request)) {
            for (Page page : pages) {
                String date = DateTimeConverter.convertDateToString(page.getFoundDateTime());
                preparedStatement.setString(1, page.getUrl());
                preparedStatement.setInt(2, page.getSiteId());
                preparedStatement.setString(3, date);
                preparedStatement.setString(4, date);
                preparedStatement.setString(5, date);
                preparedStatement.setString(6, date);
                preparedStatement.addBatch();
                System.out.printf("Page %s is added\n", page.getUrl());
            }
            preparedStatement.setQueryTimeout(30);
            preparedStatement.executeBatch();
        }
    }
}
