package http;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class PageParser {
    private Set<String> excludeWords = new HashSet<>(Arrays.asList("или", "но", "что", "чтобы", "от", "ну", "ни",
            "ко", "со", "во", "на", "из", "за", "по", "не", "для", "под", "около", "если", "да", "нет", "перед",
            "через", "сквозь", "при", "над", "до", "об", "обо", "после", "же", "так", "также", "либо", "ещё",
            "еще", "ый", "ой", "ая", "ые", "ую", "уже", "пока", "как"));
    private static Pattern pattern = Pattern.compile("[^а-я^А-Я^ё^Ё^a-z^A-Z]+");

//    private Set<String> excludeWords = new HashSet<>(Arrays.asList("и", "или", "но", "что", "чтобы", "а", "о", "к", "от",
//            "с", "ко", "со", "во", "на", "из", "за", "по", "не", "для", "под", "около", "если", "да", "нет", "перед",
//            "через", "сквозь", "при", "в", "над", "до", "об", "обо", "после", "же", "так", "также", "либо", "у", "ещё",
//            "еще", "й", "ый", "ой", "ая", "ые", "уже", "пока", "как"));

    public Map<String, Integer> countWords(String url) throws IOException {
        Document doc;
        Map<String, Integer> map = new HashMap<>();
        String article;
        doc = Jsoup.connect(url).get();
        article = doc.body().getElementsByTag("p").text();
        article = (doc.title() + " " + article).toLowerCase();
//        long startTime = System.nanoTime();
//        article = article.toLowerCase().replaceAll("[^а-я^А-Я^ё^Ё^a-z^A-Z]", " ");
//        List<String> list = Arrays.asList(article.split("\\s+"));
//        List<String> list = Arrays.asList(article.split("[^(а-яА-ЯёЁa-zA-Z)]+"));
        List<String> list = Arrays.asList(pattern.split(article));
//        long endTime = System.nanoTime();
//        System.out.printf("time: %d\n", (endTime - startTime)/1_000_000);
//        List<String> list = Arrays.asList(article.split("[^[а-яА-ЯёЁa-zA-Z]]+"));
//        List<String> list = new ArrayList<>(Arrays.asList(article.split("[^а-я^А-Я^ё^Ё^a-z^A-Z]+")));
        list.stream().filter(this::isIncludedWord)
                .forEach(word -> map.merge(word, 1, (value, newValue) -> value + newValue));
        return map;
    }

    private boolean isIncludedWord(String word) {
        return !(word.length() == 1 || excludeWords.contains(word));
    }
}
