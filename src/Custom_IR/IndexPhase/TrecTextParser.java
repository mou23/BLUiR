package Custom_IR.IndexPhase;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

import org.apache.lucene.document.*;

public class TrecTextParser {

    public static List<Document> parseDocuments(String workingDirectory, String corpusPath, Set<String> fields) throws IOException {

        List<Document> documents = new ArrayList<>();
        // Create a logger instance
        final Logger logger = Logger.getLogger(TrecTextParser.class.getName());
        FileHandler fileHandler;
        try {
            // Set up the file handler and formatter
            fileHandler = new FileHandler(workingDirectory+"/parsed_docs.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Remove the default console handler to prevent printing to the terminal
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                if (handler instanceof ConsoleHandler) {
                    rootLogger.removeHandler(handler);
                }
            }

            // Set the logger level to INFO (or adjust as needed)
            logger.setLevel(Level.INFO);
            logger.info("Walking through corpus directory: " + corpusPath);
            Files.walk(Paths.get(corpusPath))
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        try {
                            logger.info("Parsing file: " + filePath);
                            String content = new String(Files.readAllBytes(filePath));
                            List<Document> docsInFile = parseTrecText(content, fields);
                            documents.addAll(docsInFile);
                            logger.info("Parsed " + docsInFile.size() + " documents from file.");
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "Error while parsing file: " + filePath, e);
                        }
                    });

            logger.info("Total documents parsed: " + documents.size());
            fileHandler.close();
            logger.removeHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return documents;
    }

    private static List<Document> parseTrecText(String content, Set<String> fields) {
        List<Document> documents = new ArrayList<>();

        String[] docs = content.split("<DOC>");
        for (String docContent : docs) {
            if (docContent.trim().isEmpty()) continue;

            String docno = extractTagContent(docContent, "DOCNO");
            Map<String, String> fieldContents = new HashMap<>();

            for (String field : fields) {
                String fieldValue = extractTagContent(docContent, field);
                fieldContents.put(field, fieldValue);
            }

            org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
            luceneDoc.add(new StringField("docId", docno, Field.Store.YES));

            for (Map.Entry<String, String> entry : fieldContents.entrySet()) {
                luceneDoc.add(new TextField(entry.getKey(), entry.getValue(), Field.Store.YES));
            }
//            System.out.println("file "+ docno);
//            System.out.println("content "+ luceneDoc);
            documents.add(luceneDoc);
        }

        return documents;
    }

    private static String extractTagContent(String docContent, String tag) {
        int start = docContent.indexOf("<" + tag + ">");
        int end = docContent.indexOf("</" + tag + ">");
        if (start == -1 || end == -1) return "";
        start += tag.length() + 2;
        return docContent.substring(start, end).trim();
    }
}
