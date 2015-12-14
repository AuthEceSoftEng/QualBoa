package Miner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import Miner.PorterStemmer;

import Parser.SignatureExtractor;

import java.io.File;

import model.LOCcounter;

public class Scorer {
	public static void main(String[] args) throws Exception{
		//retrieve the files
		String[] path = new File("Files").list();
		String[] fileContents = new String[path.length];
		
		for (int i=0;i<path.length;i++){
			fileContents[i] = new String(Files.readAllBytes(Paths.get("Files/"+path[i])), "UTF-8");
		}
		
		//initiate a scorer object
		Scorer scorer = new Scorer();
		
		//extract Signatures
		SignatureExtractor inputSignature  = new SignatureExtractor("input.java");
		SignatureExtractor[] outputSignature = new SignatureExtractor[path.length];
		
		//calculate the score
		float[] score = new float[path.length];
		for (int i=0;i<path.length;i++){
			System.out.println(path[i]);
			outputSignature[i] = new SignatureExtractor("Files/"+path[i],inputSignature.getClassName());
			score[i] = scorer.calculateScore(inputSignature,outputSignature[i]);
		}
		System.out.println(Arrays.toString(score));
		
		//sort the results
		SortFunctions.quickSortHighToLow(fileContents,score,0,(score.length-1));
		System.out.println(Arrays.toString(score));
				
		// Create the Final_Files directory if it does not exist
		if (!Files.exists(Paths.get("Final_Files")))
		    Files.createDirectories(Paths.get("Final_Files"));
	//write the files sorted
		for (int i=0;i<fileContents.length;i++){
			Files.write(Paths.get("Final_Files/finalSourceCode"+(i+1)+".java"), fileContents[i].getBytes());
		}
	
		//post process (sort equalities based on LOC)
		scorer.postProcessor(fileContents,score);
		
		//rewrite the files after the final sorting
		for (int i=0;i<fileContents.length;i++){
			Files.write(Paths.get("Final_Files/finalSourceCode"+(i+1)+".java"), fileContents[i].getBytes());
		}
	}
	
	public float calculateScore(SignatureExtractor inputSignature,SignatureExtractor outputSignature) throws Exception{
		double classScore = stackNameScore(inputSignature.getClassName(), outputSignature.getClassName());
		double[] methodScore = methodsScore(inputSignature,outputSignature);
		double[] scoreVector = new double[methodScore.length+1];
		double[] defaultVector = new double[methodScore.length+1];
		scoreVector[0]=classScore;
		
		for(int i=0;i<methodScore.length;i++) scoreVector[i+1]=methodScore[i];
		for(int i=0;i<defaultVector.length;i++) defaultVector[i]=1;
		
		float score = TanimotoCoefficient(defaultVector,scoreVector);
		
		//test results
		System.out.println(Arrays.toString(defaultVector));
		System.out.println(Arrays.toString(scoreVector));
		System.out.println(score);
		
		return score;
	}
	
	public double stackNameScore(String inputStack, String outputStack) throws IOException{
		inputStack = inputStack.replace("\"","");
		outputStack = outputStack.replace("\"","");
		
		if (inputStack.trim().equals("-1")) return 1.0;
		
		inputStack = stringProcess(inputStack);
		outputStack = stringProcess(outputStack);
		
		double score = LevenshteinDistance(inputStack,outputStack);
		
		//test results
		System.out.println("final:"+inputStack);
		System.out.println("final:"+outputStack);
		//System.out.println(score);
		return score;
	}
	public double[] methodsScore(SignatureExtractor inputSignature,SignatureExtractor outputSignature) throws IOException{
		String inputMethodName = inputSignature.getMethodNames();
		String inputMethodType = inputSignature.getMethodTypes();
		String outputMethodName = outputSignature.getMethodNames();
		String outputMethodType = outputSignature.getMethodTypes();
		String block = outputSignature.getBlock();
				
		inputMethodName = inputMethodName.replace("\"","");
		inputMethodType = inputMethodType.replace("\"","");
		outputMethodName = outputMethodName.replace("\"","");
		outputMethodType = outputMethodType.replace("\"","");
		
		String[] inputMethodsName = inputMethodName.split(",");
		String[] inputMethodsType = inputMethodType.split(",");
		String[] outputMethodsName = outputMethodName.split(",");
		String[] outputMethodsType = outputMethodType.split(",");
		String[] hasBlock = block.split(",");
		
		for (int i=0;i<inputMethodsName.length;i++) inputMethodsName[i] = stringProcess(inputMethodsName[i]);
		for (int i=0;i<outputMethodsName.length;i++) outputMethodsName[i] = stringProcess(outputMethodsName[i]);
				
		double[] scoreMethodsName = new double[inputMethodsName.length];
		double[] scoreMethodsType = new double[inputMethodsName.length];
		for (int i=0;i<inputMethodsName.length;i++){
			scoreMethodsName[i]=0;
			scoreMethodsType[i]=0;
		}
		
		for (int i=0;i<inputMethodsName.length;i++){
			for (int j=0;j<outputMethodsName.length;j++){
				if (outputMethodsName[j].matches("(?s)(.*)"+inputMethodsName[i]+"(.*)") && hasBlock[j].trim().equals("yes")){
					if (scoreMethodsName[i]==0){
						scoreMethodsName[i] = LevenshteinDistance(inputMethodsName[i],outputMethodsName[j]);
						if (inputMethodsType[i].equals(outputMethodsType[j])) scoreMethodsType[i]=1;
					}else{
						if (scoreMethodsType[i]==0){
							if (inputMethodsType[i].equals(outputMethodsType[j])){
								scoreMethodsType[i]=1;
								scoreMethodsName[i] = LevenshteinDistance(inputMethodsName[i],outputMethodsName[j]);
							}else{
								if (LevenshteinDistance(inputMethodsName[i],outputMethodsName[j])>scoreMethodsName[i]){
									scoreMethodsName[i] = LevenshteinDistance(inputMethodsName[i],outputMethodsName[j]);
								}
							}
						}else{
							if (inputMethodsType[i].equals(outputMethodsType[j])){
								if (LevenshteinDistance(inputMethodsName[i],outputMethodsName[j])>scoreMethodsName[i]){
									scoreMethodsName[i] = LevenshteinDistance(inputMethodsName[i],outputMethodsName[j]);
								}
							}
						}
					}
				}
			}
		}
		
		double[] score = new double[inputMethodsName.length];
		for (int i=0;i<inputMethodsName.length;i++){
			if (scoreMethodsType[i]==0) score[i]=scoreMethodsName[i]/2;
			else score[i]=scoreMethodsName[i];
		}
		
		/* test results
		for (double a : scoreMethodsName) System.out.println(a);
		for (double a : scoreMethodsType) System.out.println(a);
		for (double a : score) System.out.println(a);
		*/
		return score;
	}
		
