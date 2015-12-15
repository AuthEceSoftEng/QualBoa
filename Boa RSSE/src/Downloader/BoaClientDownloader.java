package Downloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import Main.PropertiesHandler;
import Parser.SignatureExtractor;
import edu.iastate.cs.boa.*;

public class BoaClientDownloader {

	public static void main(String[] args) throws BoaException, IOException {

		//Test Query
		SignatureExtractor signature  = new SignatureExtractor("input.java");
		
		String className = signature.getClassName();
		String methodNames = signature.getMethodNames();
		String methodTypes = signature.getMethodTypes();
		
		//Files.write(Paths.get("inputClassName.txt"), className.getBytes());
		//Files.write(Paths.get("inputMethodNames.txt"), methodNames.getBytes());
		//Files.write(Paths.get("inputMethodTypes.txt"), methodTypes.getBytes());
		
		String query = Queries.query_two(className,methodNames,methodTypes);
		Files.write(Paths.get("Query.txt"), query.getBytes());
		
		try (final BoaClient client = new BoaClient()) {
			client.login(PropertiesHandler.BoaUsername,PropertiesHandler.BoaPassword);	
			try{
				// print all available input datasets
				//for (final InputHandle d : client.getDatasets())
				//	System.out.println(d);
				
				InputHandle d = client.getDataset("2015 September/GitHub (medium)");									
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
	
	public static String waitAndGetOutput(JobHandle JobOutput){
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