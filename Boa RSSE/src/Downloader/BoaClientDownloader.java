package Downloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import Main.PropertiesHandler;
import Parser.Signature;
import edu.iastate.cs.boa.*;

public class BoaClientDownloader {
	private String boaUsername;
	private String boaPassword;

	public BoaClientDownloader(String boaUser, String boaPass) {
		boaUsername = boaUser;
		boaPassword = boaPass;
	}

	public static void main(String[] args) throws BoaException, IOException {
		// test this class
		BoaClientDownloader boaDownloader = new BoaClientDownloader(PropertiesHandler.BoaUsername,
				PropertiesHandler.BoaPassword);
		String query = boaDownloader.createQuery(new String(Files.readAllBytes(Paths.get("input.java")), "UTF-8"));
		String queryResult = boaDownloader.runQuery(query);
		Files.write(Paths.get("Boa_output.txt"), queryResult.getBytes());
	}

	public String createQuery(String content) {
		Signature signature = new Signature(content);
		String className = signature.getClassName();
		String methodNames = signature.getMethodNames();
		String methodTypes = signature.getMethodTypes();
		String query = Queries.query(className, methodNames, methodTypes);
		return query;
	}

	public String runQuery(String query) throws BoaException {
		String output = "";
		try (final BoaClient client = new BoaClient()) {
			client.login(boaUsername, boaPassword);
			try {
				// print all available input datasets
				// for (final InputHandle d : client.getDatasets())
				// System.out.println(d);

				InputHandle d = client.getDataset("2015 September/GitHub (small)");
				JobHandle JobOutput = client.query(query, d);
				output = waitAndGetOutput(JobOutput);

				// System.out.println("done");
			} catch (LoginException e) {
				e.printStackTrace();
			}
		}
		return output;
	}

	public String waitAndGetOutput(JobHandle JobOutput) {
		String output = new String();
		try {
			while (true) {
				JobOutput.refresh();
				if (JobOutput.getExecutionStatus().equals(ExecutionStatus.FINISHED))
					break;
			}
			output = JobOutput.getOutput();
		} catch (NotLoggedInException e) {
			e.printStackTrace();
		} catch (BoaException e) {
			e.printStackTrace();
		}
		return output;
	}
}