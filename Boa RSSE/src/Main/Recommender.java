package Main;

import java.io.IOException;
import java.util.ArrayList;

import Database.DownloadedFile;
import Database.FileHandler;
import Downloader.*;
import edu.iastate.cs.boa.BoaException;
import Miner.*;

public class Recommender {

	public static void main(String[] args) throws Exception, BoaException, IOException {
		// The main structure of the recommender
		BoaClientDownloader boaDownloader = new BoaClientDownloader(PropertiesHandler.BoaUsername,
				PropertiesHandler.BoaPassword);
		boaDownloader.runQuery("input.java");
		FileHandler fileHandler = new FileHandler("Files");
		FileHandler resultFileHandler = new FileHandler("Final_Files");
		GitHubFileDownloader gitHubDownloader = new GitHubFileDownloader(PropertiesHandler.GitHubUsername,
				PropertiesHandler.GitHubPassword);
		ArrayList<DownloadedFile> files = gitHubDownloader.downloadFiles("Boa_output.txt", fileHandler);
		Scorer scorer = new Scorer(files, resultFileHandler);
		ArrayList<Result> results = scorer.getResults();
		// print top 10
		System.out.println("Top 10 recommended results:");
		for (int i = 0; i < 10; i++)
			System.out.println(results.get(i).toPrint());
	}
}
