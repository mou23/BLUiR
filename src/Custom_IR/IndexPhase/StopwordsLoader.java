package Custom_IR.IndexPhase;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class StopwordsLoader {

    public static Set<String> loadStopwords(String stopwordsFilePath) throws Exception {
        Set<String> stopwords = new HashSet<>();

//        System.out.println("Parsing stopwords file: " + stopwordsFilePath);
        File file = new File(stopwordsFilePath);
        Document doc = Jsoup.parse(file, "UTF-8");

        Elements wordElements = doc.select("word");
        for (org.jsoup.nodes.Element wordElement : wordElements) {
            String word = wordElement.text().trim();
            stopwords.add(word);
        }

        System.out.println("Loaded " + stopwords.size() + " stopwords.");
        return stopwords;
    }
}
