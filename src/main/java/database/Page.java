package database;

import java.util.Date;

public class Page {
    private int pageId;
    private int siteId;
    private String url;
    private Date foundDateTime;
    private Date lastScanDateTime;

    Page() {}

    Page(int siteId, String url, Date foundDateTime) {
        this.siteId = siteId;
        this.url = url;
        this.foundDateTime = foundDateTime;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getFoundDateTime() {
        return foundDateTime;
    }

    public void setFoundDateTime(Date foundDateTime) {
        this.foundDateTime = foundDateTime;
    }

    public Date getLastScanDateTime() {
        return lastScanDateTime;
    }

    public void setLastScanDateTime(Date lastScanDateTime) {
        this.lastScanDateTime = lastScanDateTime;
    }

    @Override
    public String toString() {
        return String.format("%d %s - found: %s, scan: %s", getPageId(), getUrl(), getFoundDateTime(), getLastScanDateTime());
    }
}
