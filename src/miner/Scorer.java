package miner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

	class PairWithScore {
		int index1;
		int index2;
		double score;
		public PairWithScore(int index1, int index2, double score) {
			this.index1 = index1;
			this.index2 = index2;
			this.score = score;
		}
	}

	public double[] methodsScore(Signature inputSignature, Signature outputSignature) {
		String[] inputMethodsName = inputSignature.getMethodNames().replace("\"", "").split(",");
		String[] inputMethodsType = inputSignature.getMethodTypes().replace("\"", "").split(",");
		String[] outputMethodsName = outputSignature.getMethodNames().replace("\"", "").split(",");
		String[] outputMethodsType = outputSignature.getMethodTypes().replace("\"", "").split(",");
		String[][] inputMethodArgs = inputSignature.getMethodArgs();
		String[][] outputMethodArgs = outputSignature.getMethodArgs();
		ArrayList<PairWithScore> methodPairs = new ArrayList<Scorer.PairWithScore>();

		for (int i = 0; i < inputMethodsName.length; i++) {
			for (int j = 0; j < outputMethodsName.length; j++) {
				double score = calculateMethodsScore(inputMethodsName[i], outputMethodsName[j], inputMethodsType[i],
						outputMethodsType[j], inputMethodArgs[i], outputMethodArgs[j]);
				methodPairs.add(new PairWithScore(i, j, score));
			}
		}
		Collections.sort(methodPairs, new Comparator<PairWithScore>(){
			public int compare(PairWithScore o1, PairWithScore o2) {
				if (o1.score == o2.score)
					return 0;
				return o1.score < o2.score ? 1 : -1;
			}
		});

		HashSet<Integer> matchedList1 = new HashSet<Integer>(); 
 		HashSet<Integer> matchedList2 = new HashSet<Integer>(); 
 		List<PairWithScore> selectedMethodPairs = new ArrayList<PairWithScore>(); 
 		for (PairWithScore methodPair : methodPairs) { 
			if (!(matchedList1.contains(methodPair.index1) || matchedList2.contains(methodPair.index2))) {
 				selectedMethodPairs.add(methodPair); 
 				matchedList1.add(methodPair.index1); 
 				matchedList2.add(methodPair.index2); 
 			} 
 		} 
		double[] methodScore = new double[inputMethodsName.length];
 		for (PairWithScore pairWithScore : selectedMethodPairs) {
			methodScore[pairWithScore.index1] = pairWithScore.score;
		}

		// Test the results
		// System.out.println("method score: " + Arrays.toString(methodScore));

		return methodScore;
	}

	private double calculateMethodsScore(String inputMethodName, String outputMethodName, String inputMethodType,
			String outputMethodType, String[] inputMethodArgs, String[] outputMethodArgs) {
		double[] methodVector = new double[2 + inputMethodArgs.length];
		double[] defaultVector = new double[2 + inputMethodArgs.length];
		Arrays.fill(defaultVector, 1);
		for (int k = 0; k < methodVector.length; k++)
			methodVector[k] = 0;

		String[] inputMethodTokens = tokenizeString(inputMethodName);
		String[] outputMethodTokens = tokenizeString(outputMethodName);

		methodVector[0] = JaccardCoefficient.similarity(inputMethodTokens, outputMethodTokens);
		if (inputMethodType.trim().toLowerCase().equals(outputMethodType.trim().toLowerCase()))
			methodVector[1] = 1;
		else
			methodVector[1] = 0;
		if (inputMethodArgs.length > 0){
			double[] argsScore = calculateArgsScore(inputMethodArgs, outputMethodArgs);
			for (int i = 0; i < argsScore.length; i++)
				methodVector[i + 2] = argsScore[i];
		}
		return TanimotoCoefficient.similarity(methodVector, defaultVector);
	}

	private double[] calculateArgsScore(String[] inputArgs, String[] outputArgs) {
		ArrayList<PairWithScore> argPairs = new ArrayList<Scorer.PairWithScore>();
		for (int i = 0; i < inputArgs.length; i++) {
			for (int j = 0; j < outputArgs.length; j++) {
				String[] inputArgTokens = tokenizeString(inputArgs[i]);
				String[] outputArgTokens = tokenizeString(outputArgs[j]);
				double score = JaccardCoefficient.similarity(inputArgTokens, outputArgTokens);
				argPairs.add(new PairWithScore(i, j, score));
			}
		}
		Collections.sort(argPairs, new Comparator<PairWithScore>(){
			public int compare(PairWithScore o1, PairWithScore o2) {
				if (o1.score == o2.score)
					return 0;
				return o1.score < o2.score ? 1 : -1;
			}
		});
		HashSet<Integer> matchedList1 = new HashSet<Integer>(); 
 		HashSet<Integer> matchedList2 = new HashSet<Integer>(); 
 		List<PairWithScore> selectedArgPairs = new ArrayList<PairWithScore>(); 
 		for (PairWithScore argPair : argPairs) { 
			if (!(matchedList1.contains(argPair.index1) || matchedList2.contains(argPair.index2))) {
 				selectedArgPairs.add(argPair); 
 				matchedList1.add(argPair.index1); 
 				matchedList2.add(argPair.index2); 
 			} 
 		} 
		double[] argScore = new double[inputArgs.length];
 		for (PairWithScore pairWithScore : selectedArgPairs) {
 			argScore[pairWithScore.index1] = pairWithScore.score;
		}
		return argScore;
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
