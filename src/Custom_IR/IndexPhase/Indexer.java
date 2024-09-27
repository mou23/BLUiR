package Custom_IR.IndexPhase;

import java.nio.file.Paths;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.*;
import org.apache.lucene.document.Document;

public class Indexer {

    private String workingDirectory;
    private String corpusPath;
    private String indexPath;
    private String stopwordsPath;
    private String fieldsPath;

    public Indexer(String workingDirectory, String corpusPath, String indexPath, String stopwordsPath, String fieldsPath) {
        this.workingDirectory = workingDirectory;
        this.corpusPath = corpusPath;
        this.indexPath = indexPath;
        this.stopwordsPath = stopwordsPath;
        this.fieldsPath = fieldsPath;
    }

    public boolean index(){
        try {
            // Load stopwords
//            System.out.println("Loading stopwords...");
            Set<String> stopwords = StopwordsLoader.loadStopwords(stopwordsPath);

            // Load fields
//            System.out.println("Loading fields...");
            Set<String> fields = FieldsLoader.loadFields(fieldsPath);

            // Create analyzer
//            System.out.println("Creating analyzer...");
            Analyzer analyzer = new CustomAnalyzer(stopwords);

            // Create index writer
//            System.out.println("Initializing index writer...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            IndexWriter writer = new IndexWriter(dir, iwc);

            // Parse and index documents
//            System.out.println("Parsing and indexing documents...");
            List<Document> documents = TrecTextParser.parseDocuments(workingDirectory, corpusPath, fields);

            int docCount = 0;
            for (Document doc : documents) {
                writer.addDocument(doc);
                docCount++;
                if (docCount % 1000 == 0) {
                    System.out.println(docCount + " documents indexed...");
                }
            }

            // Close writer
//            System.out.println("Closing index writer...");
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}

