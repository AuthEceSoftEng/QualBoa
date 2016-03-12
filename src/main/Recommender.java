package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import edu.iastate.cs.boa.BoaException;
import miner.*;
import database.DownloadedFile;
import database.FileHandler;
import downloader.*;

public class Recommender {

	/**
	 * Test function which has the behavior of executing java -jar QualBoa.jar -query=queries/Stack.java -folder=Files
	 * 
	 * @param args unused parameter.
	 * @throws BoaException when there is an error with the connection to Boa.
	 * @throws IOException when there is an error reading the files given.
	 */
	public static void main(String[] args) throws BoaException, IOException {
		recommend("queries/Stack.java", "Files");
	}

	/**
	 * Receives the filename of the query and the filename of the folder to save the results as parameters and executes
	 * the recommender.
	 * 
	 * @param queryFilename the filename of the query.
	 * @param folderFilename the filename of the folder to save the results.
	 * @throws BoaException when there is an error with the connection to Boa.
	 * @throws IOException when there is an error reading the files given.
	 */
	public static void recommend(String queryFilename, String folderFilename) throws BoaException, IOException {
		// Get the query as a java interface
		String queryInJava = new String(Files.readAllBytes(Paths.get(queryFilename)), "UTF-8");

		// Create a file handler to be used throughout the recommender
		FileHandler fileHandler = new FileHandler(folderFilename);
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
		Scorer scorer = new Scorer(queryInJava, files, queryResult);
		ArrayList<Result> results = scorer.getResults();
		if (useFileHandler)
			fileHandler.writeAllResults(results);

		// Print the top 10 recommended results
		int top = 10;
		if (results.size() < 10)
			top = results.size();

		System.out.println("Top " + top + " recommended results:");
		for (int i = 0; i < top; i++)
			System.out.println(results.get(i));
		if (useFileHandler) {
			String resultString = "";
			for (Result result : results)
				resultString += result + "\n";
			fileHandler.writeFile("Results.txt", resultString);
		}
	}
}
