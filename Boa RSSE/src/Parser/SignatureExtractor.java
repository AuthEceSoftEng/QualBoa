package Parser;

import astextractor.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SignatureExtractor {
	private String ast;
	private String className;
	private String methodNames;
	private String methodTypes;
	private String hasBlock;
	
	//public SignatureExtractor(){};

	public SignatureExtractor(String path) throws IOException{
		ast = ASTExtractor.parseFile(path);
		Files.write(Paths.get("AST_XML.xml"), ast.getBytes());
		className = "";
		methodNames = "";
		methodTypes = "";
		hasBlock = "";
		extractClassName();
		extractMethodNames(null);
		extractMethodTypes(null);
		System.out.println(className);
		System.out.println(methodNames);
		System.out.println(methodTypes);
		System.out.println(hasBlock);
	}
	
	public SignatureExtractor(String path,String targetClassName) throws IOException{
		ast = ASTExtractor.parseFile(path);
		Files.write(Paths.get("AST_XML.xml"), ast.getBytes());
		className = "";
		methodNames = "";
		methodTypes = "";
		hasBlock = "";
		if (targetClassName==null || targetClassName=="-1"){
			extractClassName();
			extractMethodNames(null);
			extractMethodTypes(null);
		}else{
			extractClassName(targetClassName);
			extractMethodNames(targetClassName);
			extractMethodTypes(targetClassName);
		}
		System.out.println(className);
		System.out.println(methodNames);
		System.out.println(methodTypes);
		System.out.println(hasBlock);
	}
	
	public static void main(String[] args) throws IOException{
		//test
		//SignatureExtractor("input.java");
		//SignatureExtractor signature2  = new SignatureExtractor("Files/sourceCode113.java",signature.getClassName());
	}
	public String getClassName() {return className;}
	public String getMethodNames() {return methodNames;}
	public String getMethodTypes() {return methodTypes;}
	public String getBlock() {return hasBlock;}
	
	public void extractClassName(){
		String temp[] = ast.split("<TypeDeclaration>");
		
		for (int i=1;i<temp.length;i++){
			if (temp[i].matches("(?s)(.*)<MethodDeclaration>(.*)"))
				temp[i] = temp[i].substring(0,temp[i].indexOf("<MethodDeclaration>"));
			if (temp[i].matches("(?s)(.*)<FieldDeclaration>(.*)"))
				temp[i] = temp[i].substring(0,temp[i].indexOf("<FieldDeclaration>"));
			if (temp[i].matches("(?s)(.*)<Javadoc>(.*)"))
				temp[i] = temp[i].substring(temp[i].indexOf("</Javadoc>"));
			if (temp[i].matches("(?s)(.*)<MarkerAnnotation>(.*)"))
				temp[i] = temp[i].substring(temp[i].indexOf("</MarkerAnnotation>"));
			temp[i] = temp[i].substring(temp[i].indexOf("<SimpleName>")+12,temp[i].indexOf("</SimpleName>"));
			if (temp[i].matches("_")) temp[i]="-1";
			
			if (i==1) className = "\""+temp[i]+"\"";
			else className += ",\""+temp[i]+"\"";
		}
	}
	
	public void extractClassName(String targetClassName){
		String temp[] = ast.split("<TypeDeclaration>");
		targetClassName = targetClassName.replace("\"","");
		
		for (int i=1;i<temp.length;i++){
			if (temp[i].matches("(?s)(.*)<MethodDeclaration>(.*)"))
				temp[i] = temp[i].substring(0,temp[i].indexOf("<MethodDeclaration>"));
			if (temp[i].matches("(?s)(.*)<FieldDeclaration>(.*)"))
				temp[i] = temp[i].substring(0,temp[i].indexOf("<FieldDeclaration>"));
			if (temp[i].matches("(?s)(.*)<Javadoc>(.*)"))
				temp[i] = temp[i].substring(temp[i].indexOf("</Javadoc>"));
			if (temp[i].matches("(?s)(.*)<MarkerAnnotation>(.*)"))
				temp[i] = temp[i].substring(temp[i].indexOf("</MarkerAnnotation>"));
			if (temp[i].matches("(?s)(.*)<SingleMemberAnnotation>(.*)"))
				temp[i] = temp[i].substring(temp[i].indexOf("</SingleMemberAnnotation>"));
			temp[i] = temp[i].substring(temp[i].indexOf("<SimpleName>")+12,temp[i].indexOf("</SimpleName>"));
			if (temp[i].toLowerCase().matches("(?s)(.*)"+targetClassName.toLowerCase()+"(.*)")) className = "\""+temp[i]+"\"";
		}
	}
	
		
	public void extractMethodNames(String targetClassName){
		String temp1 ="";
		if (targetClassName !=null){
			String temp[] = ast.split("<TypeDeclaration>");
			targetClassName = targetClassName.replace("\"","");
		
			for (int i=1;i<temp.length;i++){
				if (temp[i].matches("(?s)(.*)<MethodDeclaration>(.*)"))
					temp[i] = temp[i].substring(0,temp[i].indexOf("<MethodDeclaration>"));
				if (temp[i].matches("(?s)(.*)<FieldDeclaration>(.*)"))
					temp[i] = temp[i].substring(0,temp[i].indexOf("<FieldDeclaration>"));
				if (temp[i].matches("(?s)(.*)<Javadoc>(.*)"))
					temp[i] = temp[i].substring(temp[i].indexOf("</Javadoc>"));
				if (temp[i].matches("(?s)(.*)<MarkerAnnotation>(.*)"))
					temp[i] = temp[i].substring(temp[i].indexOf("</MarkerAnnotation>"));
				if (temp[i].matches("(?s)(.*)<SingleMemberAnnotation>(.*)"))
					temp[i] = temp[i].substring(temp[i].indexOf("</SingleMemberAnnotation>"));
				temp[i] = temp[i].substring(temp[i].indexOf("<SimpleName>")+12,temp[i].indexOf("</SimpleName>"));
				if (temp[i].toLowerCase().matches("(?s)(.*)"+targetClassName.toLowerCase()+"(.*)")){
					String temp2[] = ast.split("<TypeDeclaration>");
					temp1 = temp2[i];
				}
			}
		}else temp1 = ast;
		
		String temp[] = temp1.split("<MethodDeclaration>");
		
		for (int i=1;i<temp.length;i++){
			String block = "no";
			if (temp[i].matches("(?s)(.*)<Block>(.*)")){
				block = hasTrueBlock(temp[i]);
				temp[i] = temp[i].substring(0,temp[i].indexOf("<Block>"));
			}
			if (temp[i].matches("(?s)(.*)<Javadoc>(.*)"))
				temp[i] = temp[i].substring(temp[i].indexOf("</Javadoc>"));
			if (temp[i].matches("(?s)(.*)<SingleVariableDeclaration>(.*)"))
				temp[i] = temp[i].substring(0,temp[i].indexOf("<SingleVariableDeclaration>"));
			if (temp[i].matches("(?s)(.*)<ParameterizedType>(.*)"))
				temp[i] = temp[i].substring(temp[i].indexOf("</ParameterizedType>"));
			if (temp[i].matches("(?s)(.*)<SimpleType>(.*)"))
				temp[i] = temp[i].substring(temp[i].indexOf("</SimpleType>"));
			temp[i] = temp[i].substring(temp[i].indexOf("<SimpleName>")+12,temp[i].indexOf("</SimpleName>"));
			
			if (i==1){
				methodNames = "\""+temp[i]+"\"";
				hasBlock = block;
			}
			else{
				methodNames += ",\""+temp[i]+"\"";
				hasBlock += ","+block;
			}
		}
	}
	
	public void extractMethodTypes(String targetClassName){
		String temp1 ="";
		if (targetClassName !=null){
			String temp[] = ast.split("<TypeDeclaration>");
			targetClassName = targetClassName.replace("\"","");
		
			for (int i=1;i<temp.length;i++){
				if (temp[i].matches("(?s)(.*)<MethodDeclaration>(.*)"))
					temp[i] = temp[i].substring(0,temp[i].indexOf("<MethodDeclaration>"));
				if (temp[i].matches("(?s)(.*)<FieldDeclaration>(.*)"))
					temp[i] = temp[i].substring(0,temp[i].indexOf("<FieldDeclaration>"));
				if (temp[i].matches("(?s)(.*)<Javadoc>(.*)"))
					temp[i] = temp[i].substring(temp[i].indexOf("</Javadoc>"));
				if (temp[i].matches("(?s)(.*)<MarkerAnnotation>(.*)"))
					temp[i] = temp[i].substring(temp[i].indexOf("</MarkerAnnotation>"));
				if (temp[i].matches("(?s)(.*)<SingleMemberAnnotation>(.*)"))
					temp[i] = temp[i].substring(temp[i].indexOf("</SingleMemberAnnotation>"));
				temp[i] = temp[i].substring(temp[i].indexOf("<SimpleName>")+12,temp[i].indexOf("</SimpleName>"));
				if (temp[i].toLowerCase().matches("(?s)(.*)"+targetClassName.toLowerCase()+"(.*)")){
					String temp2[] = ast.split("<TypeDeclaration>");
					temp1 = temp2[i];
				}
			}
		}else temp1 = ast;
		
		String temp[] = temp1.split("<MethodDeclaration>");

		for (int i=1;i<temp.length;i++){
			if (temp[i].matches("(?s)(.*)<Javadoc>(.*)"))
				temp[i] = temp[i].substring(temp[i].indexOf("</Javadoc>"));
			if (temp[i].matches("(?s)(.*)<Block>(.*)"))
				temp[i] = temp[i].substring(0,temp[i].indexOf("<Block>"));
			if (temp[i].matches("(?s)(.*)<SingleVariableDeclaration>(.*)"))
				temp[i] = temp[i].substring(0,temp[i].indexOf("<SingleVariableDeclaration>"));
			if (temp[i].matches("(?s)(.*)<PrimitiveType>(.*)"))
				temp[i] = temp[i].substring(temp[i].indexOf("<PrimitiveType>")+15,temp[i].indexOf("</PrimitiveType>"));
			else if (temp[i].matches("(?s)(.*)<SimpleType>(.*)"))
				temp[i] = temp[i].substring(temp[i].indexOf("<SimpleName>")+12,temp[i].indexOf("</SimpleName>"));
			else temp[i]="-1";
			
			if (temp[i].matches("_")) temp[i]="-1";
			
			if (i==1) methodTypes = "\""+temp[i]+"\"";
			else methodTypes += ",\""+temp[i]+"\"";
		}
	}
	public String hasTrueBlock(String text){
		text = text.substring(text.indexOf("<Block>")+7,text.indexOf("</Block>"));
		String[] temp = text.trim().split("\\n+");
		if(temp.length<5) return "no";
		else return "yes";
	}
}
