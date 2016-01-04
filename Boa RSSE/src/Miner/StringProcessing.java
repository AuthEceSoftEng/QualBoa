package Miner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StringProcessing {

	private static String stopWords[];

	/**
	 * Static initializer for the stopwords. Any stopwords are placed in the stopwords.txt
	 * file. If the file cannot be found, an empty list is used.
	 */
	static {
		String input;
		try {
			input = new String(Files.readAllBytes(Paths.get("stopwords.txt")), "UTF-8");
			stopWords = input.split("\\n+");
		} catch (IOException e) {
			stopWords = new String[0];
		}

	}

	public static String[] tokenize(String text) {
		String[] tokens = text.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
		for (int i = 0; i < tokens.length; i++)
			tokens[i] = tokens[i].toLowerCase();

		return tokens;
	}

	public static String[] removeStopWords(String[] tokens) {
		int counter = 0;

		for (int i = 0; i < stopWords.length; i++) {
			for (int j = 0; j < tokens.length; j++) {
				if (stopWords[i].trim().equals(tokens[j].trim())) {
					tokens[j] = "";
					counter++;
				}
			}
		}

		String[] finalTokens = new String[tokens.length - counter];
		int i = 0;
		for (int j = 0; j < tokens.length; j++) {
			if (!tokens[j].equals("")) {
				finalTokens[i] = tokens[j];
				i++;
			}
		}

		return finalTokens;
	}

}
