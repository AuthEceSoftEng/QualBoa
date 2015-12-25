package Main;


import java.io.IOException;

import Downloader.*;
import edu.iastate.cs.boa.BoaException;
import Miner.*;

public class Recommender {

	public static void main(String[] args) throws Exception, BoaException, IOException {
		// TODO: Enter here the main structure of the recommender. Something like the following:
		BoaClientDownloader boaDownloader = new BoaClientDownloader(PropertiesHandler.BoaUsername, PropertiesHandler.BoaPassword);
		boaDownloader.runQuery("input.java");
		GitHubFileDownloader gitHubDownloader = new GitHubFileDownloader(PropertiesHandler.GitHubUsername, PropertiesHandler.GitHubPassword);
		gitHubDownloader.downloadFiles("Boa_output.txt");
		Scorer scorer = new Scorer();
		String[] results = scorer.getResults();
		//print top 10
		System.out.println("Top 10 recommended results: \n");
		for (int i=0;i<10;i++)System.out.println("\n"+(i+1)+"."+"\n\n"+results[i]);
	}
}
