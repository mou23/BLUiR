package bluir.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

import Custom_IR.IndexPhase.Indexer;
import Custom_IR.QueryPhase.QueryRunner;
import org.eclipse.core.runtime.CoreException;

import bluir.entity.BugReport;
import bluir.evaluation.Evaluation;
import bluir.extraction.FactExtractor;
import bluir.extraction.QueryExtractor;
import bluir.parser.XMLParser;
import bluir.utility.CommitCheckout;


public class Core {
	private final String docsLocation = Property.getInstance().WorkDir + "docs";
	private final String indexLocation = Property.getInstance().WorkDir + "index";
	private final String bugFilePath = Property.getInstance().BugFilePath;
	private final String queryFilePath = Property.getInstance().WorkDir + "query";
	private final String resultLocation = Property.getInstance().WorkDir + "result";
	private final String workDir = Property.getInstance().WorkDir;
	private int topN = Property.getInstance().topN;

	public void process() {
		XMLParser parser = new XMLParser();
		List<BugReport> bugRepo = parser.createRepositoryList(bugFilePath);
		System.out.println("total bugs " + bugRepo.size());
		for(int i=0; i<bugRepo.size(); i++) {
			BugReport bug = bugRepo.get(i);
//			System.out.println(bug.getBugId());
//			if(bug.getBugId().equals("391384") == false) {
//				continue;
//			}
				
			if (!createQueryIndex(bug))
				return;
			try
			{
				Property.getInstance().commitHash = bug.getCommit();
				System.out.println("checkout to one commit before fix...");
				new CommitCheckout().create();
			}
			catch (Exception z) {
				z.printStackTrace();
				return;
			}
			if (!createDocs())
				return;
			if (!index())
				return;
			if (!retrieve())
				return;
			if (!evaluation(bug))
				return;
//			if(i==5)
//				break;
		}

		System.out.println("finished");
	}

	boolean createQueryIndex(BugReport bug) {
		try {
			System.out.println("creating query...");
			int repoSize = QueryExtractor.extractSumDesField(bug, queryFilePath);
			System.out.println(repoSize + " queries where created successfully!");
		} catch (IOException e) {
			System.out.println("Please check your bug repo or query file path...");
			return false;
		}
		return true;
	}

	boolean createDocs() {
		try {
			System.out.println("creating docs...");

			if (!FactExtractor.extractEclipseFacts(Property.getInstance().SourceCodeDir, docsLocation))
				return false;

			System.out.println(Property.getInstance().FileCount + " file processed!");
			topN = Property.getInstance().FileCount;

		} catch (IOException | CoreException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			System.err.println("Error occurs when we're creating docs folder!");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	boolean index() {
		try {
			System.out.println("Creating indexes...");

			// Index directory
			File indexDir = new File(indexLocation);
			if (!indexDir.exists())
				if (!indexDir.mkdirs())
					throw new Exception();

			// todo: this path must be changed for each device accordingly
			// Paths to the stopwords and fields files
			String stopwordsFile = "stopwords";
			String fieldsFile = "fields";

			Indexer indexer= new Indexer(workDir, docsLocation, indexLocation, stopwordsFile, fieldsFile);
			if (!indexer.index()) {
				return false;
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error occurs while working with file IO");
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error occurs while working with process");
			System.err.println("Stopping Execution....");
			return false;
		}

		System.out.println("Indexing completed successfully!");
		return true;
	}


	boolean retrieve(){
		try {
			System.out.println("Retrieval is in progress...");

			File resultDir = new File(resultLocation);
			if (!resultDir.exists())
				if (!resultDir.mkdirs())
					throw new Exception();

			String stopwordsFile = "stopwords";
			String fieldsFile = "fields";

			QueryRunner queryRunner = new QueryRunner(resultLocation, queryFilePath, topN, indexLocation, stopwordsFile, fieldsFile);

			if (!queryRunner.run()) {
				return false;
			}

		}
		catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error occurs while working with file IO");
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error occurs while working with process");
			System.err.println("Stopping Execution....");
			return false;
		}

		System.out.println("Retrieval completed successfully!");
		return true;
	}

	boolean evaluation(BugReport bug) {
		try {
			System.out.println("Evaluating....");

			new Evaluation().evaluate(bug);

			System.out.println("Done!");

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
