package Custom_IR.IndexPhase;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class FieldsLoader {

    public static Set<String> loadFields(String fieldsFilePath) throws Exception {
        Set<String> fields = new HashSet<>();

        System.out.println("Parsing fields file: " + fieldsFilePath);
        File file = new File(fieldsFilePath);
        Document doc = Jsoup.parse(file, "UTF-8");

        Elements fieldElements = doc.select("field");
        for (org.jsoup.nodes.Element fieldElement : fieldElements) {
            String fieldName = fieldElement.select("name").text().trim();
            fields.add(fieldName);
        }

        System.out.println("Loaded " + fields.size() + " fields.");
        return fields;
    }
}

