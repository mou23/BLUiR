package bluir.extraction;

import bluir.entity.BugReport;
import bluir.parser.XMLParser;
import bluir.utility.PreProcessor;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;


public class QueryExtractor
{
	public static int extractSumDesField(BugReport bug, String outputPath) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));
		bw.write("<parameters>");
		bw.newLine();

		bw.write("\t<query>\n\t\t<number>" + bug.getBugId() + "</number>");
		bw.newLine();

		String text = PreProcessor.process1(bug.getSummary()) + " " + PreProcessor.process1(bug.getDescription());
		text = text.replace("\n", " ");

		bw.write("\t\t<text> " + text + " </text>\n\t</query>");
		bw.newLine();
		

		bw.write("</parameters>");
		bw.newLine();

		bw.close();

		return 1;
	}


	static String addField(String str, String fieldName)
	{
		String addedStr = "";

		String[] queryParts = str.split(" ");
		String[] arrayOfString1; int j = (arrayOfString1 = queryParts).length; for (int i = 0; i < j; i++) { String eachPart = arrayOfString1[i];
		if (!eachPart.equals("")) {
			eachPart = eachPart + ".(" + fieldName + ")";
			addedStr = addedStr + eachPart + " ";
		}
	}

		return addedStr;
	}

	static String addField(String str, String fieldName, double weight)
	{
		String addedStr = "";

		String[] queryParts = str.split(" ");
		String[] arrayOfString1; int j = (arrayOfString1 = queryParts).length; for (int i = 0; i < j; i++) { String eachPart = arrayOfString1[i];
		if (!eachPart.equals("")) {
			eachPart = eachPart + ".(" + fieldName + ")";
			addedStr = addedStr + eachPart + " ";
		}
	}

		return addedStr;
	}


	static String splitCamelCase(String s)
	{
		return s.replaceAll(
				String.format("%s|%s|%s", new Object[] {
						"(?<=[A-Z])(?=[A-Z][a-z])",
						"(?<=[^A-Z])(?=[A-Z])",
						"(?<=[A-Za-z])(?=[^A-Za-z])" }),

				" ");
	}



	static void calculateRatio(String XMLPath)
	{
		XMLParser parser = new XMLParser();
		List<BugReport> bugRepo = parser.createRepositoryList(XMLPath);

		double ratioSum = 0.0D;

		int sumTotal = 0;
		int desTotal = 0;

		for (int i = 0; i < bugRepo.size(); i++) {
			BugReport bug = (BugReport)bugRepo.get(i);

			int summaryLength = countWord(bug.getSummary());
			int desLength = countWord(bug.getDescription());
			sumTotal += summaryLength;
			desTotal += desLength;
			double ratio = summaryLength / desLength;
			ratioSum += ratio;
			System.out.println(summaryLength + "\t" + desLength + "\t" + ratio);
		}
		System.out.println(ratioSum / bugRepo.size() + "\t" + sumTotal / desTotal);
	}


	static int countWord(String subject)
	{
		String[] word = PreProcessor.process(subject).split(" ");
		int c = 0;
		for (int j = 0; j < word.length; j++) {
			if (word[j].length() > 2) {
				c++;
			}
		}

		return c;
	}
}

