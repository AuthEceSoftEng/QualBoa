package Downloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.bind.DatatypeConverter;

/**
 * A downloader for files of the GitHub API.
 */
public class GitHubFileDownloader {

	/**
	 * The credentials of the user in base 64 format.
	 */
	private String credentials;

	/**
	 * The rate limit of the user.
	 */
	private int rateLimit;

	/**
	 * Creates this downloader object.
	 * 
	 * @param username the GitHub username.
	 * @param password the GitHub password.
	 */
	public GitHubFileDownloader(String username, String password) {
		credentials = DatatypeConverter.printBase64Binary((username + ":" + password).getBytes());
		// Find out rate limit, this request is free
		makeRestRequest("https://api.github.com/rate_limit");
	}

	/**
	 * Returns the rate limit of the user.
	 * 
	 * @return the rate limit of the user.
	 */
	public int getRateLimit() {
		return rateLimit;
	}

	/**
	 * Makes a GET request to the GitHub api.
	 * 
	 * @param apiURL the url to which the request is performed.
	 * @return the response of the request.
	 */
	protected String makeRestRequest(String apiURL) {
		String response = null;
		try {
			URL url = new URL(apiURL);
			// Open GET connection
			URLConnection urlc = url.openConnection();
			urlc.setRequestProperty("Content-Type", "application/json");
			urlc.setDoOutput(true);
			urlc.setAllowUserInteraction(false);
			urlc.setRequestProperty("Authorization", "Basic " + credentials);

			// Get result
			BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "UTF-8"));
			String l = null;
			while ((l = br.readLine()) != null) {
				response = l;
			}
			rateLimit = Integer.parseInt(urlc.getHeaderField("X-RateLimit-Remaining"));
			br.close();
		} catch (ConnectException e) {
			// No error handling, just print the exception
			System.out.println(e);
		} catch (IOException e) {
			// No error handling, just print the exception
			System.out.println(e);
		}
		return response;
	}

	/**
	 * Downloads a file from the GitHub api given an url.
	 * Example url: https://github.com/Ebadly/JavaTutorial/blob/master/ArrayList And HashMaps/Stack.java
	 * 
	 * @param URL the url of the file to be downloaded.
	 * @return the code of the file as a string, or null if anything fails.
	 */
	public String downloadFile(String URL) {
		// If rateLimit is lower than 10, stop downloading and return null
		if (rateLimit >= 10) {
			String apiURL = URL.replace(" ", "%20");
			apiURL = apiURL.replace("https://github.com", "https://api.github.com/repos");
			apiURL = apiURL.replace("blob/master", "contents");
			String response = makeRestRequest(apiURL);
			if (response != null) {
				response = response.split("\"content\":\"")[1].split("\"")[0].replace("\\n", "");
				byte[] encodedResponse = DatatypeConverter.parseBase64Binary(response);
				return new String(encodedResponse, StandardCharsets.UTF_8);
			}
		}
		return null;
	}
	
	public String[] splitInput(String path) throws IOException {
		String input = new String(Files.readAllBytes(Paths.get(path)), "UTF-8");
		String str_temp = "";
		int int_temp;
		
		input = input.replace("Files[] = ", "");
		String[] URL = input.split("\\n+");
		for(int i=0; i < URL.length;i++){
			str_temp = URL[i].substring(URL[i].indexOf(',')+2);
			str_temp = str_temp.substring(0,str_temp.indexOf('.'));
			int_temp = Integer.parseInt(str_temp);
			
			if (int_temp>100) URL[i]="";
			else URL[i]=URL[i].substring(0,URL[i].indexOf(','));
		}
		return URL;
	}
	
	/**
	 * This main function is used as a test function for the GitHub downloader.
	 */
	public static void main(String args[]) throws IOException {
		// Input here your GitHub username and password
		// Note that if the response code is 401, it means you provided a wrong username or password
		GitHubFileDownloader gitHubDownloader = new GitHubFileDownloader("klearchos10", "kle1992");

		String[] URL = gitHubDownloader.splitInput("Boa_output.txt");

		// Create the Files directory if it does not exist
		if (!Files.exists(Paths.get("Files")))
		    Files.createDirectories(Paths.get("Files"));
		for(int i=0; i < URL.length;i++){
			if (!URL[i].equals("")){
				String fileContents = gitHubDownloader.downloadFile(URL[i]);
				if (fileContents != null) {
					Files.write(Paths.get("Files/sourceCode"+(i+1)+".java"), fileContents.getBytes());
				}
			}
		}
		System.out.println("done");
	}
}