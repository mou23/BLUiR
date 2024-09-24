package custom_indri;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.util.regex.*;

import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.KStemFilter;

import java.io.IOException;
import java.io.StringReader;


public class CustomIndriIndexer {
    private Set<String> stopwords = new HashSet<>();
    private Set<String> fieldsToIndex = new HashSet<>();
    private Map<String, Map<String, List<Integer>>> invertedIndex = new HashMap<>();


    // Load stopwords from XML
    public void loadStopwords(String stopwordsFile) throws Exception {
        File file = new File(stopwordsFile);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("word");
        for (int i = 0; i < nList.getLength(); i++) {
            stopwords.add(nList.item(i).getTextContent().trim().toLowerCase());
        }
    }

    // Load fields to index from XML
    public void loadFields(String fieldsFile) throws Exception {
        File file = new File(fieldsFile);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("field");
        for (int i = 0; i < nList.getLength(); i++) {
            Element fieldElement = (Element) nList.item(i);
            String fieldName = fieldElement.getElementsByTagName("name").item(0).getTextContent().trim().toLowerCase();
            fieldsToIndex.add(fieldName);
        }
    }

    // Process documents and build the inverted index
    public void processDocuments(String docsPath) throws Exception {
        Files.walk(Paths.get(docsPath)).filter(Files::isRegularFile).forEach(path -> {
            try {
                String content = new String(Files.readAllBytes(path));
                processDocument(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // Process individual document
    private void processDocument(String content) {
        Pattern docPattern = Pattern.compile("<DOC>(.*?)</DOC>", Pattern.DOTALL);
        Matcher matcher = docPattern.matcher(content);
        while (matcher.find()) {
            String docContent = matcher.group(1);
            String docNo = extractTagContent(docContent, "DOCNO");

            for (String field : fieldsToIndex) {
                String fieldContent = extractTagContent(docContent, field);
                if (fieldContent != null) {
                    tokenizeAndIndex(docNo, fieldContent);
                }
            }
        }
    }

    // Tokenize, remove stopwords, stem, and index tokens
    private void tokenizeAndIndex(String docNo, String text) {
        try (WhitespaceTokenizer tokenizer = new WhitespaceTokenizer()) {
            tokenizer.setReader(new StringReader(text));

            // Apply KStemFilter for Krovetz stemming
            TokenStream tokenStream = new KStemFilter(tokenizer);
            CharTermAttribute charTermAttr = tokenStream.addAttribute(CharTermAttribute.class);

            tokenStream.reset();
            int position = 0;
            while (tokenStream.incrementToken()) {
                String token = charTermAttr.toString().toLowerCase();

                // Skip stopwords
                if (!stopwords.contains(token) && !token.isEmpty()) {
                    indexToken(docNo, token, position);
                }
                position++;
            }
            tokenStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add token to inverted index (same as before)
    private void indexToken(String docNo, String token, int position) {
        invertedIndex.putIfAbsent(token, new HashMap<>());
        invertedIndex.get(token).putIfAbsent(docNo, new ArrayList<>());
        invertedIndex.get(token).get(docNo).add(position);
    }

    // Extract content between tags
    private String extractTagContent(String docContent, String tagName) {
        Pattern tagPattern = Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">", Pattern.DOTALL);
        Matcher matcher = tagPattern.matcher(docContent);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    // Save the inverted index to a file
    public void saveIndexToFile(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(invertedIndex);
        }
    }


    public static void main(String[] args) throws Exception {
        CustomIndriIndexer indexer = new CustomIndriIndexer();

        // Load stopwords and fields
        indexer.loadStopwords("stopwords");
        indexer.loadFields("fields");

        // Process documents and build the index
        indexer.processDocuments("data/temp/BLUiR_first_Run/docs");

        // Save the index to a file
        indexer.saveIndexToFile("index.dat");
    }
}
