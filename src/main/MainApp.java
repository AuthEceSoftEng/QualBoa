package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.iastate.cs.boa.BoaException;

/**
 * Contains the main class of the application.
 * 
 * @author themis
 */
public class MainApp {

	/**
	 * Prints the help message of the command line interface.
	 */
	private static void printHelpMessage() {
		System.out.println("QualBoa: Reusability-aware Recommendations of Source Code Components\n");
		System.out.println("Run as:\n java -jar QualBoa.jar -query=\"path/to/query\" -folder=\"path/to/query\"");
		System.out.println("where query is the queried component in the form of a java interface and folder is the "
				+ "folder where the results will be downloaded");
	}

	/**
	 * Executes the application.
	 * 
	 * @param args arguments for executing in command line mode.
	 * @throws BoaException when there is an error with the connection to Boa.
	 * @throws IOException when there is an error reading the files given.
	 */
	public static void main(String args[]) throws BoaException, IOException {
		if (args.length == 2) {
			String[] arguments = parseArgs(args);
			String queryFilename = arguments[0];
			String folderFilename = arguments[1];
			if (queryFilename.length() > 0 && folderFilename.length() > 0) {
				Recommender.recommend(queryFilename, folderFilename);
			} else {
				printHelpMessage();
			}
		} else {
			printHelpMessage();
		}
	}

	/**
	 * Parses the command line arguments.
	 * 
	 * @param args the arguments to be parsed.
	 * @return a string with the values of the arguments.
	 */
	public static String[] parseArgs(String[] args) {
		List<String> col = new ArrayList<String>();
		for (String arg : args) {
			String narg = arg.trim();
			if (narg.contains("=")) {
				for (String n : narg.split("=")) {
					col.add(n);
				}
			} else
				col.add(arg.trim());
		}
		boolean isQuery = false;
		boolean isFolder = false;
		String queryFilename = "";
		String folderFilename = "";
		for (String c : col) {
			if (c.startsWith("-query")) {
				isQuery = true;
				isFolder = false;
			} else if (c.startsWith("-folder")) {
				isQuery = false;
				isFolder = true;
			} else {
				if (isQuery)
					queryFilename += c + " ";
				else if (isFolder)
					folderFilename += c + " ";
			}
		}
		queryFilename = queryFilename.trim();
		folderFilename = folderFilename.trim();
		return new String[] { queryFilename.trim(), folderFilename.trim() };
	}
}
