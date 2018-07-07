package database;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public interface PageDAO {
    LinkedList<Page> findByUrlAndLastScanDateTime(String url, String lastScanDateTime);
    LinkedList<Page> findByNotMatchUrlsAndMatchLastScanDateTime(String url1, String url2, String lastScanDateTime, int skip);
    LinkedList<Page> findBySiteId(int siteId);
    int countBySiteId(int siteId);
    void updateLastScanDateTime(int pageId);
    void updateLastScanDateTime(List<Integer> pagesId);
    void insert(Page page) throws SQLException;
    void insertNew(Page page) throws SQLException;
    void insert(List<Page> pages) throws SQLException;
    void insertNew(List<Page> pages) throws SQLException;
}
