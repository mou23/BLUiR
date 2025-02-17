package bluir.parser;

import bluir.entity.BugReport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser {

	public HashMap<String, BugReport> createRepositoryMap(String XMLPath) {
		HashMap<String, BugReport> bugRepository = new HashMap<>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(XMLPath);
			doc.getDocumentElement().normalize();

			// Locate the <database> element
			NodeList databaseList = doc.getElementsByTagName("database");

			for (int dbIndex = 0; dbIndex < databaseList.getLength(); dbIndex++) {
				Node databaseNode = databaseList.item(dbIndex);
				if (databaseNode.getNodeType() == Node.ELEMENT_NODE) {
					Element databaseElement = (Element) databaseNode;

					// Locate the <table> element
					NodeList tableList = databaseElement.getElementsByTagName("table");

					for (int tableIndex = 0; tableIndex < tableList.getLength(); tableIndex++) {
						Node tableNode = tableList.item(tableIndex);
						if (tableNode.getNodeType() == Node.ELEMENT_NODE) {
							Element tableElement = (Element) tableNode;

							BugReport bugReport = new BugReport();

							// Extract column data
							String bugId = getColumnValue("bug_id", tableElement);
							bugReport.setBugId(bugId);

							String summary = getColumnValue("summary", tableElement);
							bugReport.setSummary(summary);

							String description = getColumnValue("description", tableElement);
							bugReport.setDescription(description);

							// Extract the fixed files
							String files = getColumnValue("files", tableElement);
							Set<String> fileSet = parseFiles(files);
							bugReport.setFixedFiles(fileSet);

							bugRepository.put(bugId, bugReport);
						}
					}
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		return bugRepository;
	}

	public List<BugReport> createRepositoryList(String XMLPath) {
		List<BugReport> bugRepository = new ArrayList<>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(XMLPath);
			doc.getDocumentElement().normalize();

			NodeList databaseList = doc.getElementsByTagName("database");

			for (int dbIndex = 0; dbIndex < databaseList.getLength(); dbIndex++) {
				Node databaseNode = databaseList.item(dbIndex);
				if (databaseNode.getNodeType() == Node.ELEMENT_NODE) {
					Element databaseElement = (Element) databaseNode;

					NodeList tableList = databaseElement.getElementsByTagName("table");

					for (int tableIndex = 0; tableIndex < tableList.getLength(); tableIndex++) {
						Node tableNode = tableList.item(tableIndex);
						if (tableNode.getNodeType() == Node.ELEMENT_NODE) {
							Element tableElement = (Element) tableNode;

							BugReport bugReport = new BugReport();
							String bugId = getColumnValue("bug_id", tableElement);
//							System.out.println(bugId);
							bugReport.setBugId(bugId);

							String summary = getColumnValue("summary", tableElement);
							bugReport.setSummary(summary);

							String description = getColumnValue("description", tableElement);
							bugReport.setDescription(description);
							
							String commit = getColumnValue("commit", tableElement);
							bugReport.setCommit(commit);
							
							String commit_timestamp = getColumnValue("commit_timestamp", tableElement);
							bugReport.setFixedDate(Long.parseLong(commit_timestamp));

							String files = getColumnValue("files", tableElement);
							Set<String> fileSet = parseFiles(files);
							bugReport.setFixedFiles(fileSet);

							bugRepository.add(bugReport);
						}
					}
				}
			}
			bugRepository.sort((bug1, bug2) -> Long.compare(bug1.getFixedDate(), bug2.getFixedDate()));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		int totalSize = bugRepository.size();
		int splitIndex = (int) Math.ceil(totalSize * 0.6);
		
		return bugRepository.subList(splitIndex, totalSize);
	}

	private static String getColumnValue(String columnName, Element tableElement) {
		NodeList columnList = tableElement.getElementsByTagName("column");
		for (int i = 0; i < columnList.getLength(); i++) {
			Node columnNode = columnList.item(i);
			if (columnNode.getNodeType() == Node.ELEMENT_NODE) {
				Element columnElement = (Element) columnNode;
				if (columnElement.getAttribute("name").equals(columnName)) {
					return columnElement.getTextContent();
				}
			}
		}
		return null;
	}

	private static Set<String> parseFiles(String files) {
		Set<String> fileSet = new HashSet<>();
		if (files != null && !files.isEmpty()) {
			String[] fixedFiles = files.split("\\.java");
			for (String file : fixedFiles) {
				if(file.length()>0)
					fileSet.add(file.trim()+".java");
			}
		}
//		System.out.println(fileSet);
		return fileSet;
	}

}
