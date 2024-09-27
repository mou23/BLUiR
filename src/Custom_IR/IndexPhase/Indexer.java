package Custom_IR.IndexPhase;

import java.nio.file.Paths;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.*;

public class Indexer {

    public static void main(String[] args) {
        System.out.println("Starting indexing process...");

        String corpusPath = "data/temp/BLUiR_first_Run/docs";
        String indexPath = "data/temp/BLUiR_first_Run/index";
        String stopwordsPath = "stopwords";
        String fieldsPath = "fields";

        try {
            // Load stopwords
            System.out.println("Loading stopwords...");
            Set<String> stopwords = StopwordsLoader.loadStopwords(stopwordsPath);

            // Load fields
            System.out.println("Loading fields...");
            Set<String> fields = FieldsLoader.loadFields(fieldsPath);

            // Create analyzer
            System.out.println("Creating analyzer...");
            Analyzer analyzer = new CustomAnalyzer(stopwords);

            // Create index writer
            System.out.println("Initializing index writer...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            IndexWriter writer = new IndexWriter(dir, iwc);

            // Parse and index documents
            System.out.println("Parsing and indexing documents...");
            List<org.apache.lucene.document.Document> documents = TrecTextParser.parseDocuments(corpusPath, fields);

            int docCount = 0;
            for (org.apache.lucene.document.Document doc : documents) {
                writer.addDocument(doc);
                docCount++;
                if (docCount % 100 == 0) {
                    System.out.println(docCount + " documents indexed...");
                }
            }

            // Close writer
            System.out.println("Closing index writer...");
            writer.close();

            System.out.println("Indexing completed successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

