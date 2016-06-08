package miner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import database.DownloadedFile;
import database.FileHandler;
import parser.Signature;

public class Scorer {
	private ArrayList<Result> results;

	public ArrayList<Result> getResults() {
		return results;
	}

	private static void testScoringSystem() throws IOException {
		String inputContent = new String(Files.readAllBytes(Paths.get("input.java")), "UTF-8");
		Scorer scorer = new Scorer("", new ArrayList<DownloadedFile>(), "_,_,_,_,_,_,_");
		String content = new String(Files.readAllBytes(Paths.get("input2.java")), "UTF-8");

		Signature inputSignature = new Signature(inputContent);
		Signature outputSignature = new Signature(content, inputSignature.getClassName());

		double score = scorer.calculateScore(inputSignature, outputSignature);
		System.out.println(score);
		System.exit(0);
	}
	
	public static void main(String[] args) throws Exception {
		// Uncomment the following line to test the scoring system on two files
		// testScoringSystem();

		// testing
		String inputContent = new String(Files.readAllBytes(Paths.get("input.java")), "UTF-8");
		FileHandler fileHandler = new FileHandler("Files");
		Scorer scorer = new Scorer(inputContent, fileHandler.readAllDownloadedFiles(),
				fileHandler.readFile("Boa_output.txt"));
		ArrayList<Result> results = scorer.getResults();
		fileHandler.writeAllResults(results);
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

		// Test the results
		// System.out.println("score vector: "+Arrays.toString(scoreVector));
		// System.out.println("total score: "+score+"\n");

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

		// Test the results
		// System.out.println("inputClass name: " + inputClass);
		// System.out.println("outputClass name: " + outputClass);
		// System.out.println("class name score: " + score);
		return score;
	}

	public double[] methodsScore(Signature inputSignature, Signature outputSignature) {
		String[] inputMethodsName = inputSignature.getMethodNames().replace("\"", "").split(",");
		String[] inputMethodsType = inputSignature.getMethodTypes().replace("\"", "").split(",");
		String[] outputMethodsName = outputSignature.getMethodNames().replace("\"", "").split(",");
		String[] outputMethodsType = outputSignature.getMethodTypes().replace("\"", "").split(",");
		String[][] inputMethodArgs = inputSignature.getMethodArgs();
		String[][] outputMethodArgs = outputSignature.getMethodArgs();
		String[] hasBlock = outputSignature.getBlock().split(",");

		double[] methodScore = new double[inputMethodsName.length];

		// definition of methodVector = [nameScore, typeScore, argScore]
		double[] methodVector = { 0,0,0 };
		double[] defaultVector = { 1,1,1 };

		for (int i = 0; i < inputMethodsName.length; i++) {
			methodScore[i] = 0;
		}
		// find the max method score for each method
		for (int i = 0; i < inputMethodsName.length; i++) {
			for (int j = 0; j < outputMethodsName.length; j++) {
				double jaccard = 0;
				double tanimoto = 0;
				for (int k = 0; k < methodVector.length; k++)
					methodVector[k] = 0;

				String[] inputMethodTokens = tokenizeString(inputMethodsName[i]);
				String[] outputMethodTokens = tokenizeString(outputMethodsName[j]);

				jaccard = JaccardCoefficient.similarity(inputMethodTokens, outputMethodTokens);
				if (hasBlock[j].trim().equals("yes") && jaccard > 0) {
					methodVector[0] = jaccard;
					if (inputMethodsType[i].trim().equals(outputMethodsType[j]))
						methodVector[1] = 1;
					methodVector[2] = calculateArgsScore(inputMethodArgs,i,outputMethodArgs,j);
				}
				tanimoto = TanimotoCoefficient.similarity(methodVector, defaultVector);
				if (tanimoto > methodScore[i]) {
					methodScore[i] = tanimoto;
				}
			}
		}
		// Test the results
		// System.out.println("method score: " + Arrays.toString(methodScore));

		return methodScore;
	}

	private double calculateArgsScore(String[][] inputMethodArgs, int a, String[][] outputMethodArgs, int b) {
		int counter = 0;
		String[][] inputArgs = inputMethodArgs;
		String[][] outputArgs = outputMethodArgs;

		for (int i = 0; i < inputArgs[a].length; i++) {
			for (int j = 0; j < outputArgs[b].length; j++) {
				if (inputArgs[a][i].equals(outputArgs[b][j])){
					counter++;
					outputArgs[b][j] = null;
				}
			}
		}

		double score;
		if (inputArgs[a].length < outputArgs[b].length) {
			score = (double) counter / outputArgs[b].length;
		} else if (inputArgs[a].length > outputArgs[b].length) {
			score = (double) counter / inputArgs[a].length;
		} else {
			if (inputArgs[a].length == 0)
				score = 1;
			else
				score = (double) counter / inputArgs[a].length;
		}
		return score;
	}

	public String[] tokenizeString(String text) {

		// Split camelCase or "_"
		String[] tokens = text.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])|[_]");
		for (int i = 0; i < tokens.length; i++)
			tokens[i] = tokens[i].toLowerCase();

		// Remove any empty values
		List<String> list = new ArrayList<>();
		Collections.addAll(list, tokens);
		list.removeAll(Arrays.asList(""));
		tokens = list.toArray(new String[list.size()]);
		return tokens;
	}
}
