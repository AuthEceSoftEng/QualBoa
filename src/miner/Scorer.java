package miner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import database.DownloadedFile;
import database.FileHandler;
import parser.Signature;

public class Scorer {
	private ArrayList<Result> results;

	public ArrayList<Result> getResults() {
		return results;
	}

	public static void main(String[] args) throws Exception {
		// testing
		String inputContent = new String(Files.readAllBytes(Paths.get("input.java")), "UTF-8");
		FileHandler fileHandler = new FileHandler("Files");
		Scorer scorer = new Scorer(inputContent, fileHandler.readAllDownloadedFiles(),
				fileHandler.readFile("Boa_output.txt"));
		ArrayList<Result> results = scorer.getResults();
		// Print the top 10 recommended results
		int top = 10;
		if (results.size() < 10)
			top = results.size();

		System.out.println("Top " + top + " recommended results:");
		for (int i = 0; i < top; i++)
			System.out.println(results.get(i));
	}

	public Scorer(String inputContent, ArrayList<DownloadedFile> files, String metricsContent) throws IOException {
		ArrayList<Result> fileContents = new ArrayList<Result>();

		// read the metrics for all results
		HashMap<String, String> metricResults = new HashMap<String, String>();
		for (String metricResult : metricsContent.split("\\n")) {
			String[] splitMetricResult = metricResult.substring(10).split(",", 2);
			metricResults.put(splitMetricResult[0], splitMetricResult[1]);
		}

		// for each result
		// get the signature of the file
		Signature inputSignature = new Signature(inputContent);
		for (DownloadedFile file : files) {
			// get the content of the file
			String content = file.getContent();

			// extract signature
			Signature outputSignature = new Signature(content, inputSignature.getClassName());

			// calculate the functional score
			double score = calculateScore(inputSignature, outputSignature);

			// find the relevant metrics
			Metrics metrics = new Metrics(metricResults.get(file.getPath()));

			// calculate the reusability index
			double qualityScore = metrics.calculateQualityScore(metrics);

			fileContents.add(new Result(file.getPath(), content, score, metrics, qualityScore));
		}

		// sort the results in descending order (equalities are sorted based on quality score)
		Collections.sort(fileContents, new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				if (o1.score < o2.score)
					return 1;
				else if (o1.score > o2.score)
					return -1;
				else
					return o1.qualityScore < o2.qualityScore ? 1 : (o1.qualityScore > o2.qualityScore ? -1 : 0);
			}
		});
		results = fileContents;
	}

	public float calculateScore(Signature inputSignature, Signature outputSignature) {
		double classScore = classNameScore(inputSignature.getClassName(), outputSignature.getClassName());
		double[] methodScore = methodsScore(inputSignature, outputSignature);
		double[] scoreVector = new double[methodScore.length + 1];
		double[] defaultVector = new double[methodScore.length + 1];

		scoreVector[0] = classScore;
		for (int i = 0; i < methodScore.length; i++)
			scoreVector[i + 1] = methodScore[i];
		for (int i = 0; i < defaultVector.length; i++)
			defaultVector[i] = 1;

		float score = TanimotoCoefficient.similarity(defaultVector, scoreVector);

		// test results
		// System.out.println(Arrays.toString(defaultVector));
		// System.out.println(Arrays.toString(scoreVector));
		// System.out.println(score);

		return score;
	}

	public double classNameScore(String inputClass, String outputClass) {
		inputClass = inputClass.replace("\"", "");
		outputClass = outputClass.replace("\"", "");

		if (inputClass.trim().equals("-1"))
			return 1.0;

		String[] inputClassTokens = tokenizeString(inputClass);
		String[] outputClassTokens = tokenizeString(outputClass);

		double score = JaccardCoefficient.similarity(inputClassTokens, outputClassTokens);

		// test results
		// System.out.println("final:" + inputClass);
		// System.out.println("final:" + outputClass);
		// System.out.println(score);
		return score;
	}

	public double[] methodsScore(Signature inputSignature, Signature outputSignature) {
		String inputMethodName = inputSignature.getMethodNames();
		String inputMethodType = inputSignature.getMethodTypes();
		String outputMethodName = outputSignature.getMethodNames();
		String outputMethodType = outputSignature.getMethodTypes();
		String block = outputSignature.getBlock();

		inputMethodName = inputMethodName.replace("\"", "");
		inputMethodType = inputMethodType.replace("\"", "");
		outputMethodName = outputMethodName.replace("\"", "");
		outputMethodType = outputMethodType.replace("\"", "");

		String[] inputMethodsName = inputMethodName.split(",");
		String[] inputMethodsType = inputMethodType.split(",");
		String[] outputMethodsName = outputMethodName.split(",");
		String[] outputMethodsType = outputMethodType.split(",");
		String[] hasBlock = block.split(",");

		double[] scoreMethodsName = new double[inputMethodsName.length];
		double[] scoreMethodsType = new double[inputMethodsName.length];
		for (int i = 0; i < inputMethodsName.length; i++) {
			scoreMethodsName[i] = 0;
			scoreMethodsType[i] = 0;
		}

		for (int i = 0; i < inputMethodsName.length; i++) {
			for (int j = 0; j < outputMethodsName.length; j++) {
				String[] inputMethodTokens = tokenizeString(inputMethodsName[i]);
				String[] outputMethodTokens = tokenizeString(outputMethodsName[j]);
				if (JaccardCoefficient.similarity(inputMethodTokens, outputMethodTokens) > 0
						&& hasBlock[j].trim().equals("yes")) {
					if (scoreMethodsName[i] == 0) {
						scoreMethodsName[i] = JaccardCoefficient.similarity(inputMethodTokens, outputMethodTokens);
						if (inputMethodsType[i].equals(outputMethodsType[j]))
							scoreMethodsType[i] = 1;
					} else {
						if (scoreMethodsType[i] == 0) {
							if (inputMethodsType[i].equals(outputMethodsType[j])) {
								scoreMethodsType[i] = 1;
								scoreMethodsName[i] = JaccardCoefficient.similarity(inputMethodTokens,
										outputMethodTokens);
							} else {
								if (JaccardCoefficient.similarity(inputMethodTokens, outputMethodTokens) > scoreMethodsName[i]) {
									scoreMethodsName[i] = JaccardCoefficient.similarity(inputMethodTokens,
											outputMethodTokens);
								}
							}
						} else {
							if (inputMethodsType[i].equals(outputMethodsType[j])) {
								if (JaccardCoefficient.similarity(inputMethodTokens, outputMethodTokens) > scoreMethodsName[i]) {
									scoreMethodsName[i] = JaccardCoefficient.similarity(inputMethodTokens,
											outputMethodTokens);
								}
							}
						}
					}
				}
			}
		}

		double[] score = new double[scoreMethodsName.length];
		for (int i = 0; i < scoreMethodsName.length; i++) {
			if (scoreMethodsType[i] == 0)
				score[i] = scoreMethodsName[i] / 2;
			else
				score[i] = scoreMethodsName[i];
		}

		/*
		 * test results
		 * for (double a : scoreMethodsName) System.out.println(a);
		 * for (double a : scoreMethodsType) System.out.println(a);
		 * for (double a : score) System.out.println(a);
		 */
		return score;
	}

	public String[] tokenizeString(String text) {
		String[] tokens = text.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
		for (int i = 0; i < tokens.length; i++)
			tokens[i] = tokens[i].toLowerCase();
		return tokens;
	}
}