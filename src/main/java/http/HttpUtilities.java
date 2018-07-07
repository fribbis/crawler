package http;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class HttpUtilities {
    private HttpClient client;

    public HttpUtilities() {
        client = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();
    }

    public boolean siteAvailable(String url) {
        boolean result = false;
        HttpHead headRequest = new HttpHead(url);
        try {
            result = client.execute(headRequest).getStatusLine().getStatusCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!result) System.out.printf("Site %s is unavailable\n", url);
        return result;
    }

    boolean siteAvailable2(String url) {
        boolean result = false;
        try {
            result = Jsoup.connect(url).method(Connection.Method.HEAD).execute().statusCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!result) System.out.printf("Site %s is unavailable\n", url);
        return result;
    }

    String getTextOfPage(String url) {
        String page = "";
        if (siteAvailable(url)) {
            try {
                Document document = Jsoup.connect(url).get();
                page = document.body().text();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.printf("Page %s is unavailable\n", url);
            }
        }
        return page;
    }

    public StringBuilder getPage(String url) {
        StringBuilder page = new StringBuilder();
        if (siteAvailable(url)) {
            try {
                if (url.endsWith("xml.gz")) {
                    GZIPInputStream gis = new GZIPInputStream(new URL(url).openStream());
                    byte[] buffer = new byte[1024];
                    int count = 0;
                    while ((count = gis.read(buffer, 0, 1024)) != -1) {
                        page.append(new String(buffer), 0, count);
                    }
                } else {
//                    HttpGet request = new HttpGet(url);
//                    HttpResponse response = client.execute(request);
//                    page.append(EntityUtils.toString(response.getEntity()));
                    BufferedInputStream bis = new BufferedInputStream(new URL(url).openStream());
                    //byte[] buffer = new byte[1024];
                    int count = 0;
                    while (count != -1) {
                        byte[] buffer = new byte[1024];
                        count = bis.read(buffer, 0, 1024);
                        page.append(new String(buffer).trim());
                    }
                }
            } catch (IOException e) {
                System.out.println("Error " + url);
                e.printStackTrace();
            }
        }
        return page;
    }
}
