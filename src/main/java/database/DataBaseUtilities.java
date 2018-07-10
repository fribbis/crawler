package database;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import http.HttpPageHandler;
import http.PageParser;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;

public class DataBaseUtilities {


    private HttpPageHandler httpPageHandler;
    private AtomicInteger count;

    public DataBaseUtilities() {
        httpPageHandler = new HttpPageHandler();
    }

    public void addRobotsTxt() {
        LinkedList<Site> sites = new SiteDAOImp().findAll();
        PageDAO pageDAO = new PageDAOImp();
        ExecutorService service = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(sites.size());
        while (sites.size() != 0) {
            Site site = sites.pop();
            service.submit(() -> {

                try {
                    if (pageDAO.countBySiteId(site.getSiteId()) == 1) {
                        Page page = pageDAO.findBySiteId(site.getSiteId()).getFirst();
                        if (page.getLastScanDateTime() == null) {
                            String url = page.getUrl() + "/robots.txt";
                            if (httpPageHandler.getHttpUtilities().siteAvailable(url)) {
                                pageDAO.insertNew(new Page(site.getSiteId(), url, new Date()));
                                pageDAO.updateLastScanDateTime(page.getPageId());
                            }
                        }
                    }
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

    public void addRootSitemaps() {
        PageDAO pageDAO = new PageDAOImp();
        LinkedList<Page> pages = pageDAO.findByUrlAndLastScanDateTime("%robots.txt", "current_date()");
        ExecutorService service = Executors.newFixedThreadPool(10);
        while (pages.size() != 0) {
            CountDownLatch latch = new CountDownLatch(pages.size());
            while (pages.size() != 0) {
                Page page = pages.pop();
                service.submit(() -> {
                    System.out.println(Thread.currentThread().getName() + " - start");
                    Set<String> rootSitemaps = httpPageHandler.getRootSitemaps(page.getUrl());
                    try {
                        for (String sitemap : rootSitemaps) {
                            pageDAO.insert(new Page(page.getSiteId(), sitemap, new Date()));
                        }
                        pageDAO.updateLastScanDateTime(page.getPageId());
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
            if (pages.size() == 0) pages = pageDAO.findByUrlAndLastScanDateTime("%robots.txt", "current_date()");
        }
        service.shutdown();
    }

    public void addSitemapsAndArticles() {
        PageDAO pageDAO = new PageDAOImp();
        LinkedList<Page> pages = pageDAO.findByUrlAndLastScanDateTime("%sitemap%", "current_date()");
        ExecutorService service = Executors.newFixedThreadPool(100);
        while (pages.size() != 0) {
            CountDownLatch latch = new CountDownLatch(pages.size());
            while (pages.size() != 0) {
                Page page = pages.pop();
                service.submit(() -> {
                    System.out.println(Thread.currentThread().getName() + " - start");
                    Map<String, Date> urls = httpPageHandler.wrapper(page.getUrl());
                    LinkedList<Page> pages2 = new LinkedList<>();
                    for (Map.Entry<String, Date> urlDate : urls.entrySet()) {
                        pages2.add(new Page(page.getSiteId(), urlDate.getKey(), urlDate.getValue()));
                    }
                    try {
                        pageDAO.insertNew(pages2);
                        pageDAO.updateLastScanDateTime(page.getPageId());
                        System.out.println(Thread.currentThread().getName() + " - end");
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
            System.out.println(pages.size());
            if (pages.size() == 0) pages = pageDAO.findByUrlAndLastScanDateTime("%sitemap%", "current_date()");
        }
        service.shutdown();
    }

    public void handlePages() {
        MongoClient mongoClient = new MongoClient("localhost");
        MongoDatabase database = mongoClient.getDatabase("urlwordsrate");
        MongoCollection<Document> collection = database.getCollection("rating");
        PageParser pageParser = new PageParser();
        PageDAO pageDAO = new PageDAOImp();
        count = new AtomicInteger(0);
        LinkedList<Page> pages = pageDAO.findByNotMatchUrlsAndMatchLastScanDateTime("%sitemap%", "%robots.txt%", "null", count.get());
        ExecutorService service = Executors.newFixedThreadPool(100);
        while (pages.size() != 0) {
            CountDownLatch latch = new CountDownLatch(pages.size());
            while (pages.size() != 0) {
                Page page = pages.pop();
                service.submit(() -> {
                    try {
                        Map<String, Integer> map = pageParser.countWords(page.getUrl());
                        Document doc = new Document();
                        map.forEach((key, value) -> doc.append(key, value));
                        try {
                            collection.updateOne(eq("_id", page.getPageId()), set("words", doc), new UpdateOptions().upsert(true));
                            System.out.printf("%d %s is handled\n", page.getPageId(), page.getUrl());
                        } catch (MongoException e) {
                            System.out.printf("%d %s - mongodb exception\n", page.getPageId(), page.getUrl());
                        }
                        pageDAO.updateLastScanDateTime(page.getPageId());
                    } catch (IOException e) {
                        count.incrementAndGet();
                        System.out.printf("%d %s is unavailable\n", page.getPageId(), page.getUrl());
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
            if (pages.size() == 0)
                pages = pageDAO.findByNotMatchUrlsAndMatchLastScanDateTime("%sitemap%", "%robots.txt%", "null", count.get());
        }
        service.shutdown();
        mongoClient.close();
    }

    private LinkedList<PersonAndKeywords> getPersonsAndKeywords() {
        PersonDAOImp personDAOImp = new PersonDAOImp();
        KeywordDAOImp keywordDAOImp = new KeywordDAOImp();
        LinkedList<PersonAndKeywords> list = new LinkedList<>();
        for (Person person : personDAOImp.findAll()) {
            List<String> keywords = new ArrayList<>();
            keywords.add(person.getName());
            keywordDAOImp.findByPersonId(person.getPersonId()).forEach(keyword -> keywords.add(keyword.getName()));
            list.add(new PersonAndKeywords(person.getPersonId(), keywords));
        }
        return list;
    }

    public void fillPersonsPageRank() {
        LinkedList<PersonAndKeywords> list = getPersonsAndKeywords();
        CountDownLatch latch = new CountDownLatch(list.size());
        ExecutorService service = Executors.newFixedThreadPool(10);
        MongoClient mongoClient = new MongoClient("localhost");
        MongoDatabase database = mongoClient.getDatabase("urlwordsrate");
        MongoCollection<Document> collection = database.getCollection("rating");
        while (list.size() != 0) {
            PersonAndKeywords personAndKeywords = list.pop();
            service.submit(() -> {
                List<Bson> bsonList = new ArrayList<>();
                for (String word : personAndKeywords.getKeywords()) {
                    bsonList.add(exists("words." + word, true));
                }
                PersonPageRankDAOImp personPageRankDAOImp = new PersonPageRankDAOImp();
                try {
                    for (Document doc : collection.find(or(bsonList))) {
                        int count = 0;
                        for (String word : personAndKeywords.getKeywords()) {
                            count += doc.get("words", new Document()).getInteger(word, 0);
                        }
                        try {
                            personPageRankDAOImp.insert(new PersonsPageRank(personAndKeywords.getPersonId(),
                                    doc.getInteger("_id"), count));
                            System.out.printf("personID: %s, pageID: %d, count: %d is added to personspagerank\n",
                                    personAndKeywords.getPersonId(), doc.getInteger("_id"), count);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (MongoException e) {
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
        mongoClient.close();
    }
}