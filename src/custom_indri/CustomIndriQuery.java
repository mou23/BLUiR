package custom_indri;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CustomIndriQuery {
    private Map<String, Map<String, List<Integer>>> invertedIndex = new HashMap<>();
    private int totalDocs = 0; // Total number of documents in the collection
    private double k1 = 1.0;   // TF scaling factor (from the command)
    private double b = 0.3;    // Document length normalization factor (from the command)
    private Map<String, Integer> docLengths = new HashMap<>();  // Stores document lengths

    // Load the inverted index from a file
    @SuppressWarnings("unchecked")
    public void loadIndexFromFile(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            invertedIndex = (Map<String, Map<String, List<Integer>>>) ois.readObject();
            totalDocs = invertedIndex.values().stream()
                    .flatMap(docMap -> docMap.keySet().stream())
                    .collect(Collectors.toSet())
                    .size();
        }
    }

    // Compute average document length
    public double computeAvgDocLength() {
        return docLengths.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    // Compute TF-IDF with k1 and b parameters for a query
    public Map<String, Double> computeTfIdf(String[] queryTerms) {
        Map<String, Double> docScores = new HashMap<>();
        double avgDocLength = computeAvgDocLength();  // Average document length

        // Safeguard: Prevent division by zero in average document length
        if (avgDocLength == 0) {
            avgDocLength = 1.0;  // Set a default value if avgDocLength is zero
        }

        for (String term : queryTerms) {
            term = term.toLowerCase();
            if (invertedIndex.containsKey(term)) {
                Map<String, List<Integer>> docMap = invertedIndex.get(term);
                int docFreq = docMap.size();

                // Safeguard: Ensure docFreq is greater than zero for IDF calculation
                if (docFreq > 0) {
                    // Smoothed IDF calculation to avoid negative values
                    double idf = Math.log((1.0 + totalDocs) / (1.0 + docFreq));

                    for (Map.Entry<String, List<Integer>> entry : docMap.entrySet()) {
                        String docId = entry.getKey();
                        int tf = entry.getValue().size();
                        int docLength = docLengths.getOrDefault(docId, 0);

                        // Safeguard: Avoid division by zero in document length normalization
                        if (docLength == 0) {
                            docLength = 1;  // Set a default value if docLength is zero
                        }

                        // BM25-like TF-IDF with k1 and b
                        double normTf = (tf * (k1 + 1)) / (tf + k1 * (1 - b + b * (docLength / avgDocLength)));
                        double tfIdfScore = normTf * idf;

                        // Sum the scores for each document
                        docScores.put(docId, docScores.getOrDefault(docId, 0.0) + tfIdfScore);
                    }
                } else {
                    // Safeguard: Skip term if docFreq is zero
                    System.out.println("Warning: docFreq is zero for term " + term);
                }
            }
        }

        return docScores;
    }


    // Sort and rank documents based on TF-IDF scores
    public List<Map.Entry<String, Double>> rankDocuments(Map<String, Double> docScores) {
        List<Map.Entry<String, Double>> rankedDocs = new ArrayList<>(docScores.entrySet());
        rankedDocs.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));  // Sort in descending order
        return rankedDocs;
    }

    // Execute the query and print results in TREC format
    public void runQuery(String query, String queryId, int topN, File recommendedDir) throws IOException {
        String[] queryTerms = query.toLowerCase().split("\\s+");
        Map<String, Double> docScores = computeTfIdf(queryTerms);
        List<Map.Entry<String, Double>> rankedDocs = rankDocuments(docScores);

        // Print results in TREC format
        int rank = 1;
        String runId = "CustomIndriRun";
        for (Map.Entry<String, Double> entry : rankedDocs) {
            String docId = entry.getKey();
            double score = entry.getValue();
            System.out.printf("%s Q0 %s %d %.4f %s\n", queryId, docId, rank, score, runId);
            rank++;
            if (rank > topN) break;  // Ensure we only output the top N results
        }

        // Save the top N results to a file
        saveTopNResultsToFile(rankedDocs, queryId, topN, recommendedDir);
    }

    // Save top N results to a file
    private void saveTopNResultsToFile(List<Map.Entry<String, Double>> rankedDocs, String queryId, int topN, File recommendedDir) throws IOException {
        File outputFile = new File(recommendedDir, queryId);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            int rank = 1;
            for (Map.Entry<String, Double> entry : rankedDocs) {
                if (rank > topN) break;
                String docId = entry.getKey();
                double score = entry.getValue();
                writer.write(String.format("%s Q0 %s %d %.4f CustomIndriRun\n", queryId, docId, rank, score));
                rank++;
            }
        }
    }

    // Parse the query XML file
    public List<Query> parseQueryFile(String queryFile) throws Exception {
        List<Query> queries = new ArrayList<>();

        File file = new File(queryFile);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        NodeList queryList = doc.getElementsByTagName("query");
        for (int i = 0; i < queryList.getLength(); i++) {
            Element queryElement = (Element) queryList.item(i);
            String queryNumber = queryElement.getElementsByTagName("number").item(0).getTextContent().trim();
            String queryText = queryElement.getElementsByTagName("text").item(0).getTextContent().trim();
            queries.add(new Query(queryNumber, queryText));
        }

        return queries;
    }

    // Helper class to store query number and text
    static class Query {
        String number;
        String text;

        public Query(String number, String text) {
            this.number = number;
            this.text = text;
        }

        public String getNumber() {
            return number;
        }

        public String getText() {
            return text;
        }
    }

    public static void main(String[] args) throws Exception {
        CustomIndriQuery queryProcessor = new CustomIndriQuery();

        // Load the index from a file
        queryProcessor.loadIndexFromFile("index.dat");

        // Parse the query file (replace with the path to your query file)
        String queryFilePath = "data/temp/BLUiR_first_Run/query";  // Replace with your query file path
        List<Query> queries = queryProcessor.parseQueryFile(queryFilePath);

        // Get the directory of the query file
        File queryFile = new File(queryFilePath);
        File parentDir = queryFile.getParentFile();
        File recommendedDir = new File(parentDir, "recommended");

        // Create the "recommended" directory if it doesn't exist
        if (!recommendedDir.exists()) {
            recommendedDir.mkdir();
        }

        // Run each query and output in TREC format, also save top N recommended results
        int topN = 100;  // Output top 100 results, similar to -count=100
        for (Query query : queries) {
            queryProcessor.runQuery(query.getText(), query.getNumber(), topN, recommendedDir);
        }
    }
}