	public String stringProcess (String text) throws IOException{
		String[] tokens = Tokenize(text);
		tokens = removeStopWords(tokens);
		tokens = stemming(tokens);
		
		text="";
		for (int i=0;i<tokens.length;i++) text+=tokens[i];
		
		return text;
	}
	public String[] Tokenize(String text) {
		String[] tokens = text.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
		for (int i=0;i<tokens.length;i++) tokens[i]=tokens[i].toLowerCase();
		
		// test results
		//for (String a : tokens) System.out.println(a);
		
		return tokens;
	}
	
	public String[] removeStopWords(String[] tokens) throws IOException {
		String input = new String(Files.readAllBytes(Paths.get("stopwords.txt")), "UTF-8");
		String stopWords[] = input.split("\\n+");
		
		for(int i=0;i<stopWords.length;i++){
			for(int j=0;j<tokens.length;j++){
				if (stopWords[i].trim().equals(tokens[j].trim())){
					tokens[j]="";
				}
			}
		}
		
		//test results
		//for (String a : tokens) System.out.println(a);
		
		return tokens;
	}
	
	public String[] stemming (String[] terms) {
	    PorterStemmer stemmer = new PorterStemmer();
	    for(int i=0;i<terms.length;i++) terms[i]=stemmer.stem(terms[i]);
	    return terms;
	}
	
	public double LevenshteinDistance (CharSequence lhs, CharSequence rhs) {                          
	    int len0 = lhs.length() + 1;                                                     
	    int len1 = rhs.length() + 1;                                                     
	                                                                                    
	    // the array of distances                                                       
	    int[] cost = new int[len0];                                                     
	    int[] newcost = new int[len0];                                                  
	                                                                                    
	    // initial cost of skipping prefix in String s0                                 
	    for (int i = 0; i < len0; i++) cost[i] = i;                                     
	                                                                                    
	    // dynamically computing the array of distances                                  
	                                                                                    
	    // transformation cost for each letter in s1                                    
	    for (int j = 1; j < len1; j++) {                                                
	        // initial cost of skipping prefix in String s1                             
	        newcost[0] = j;                                                             
	                                                                                    
	        // transformation cost for each letter in s0                                
	        for(int i = 1; i < len0; i++) {                                             
	            // matching current letters in both strings                             
	            int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;             
	                                                                                    
	            // computing cost for each transformation                               
	            int cost_replace = cost[i - 1] + match;                                 
	            int cost_insert  = cost[i] + 1;                                         
	            int cost_delete  = newcost[i - 1] + 1;                                  
	                                                                                    
	            // keep minimum cost                                                    
	            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
	        }                                                                           
	                                                                                    
	        // swap cost/newcost arrays                                                 
	        int[] swap = cost; cost = newcost; newcost = swap;                          
	    }                                                                               
	                                                                                    
	    // the distance is the cost for transforming all letters in both strings 
	    return 1-(cost[len0-1]*1.0/(len0+len1-2));                                                          
	}
	
	public float TanimotoCoefficient(double[] features1, double[] features2) throws Exception {

        if (features1.length != features2.length) {
            throw new Exception("Features vectors must be of the same length");
        }

        int n = features1.length;
        double ab = 0.0;
        double a2 = 0.0;
        double b2 = 0.0;

        for (int i = 0; i < n; i++) {
            ab += features1[i] * features2[i];
            a2 += features1[i]*features1[i];
            b2 += features2[i]*features2[i];
        }
        return (float)ab/(float)(a2+b2-ab);
    }

	public void postProcessor(String[] fileContents,float[] score){
		LOCcounter loccounter = new LOCcounter();
		float[] loc= new float[fileContents.length];
		float[] temploc= new float[fileContents.length];
		
		for (int i=0;i<fileContents.length;i++){
			temploc[i] = (float)loccounter.getLinesInFile("Final_Files/finalSourceCode"+(i+1)+".java");
		}
		System.out.println(Arrays.toString(temploc));
		
		for (int i=0;i<fileContents.length;i++){
			int counter=0;
			for (int j=i;j<fileContents.length;j++){
				if (score[i]==score[j]){
					counter++;
					loc[j] = (float)loccounter.getLinesInFile("Final_Files/finalSourceCode"+(j+1)+".java");
				}else break;
			}
			if (counter>1){
				SortFunctions.quickSortLowToHigh(fileContents,loc, i, i+counter-1);
				i+=counter-1;
			}
		}
		
		System.out.println(Arrays.toString(loc));
	}
		
}