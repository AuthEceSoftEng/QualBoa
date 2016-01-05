package Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import Database.DownloadedFile;
import Database.FileHandler;
import Downloader.*;
import edu.iastate.cs.boa.BoaException;
import Miner.*;

public class Recommender {

	public static void main(String[] args) throws Exception, BoaException, IOException {
		// Get the query as a java interface
		String queryInJava = new String(Files.readAllBytes(Paths.get("input.java")), "UTF-8");

		// Create a file handler to be used throughout the recommender
		FileHandler fileHandler = new FileHandler("Files");
		boolean useFileHandler = true;

		// Execute query in Boa
		BoaClientDownloader boaDownloader = new BoaClientDownloader(PropertiesHandler.BoaUsername,
				PropertiesHandler.BoaPassword);
		String query = boaDownloader.createQuery(queryInJava);
		if (useFileHandler)
			fileHandler.writeFile("Query.txt", query);
		String queryResult = boaDownloader.runQuery(query);
		if (useFileHandler)
			fileHandler.writeFile("Boa_output.txt", queryResult);
		// Comment the lines above and uncomment the following line to use the downloaded Boa query result
		// String queryResult = fileHandler.readFile("Boa_output.txt");

		// Download files from GitHub
		GitHubFileDownloader gitHubDownloader = new GitHubFileDownloader(PropertiesHandler.GitHubUsername,
				PropertiesHandler.GitHubPassword);
		ArrayList<DownloadedFile> files = gitHubDownloader.downloadFiles(queryResult);
		if (useFileHandler)
			fileHandler.writeAllDownloadedFiles(files);
		// Comment the lines above and uncomment the following line to use the pre-downloaded results
		// ArrayList<DownloadedFile> files = fileHandler.readAllDownloadedFiles();

		// Score the files
		Scorer scorer = new Scorer(files);
		ArrayList<Result> results = scorer.getResults();
		if (useFileHandler)
			fileHandler.writeAllResults(results);

		// Print the top 10 recommended results
		System.out.println("Top 10 recommended results:");
		for (int i = 0; i < 10; i++)
			System.out.println(results.get(i).toPrint());
		if (useFileHandler) {
			String resultString = "";
			for (Result result : results)
				resultString += result.toPrint() + "\n";
			fileHandler.writeFile("Results.txt", resultString);
		}
	}
}
