package Custom_IR.QueryPhase;

import java.nio.file.Paths;
import java.util.*;

import Custom_IR.IndexPhase.FieldsLoader;
import Custom_IR.IndexPhase.StopwordsLoader;
import Custom_IR.IndexPhase.CustomAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.*;
import org.apache.lucene.queryparser.classic.*;

public class QueryRunner {

    public static void main(String[] args) {
        System.out.println("Starting query execution...");

        String queryFilePath = "data/temp/BLUiR_first_Run/query";
        int count = 100;
        String indexPath = "data/temp/BLUiR_first_Run/index";
        String stopwordsPath = "stopwords";
        String fieldsPath = "fields";
        String rule = "method:tfidf,k1:1.0,b:0.3";
        String runTag = "myRun"; // You can change this to any identifier

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

            // Open index
            System.out.println("Opening index at: " + indexPath);
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            DirectoryReader reader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(reader);

            // Set similarity
            System.out.println("Setting similarity...");
            Similarity similarity = getSimilarity(rule);
            searcher.setSimilarity(similarity);

            // Parse queries
            System.out.println("Parsing queries from file: " + queryFilePath);
            List<QueryData> queries = QueryLoader.loadQueries(queryFilePath);

            // Execute queries
            System.out.println("Executing queries...");
            executeQueries(searcher, analyzer, queries, count, fields, runTag);

            // Close reader
            System.out.println("Closing index reader...");
            reader.close();

            System.out.println("Query execution completed successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void executeQueries(IndexSearcher searcher, Analyzer analyzer, List<QueryData> queries, int count, Set<String> fields, String runTag) throws Exception {
        String[] fieldsArray = fields.toArray(new String[0]);

        for (QueryData queryData : queries) {
            String queryNumber = queryData.getNumber();
            String queryText = queryData.getText();

            System.out.println("Executing query number: " + queryNumber);

            MultiFieldQueryParser parser = new MultiFieldQueryParser(fieldsArray, analyzer);

            Query query = parser.parse(QueryParser.escape(queryText));

            TopDocs topDocs = searcher.search(query, count);
            ScoreDoc[] hits = topDocs.scoreDocs;

            for (int i = 0; i < hits.length; i++) {
                Document doc = searcher.doc(hits[i].doc);
                String docId = doc.get("docId");
                float score = hits[i].score;
                int rank = i + 1;

                // Output in TREC format
                System.out.println(String.format("%s Q0 %s %d %f %s", queryNumber, docId, rank, score, runTag));
            }
        }
    }

    private static Similarity getSimilarity(String rule) {
        // Parse rule string and create Similarity accordingly
        // For 'method:tfidf,k1:1.0,b:0.3', we can use BM25Similarity with k1 and b
        float k1 = 1.0f;
        float b = 0.3f;

        String[] params = rule.split(",");
        for (String param : params) {
            if (param.startsWith("k1:")) {
                k1 = Float.parseFloat(param.substring(3));
            } else if (param.startsWith("b:")) {
                b = Float.parseFloat(param.substring(2));
            }
        }

        // Using BM25Similarity as an approximation
        return new BM25Similarity(k1, b);
    }
}

