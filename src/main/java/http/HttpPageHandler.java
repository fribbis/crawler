package http;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

public class HttpPageHandler {

    private HttpUtilities httpUtilities;

    public HttpPageHandler() {
        httpUtilities = new HttpUtilities();
    }

    public HttpUtilities getHttpUtilities() {
        return httpUtilities;
    }

    public Set<String> getRootSitemaps(String url) {
        StringBuilder page;
        page = httpUtilities.getPage(url);
        Set<String> sitemap = new HashSet<>();
        String[] strings = page.toString().split("\n");
        for (String string : strings) {
            if (string.toLowerCase().trim().startsWith("sitemap")) {
                sitemap.add(string.substring(string.indexOf("http")).trim());
            }
        }
        return sitemap;
    }

    public Map<String, Date> wrapper(String url) {
        StringBuilder page = httpUtilities.getPage(url);
        Map<String, Date> mapOfUrls = new HashMap<>();
        if (page.length() != 0) {
            mapOfUrls = getSitemaps(page);
            if (mapOfUrls.isEmpty())
                mapOfUrls = getUrls(page);
        }
        return mapOfUrls;
    }

    private Map<String, Date> getSitemaps(StringBuilder page) {
        Map<String, Date> mapOfSitemaps = new HashMap<>();
        Document doc = Jsoup.parse(page.toString());
        Elements elementsOfSitemapindexes = doc.getElementsByTag("sitemapindex");
        Elements elementsOfSitemaps;
        if (!elementsOfSitemapindexes.isEmpty()) {
            for (Element sitemapindex : elementsOfSitemapindexes) {
                elementsOfSitemaps = sitemapindex.getElementsByTag("sitemap");
                for (Element sitemap : elementsOfSitemaps) {
                    String loc = sitemap.getElementsByTag("loc").first().text();
                    String date = "";
                    Elements lastmod = sitemap.getElementsByTag("lastmod");
                    if (!lastmod.isEmpty()) date = sitemap.getElementsByTag("lastmod").first().text();
                    mapOfSitemaps.put(loc, DateTimeConverter.convertStringToDate(date));
                }
            }
        }
        return mapOfSitemaps;
    }

    private Map<String, Date> getUrls(StringBuilder page) {
        Map<String, Date> mapOfUrls = new HashMap<>();
        Document doc = Jsoup.parse(page.toString());
        Elements elementsOfUrlset = doc.getElementsByTag("urlset");
        Elements elementsOfUrl;
        if (!elementsOfUrlset.isEmpty()) {
            for (Element elementOfUrl : elementsOfUrlset) {
                elementsOfUrl = elementOfUrl.getElementsByTag("url");
                for (Element sitemap : elementsOfUrl) {
                    String loc = sitemap.getElementsByTag("loc").first().text();
                    String date = "";
                    Elements lastmod = sitemap.getElementsByTag("lastmod");
                    if (!lastmod.isEmpty()) date = sitemap.getElementsByTag("lastmod").first().text();
                    mapOfUrls.put(loc, DateTimeConverter.convertStringToDate(date));
                }
            }
        }
        return mapOfUrls;
    }
}
