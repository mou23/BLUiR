package Custom_IR.QueryPhase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class QueryLoader {

    public static List<QueryData> loadQueries(String queryFilePath) throws Exception {
        List<QueryData> queries = new ArrayList<>();

        File file = new File(queryFilePath);
        org.jsoup.nodes.Document doc = Jsoup.parse(file, "UTF-8");

        Elements queryElements = doc.select("query");
        for (org.jsoup.nodes.Element queryElement : queryElements) {
            String number = queryElement.select("number").text().trim();
            String text = queryElement.select("text").text().trim();

            if (number.contains(" ")){
                continue;
            }

            queries.add(new QueryData(number, text));
        }

        System.out.println("Loaded " + queries.size() + " queries.");
        return queries;
    }
}