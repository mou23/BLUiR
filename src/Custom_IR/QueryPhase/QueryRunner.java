package Custom_IR.QueryPhase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
    private final String queryFilePath;
    private int count = 100;
    private final String indexPath;
    private final String stopwordsPath;
    private final String fieldsPath;
    private static String resultLocation;
    private final String rule = "method:tfidf,k1:1.0,b:0.3";
    private final String runTag = "CustomIR";

    public QueryRunner(String resultLocation, String queryFilePath, int count, String indexPath, String stopwordsPath, String fieldsPath) {
        this.queryFilePath = queryFilePath;
        this.count = count;
        this.indexPath = indexPath;
        this.stopwordsPath = stopwordsPath;
        this.fieldsPath = fieldsPath;
        this.resultLocation = resultLocation;
    }

    public boolean run() {
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

            // Open index
//            System.out.println("Opening index at: " + indexPath);
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            DirectoryReader reader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(reader);

            // Set similarity
//            System.out.println("Setting similarity...");
            Similarity similarity = getSimilarity(rule);
            searcher.setSimilarity(similarity);

            // Parse queries
//            System.out.println("Parsing queries from file: " + queryFilePath);
            List<QueryData> queries = QueryLoader.loadQueries(queryFilePath);

            // Execute queries
            System.out.println("Executing queries...");
            executeQueries(searcher, analyzer, queries, count, fields, runTag);

            // Close reader
//            System.out.println("Closing index reader...");
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void executeQueries(IndexSearcher searcher, Analyzer analyzer, List<QueryData> queries, int count, Set<String> fields, String runTag) throws Exception {
        String[] fieldsArray = fields.toArray(new String[0]);

        // Increase the max clause count
        BooleanQuery.setMaxClauseCount(10000); // Adjust the number as needed

        for (QueryData queryData : queries) {
            String queryNumber = queryData.getNumber();
            String queryText = queryData.getText();

            // Create a new file for the query results, named with the queryNumber
            if (queryNumber.length() > 10){
                System.out.println(queryNumber);
            }

            File resultsFile = new File(Paths.get(resultLocation, queryNumber + ".txt").toString());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultsFile))) {

                MultiFieldQueryParser parser = new MultiFieldQueryParser(fieldsArray, analyzer);

                try {
                    Query query = parser.parse(QueryParser.escape(queryText));

                    TopDocs topDocs = searcher.search(query, count);
                    ScoreDoc[] hits = topDocs.scoreDocs;
                    
                    System.out.println("Query Number: " + queryNumber + ", Max Count: " + count + ", Hits Length: " + hits.length);
                    
                    for (int i = 0; i < hits.length; i++) {
                        Document doc = searcher.doc(hits[i].doc);
                        String docId = doc.get("docId");
                        float score = hits[i].score;
                        int rank = i + 1;

                        // Write the result to the file in TREC format
                        writer.write(String.format("%s,Q0,%s,%d,%f,%s%n", queryNumber, docId, rank, score, runTag));
                    }
                } catch (BooleanQuery.TooManyClauses e) {
                    System.err.println("Query " + queryNumber + " exceeds the maximum number of clauses allowed. Re-run the program and adjust MaxClauseCount!");
                    e.printStackTrace();

                }

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

