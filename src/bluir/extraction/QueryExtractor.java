package bluir.extraction;

import bluir.entity.BugReport;
import bluir.parser.XMLParser;
import bluir.utility.PreProcessor;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class QueryExtractor {

	public static int extractSumDesField(String XMLPath, String outputPath) throws IOException {
		XMLParser parser = new XMLParser();
		List<BugReport> bugRepo = parser.createRepositoryList(XMLPath);

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath))) {
			bw.write("<parameters>");
			bw.newLine();

			for (BugReport bug : bugRepo) {
				bw.write("\t<query>\n\t\t<number>" + bug.getBugId() + "</number>");
				bw.newLine();
				bw.write("\t\t<text> #weight(" +
						generateWeightedFields(bug) +
						")</text>\n\t</query>");
				bw.newLine();
			}

			bw.write("</parameters>");
			bw.newLine();
		}

		return bugRepo.size();
	}

	private static String generateWeightedFields(BugReport bug) {
		return String.join(" ",
				addWeightedField(PreProcessor.process(bug.getSummary()), "class", 1.0),
				addWeightedField(PreProcessor.process1(bug.getSummary()), "class", 1.0),
				addWeightedField(PreProcessor.process(bug.getDescription()), "class", 1.0),
				addWeightedField(PreProcessor.process1(bug.getDescription()), "class", 1.0),

				addWeightedField(PreProcessor.process(bug.getSummary()), "method", 1.0),
				addWeightedField(PreProcessor.process1(bug.getSummary()), "method", 1.0),
				addWeightedField(PreProcessor.process(bug.getDescription()), "method", 1.0),
				addWeightedField(PreProcessor.process1(bug.getDescription()), "method", 1.0),

				addWeightedField(PreProcessor.process(bug.getSummary()), "identifier", 1.0),
				addWeightedField(PreProcessor.process1(bug.getSummary()), "identifier", 1.0),
				addWeightedField(PreProcessor.process(bug.getDescription()), "identifier", 1.0),
				addWeightedField(PreProcessor.process1(bug.getDescription()), "identifier", 1.0),

				addWeightedField(PreProcessor.process(bug.getSummary()), "comments", 1.0),
				addWeightedField(PreProcessor.process(bug.getDescription()), "comments", 1.0)
		);
	}

	private static String addWeightedField(String text, String fieldName, double weight) {
		StringBuilder result = new StringBuilder();
		String[] queryParts = text.split(" ");
		for (String part : queryParts) {
			if (!part.isEmpty()) {
				result.append(weight).append(" ").append(part).append(".(").append(fieldName).append(") ");
			}
		}
		return result.toString().trim();
	}

	public static void calculateRatio(String XMLPath) {
		XMLParser parser = new XMLParser();
		List<BugReport> bugRepo = parser.createRepositoryList(XMLPath);

		double ratioSum = 0.0;
		int sumTotal = 0;
		int desTotal = 0;

		for (BugReport bug : bugRepo) {
			int summaryLength = countWords(bug.getSummary());
			int descriptionLength = countWords(bug.getDescription());
			sumTotal += summaryLength;
			desTotal += descriptionLength;
			double ratio = (double) summaryLength / descriptionLength;
			ratioSum += ratio;
			System.out.println(summaryLength + "\t" + descriptionLength + "\t" + ratio);
		}
		System.out.println(ratioSum / bugRepo.size() + "\t" + (double) sumTotal / desTotal);
	}

	private static int countWords(String text) {
		String[] words = PreProcessor.process(text).split(" ");
		int count = 0;
		for (String word : words) {
			if (word.length() > 2) {
				count++;
			}
		}
		return count;
	}
}
