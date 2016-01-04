package Miner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import Database.DownloadedFile;
import Database.FileHandler;
import Parser.Signature;

import model.LOCcounter;

public class Scorer {
	private ArrayList<Result> results;

	public ArrayList<Result> getResults() {
		return results;
	}

	public static void main(String[] args) throws Exception {
		// testing
		Scorer scorer = new Scorer(new FileHandler("Files"), new FileHandler("Final_Files"));
		ArrayList<Result> results = scorer.getResults();
		// print top 10
		System.out.println("Top 10 recommended results: \n");
		for (int i = 0; i < 10; i++)
			System.out.println("\n" + (i + 1) + "." + "\n\n" + results.get(i));
	}

	public Scorer(FileHandler fileHandler, FileHandler resultFileHandler) throws Exception {
		this(fileHandler.readAllFiles(), resultFileHandler);
	}

	public Scorer(ArrayList<DownloadedFile> files, FileHandler resultFileHandler) throws IOException {
		ArrayList<Result> fileContents = new ArrayList<Result>();

		// for each result
		// get the signature of the file
		String inputContent = new String(Files.readAllBytes(Paths.get("input.java")), "UTF-8");
		Signature inputSignature = new Signature(inputContent);
		for (DownloadedFile file : files) {
			// get the content of the file
			String content = file.getContent();

			// extract signature
			Signature outputSignature = new Signature(content, inputSignature.getClassName());

			// calculate the score
			double score = calculateScore(inputSignature, outputSignature);

			// count the lines of code
			LOCcounter loccounter = new LOCcounter();
			float loc = (float) loccounter.getLinesInFile(new BufferedReader(new StringReader(content)));

			fileContents.add(new Result(file.getPath(), content, score, loc));
		}

		// sort the results in descending order (equalities are sorted based on LOC)
		Collections.sort(fileContents, new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				if (o1.score < o2.score)
					return 1;
				else if (o1.score > o2.score)
					return -1;
				else
					return o1.loc < o2.loc ? -1 : o1.loc > o2.loc ? 1 : 0;
			}
		});
		results = fileContents;
		if (resultFileHandler != null) {
			for (int i = 0; i < results.size(); i++) {
				Result result = results.get(i);
				resultFileHandler.writeFile("result" + i + ".java", result);
			}
		}
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

		String[] inputClassTokens = stringProcess(inputClass);
		String[] outputClassTokens = stringProcess(outputClass);

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

		// for (int i=0;i<inputMethodsName.length;i++) inputMethodsName[i] = stringProcess(inputMethodsName[i]);
		// for (int i=0;i<outputMethodsName.length;i++) outputMethodsName[i] = stringProcess(outputMethodsName[i]);

		double[] scoreMethodsName = new double[inputMethodsName.length];
		double[] scoreMethodsType = new double[inputMethodsName.length];
		for (int i = 0; i < inputMethodsName.length; i++) {
			scoreMethodsName[i] = 0;
			scoreMethodsType[i] = 0;
		}

		for (int i = 0; i < inputMethodsName.length; i++) {
			for (int j = 0; j < outputMethodsName.length; j++) {
				String[] inputMethodTokens = stringProcess(inputMethodsName[i]);
				String[] outputMethodTokens = stringProcess(outputMethodsName[j]);
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
								if (JaccardCoefficient.similarity(inputMethodTokens,
										outputMethodTokens) > scoreMethodsName[i]) {
									scoreMethodsName[i] = JaccardCoefficient.similarity(inputMethodTokens,
											outputMethodTokens);
								}
							}
						} else {
							if (inputMethodsType[i].equals(outputMethodsType[j])) {
								if (JaccardCoefficient.similarity(inputMethodTokens,
										outputMethodTokens) > scoreMethodsName[i]) {
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

	public String[] stringProcess(String text) {
		String[] tokens = StringProcessing.tokenize(text);
		tokens = StringProcessing.removeStopWords(tokens);

		return tokens;
	}

}