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

	public BoaClientDownloader(String boaUser, String boaPass){
		boaUsername=boaUser;
		boaPassword=boaPass;
	}
	
	public static void main(String[] args) throws BoaException, IOException {
		//test this class
		BoaClientDownloader boaDownloader = new BoaClientDownloader(PropertiesHandler.BoaUsername, PropertiesHandler.BoaPassword);
		boaDownloader.runQuery("input.java");
	}
	public void runQuery(String path) throws BoaException, IOException {
		// get the signature of the file
		String content = new String(Files.readAllBytes(Paths.get(path)), "UTF-8");
		Signature signature  = new Signature(content);
		
		String className = signature.getClassName();
		String methodNames = signature.getMethodNames();
		String methodTypes = signature.getMethodTypes();
		
		String query = Queries.query(className,methodNames,methodTypes);
		Files.write(Paths.get("Query.txt"), query.getBytes());
		
		try (final BoaClient client = new BoaClient()) {
			client.login(boaUsername,boaPassword);	
			try{
				// print all available input datasets
				//for (final InputHandle d : client.getDatasets())
				//	System.out.println(d);
				
				InputHandle d = client.getDataset("2015 September/GitHub (small)");									
				JobHandle JobOutput = client.query(query,d);
				String output = waitAndGetOutput(JobOutput);
				
				Files.write(Paths.get("Boa_output.txt"), output.getBytes());
				System.out.println("done");
			}
			catch (LoginException e){
				e.printStackTrace();
			}
		}
	}
	
	public String waitAndGetOutput(JobHandle JobOutput){
		String output = new String();
		try{
			while (true){
				JobOutput.refresh();
				if (JobOutput.getExecutionStatus().equals(ExecutionStatus.FINISHED))
					break;
			}
			output = JobOutput.getOutput();
		}
		catch (NotLoggedInException e){
			e.printStackTrace();
		}
		catch (BoaException e){
			e.printStackTrace();
		}
		return output;
	}
}