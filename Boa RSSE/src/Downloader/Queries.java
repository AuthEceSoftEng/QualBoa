package Downloader;

public class Queries {	
	public static String query_one(String a, String b){
		
		String query =  "p: Project = input;\n"+
						"counts: output sum of int;\n"+

						"visit(p, visitor {\n"+
						"	before n: Project -> ifall (i: int; !match(`^java$`, lowercase(n.programming_languages[i]))) stop;\n"+
						"	before node: CodeRepository ->\n"+
						"	if (node.kind == RepositoryKind."+a+")\n"+
						"		exists (j: int; yearof(node.revisions[j].commit_date) == "+b+")\n"+
						"		counts << 1;\n"+
						"});";
		return query;
	}
	public static String query_two(String class_name, String method_names, String method_types){
		
		String query = "p: Project = input;\n"+
						"Files: output top(150) of string weight float;\n"+

						"out:string;\n"+
						"cur_file: string;\n"+

						"class_name:= "+class_name+";\n"+

						"Flag1: array of bool;\n"+
						"Flag2: array of bool;\n"+
						"Flag3: array of bool;\n"+
						"method_name: array of string;\n"+
						"method_type: array of string;\n"+
						
						"method_name = {"+method_names+"};\n"+
						"method_type = {"+method_types+"};\n"+
						"length:= len(method_name);\n"+
						"Flag1 = new(Flag1,length,false);\n"+
						"Flag2 = new(Flag2,length,false);\n"+
						"Flag3 = new(Flag3,length,false);\n"+
						
						"visit(p, visitor {\n"+
						"# only look at the latest snapshot\n"+
						"	before node: CodeRepository -> {\n"+
						"		snapshot := getsnapshot(node);\n"+
						"		foreach (i: int; def(snapshot[i]))\n"+
						"			visit(snapshot[i]);\n"+
						"		stop;\n"+
						"	}\n"+
						"	before node: ChangedFile -> {\n"+
						"		cur_file = node.name;\n"+
						"	}\n"+
						"	before node: Declaration -> {\n"+
						"		if (node.kind == TypeKind.CLASS && (lowercase(node.name) == lowercase(class_name) || match(lowercase(class_name), lowercase(node.name)))){\n"+
						"			for (j:=0; j < length; j++){\n"+
						"				Flag1[j] = false;\n"+
						"				Flag2[j] = false;\n"+
						"				Flag3[j] = false;\n"+
						"			}\n"+
						"			foreach (i: int; node.methods[i]){\n"+
						"				visit(node.methods[i]);\n"+
						"			}\n"+
						"		}else{\n"+
						"			stop;\n"+
						"		}\n"+
						"	}\n"+
						"	after node: Declaration -> {\n"+
						"		exists (i: int; (Flag1[i] == true)){\n"+
						"		counter1:=0;\n"+
						"		counter2:=0;\n"+
						"		counter3:=0;\n"+
						"		for (k:=0; k < length ; k++){\n"+
						"			if (Flag1[k] == true) counter1++;\n"+
						"			if (Flag2[k] == true) counter2++;\n"+
						"			if (Flag3[k] == true) counter3++;\n"+
						"		}\n"+
						" 		if (counter1 > (length - 2)){\n"+
						"		out = format(\"%s/blob/master/%s\",p.project_url,cur_file);\n"+
						"		for (l:=0; l < length ; l++){\n"+
						"			if (counter1 == (length - l)){\n"+
						"				if (counter2>0 && counter3>0) Files << out weight 80-(30*l)-(5*counter2/length)-(5*counter3/length); \n"+
						"				else if (counter2>0) Files << out weight 100-(30*l)-(10*counter2/length);\n"+
						"				else if (counter3>0) Files << out weight 90-(30*l)-(10*counter3/length);\n"+
						"				else Files << out weight 100-(30*l); \n"+
						"			}\n"+
						"		}\n"+
						"		}\n"+
						"		}\n"+
						"	}\n"+
						"	before node: Method -> {\n"+
						"		for (i:=0; i < length ; i++){\n"+
						"			if (match(lowercase(method_name[i]), lowercase(node.name))){\n"+
						"				Flag1[i] = true;\n"+
						"				if (lowercase(method_name[i]) != lowercase(node.name)) Flag2[i] = true;\n"+
						"				if (method_type[i] == \"-1\" || node.return_type.name == method_type[i]) stop;\n"+
						"				else{\n"+
						"					Flag3[i] = true;\n"+
						"					stop;\n"+
						"				}\n"+
						"			}\n"+
						"		}\n"+
						"		stop;\n"+
						"	}\n"+
						"	before Variable, Modifier -> stop;\n"+
						"});";
		return query;
	}
}

