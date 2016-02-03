package Downloader;

public class Queries {	
	
	public static String query(String class_name, String method_names, String method_types){
		
		String query = "p: Project = input;\n"+
				"Files: output top(150) of string weight float;\n"+

				"out: string;\n"+
				"cur_file: string;\n"+
				"Flag1: array of bool;\n"+
				"Flag2: array of bool;\n"+
				"Flag3: array of bool;\n"+
				"method_name: array of string;\n"+
				"method_type: array of string;\n"+
				
				"class_name:= "+class_name+";\n"+
				"method_name = {"+method_names+"};\n"+
				"method_type = {"+method_types+"};\n"+
				"length:= len(method_name);\n"+
				"Flag1 = new(Flag1,length,false);\n"+
				"Flag2 = new(Flag2,length,false);\n"+
				"Flag3 = new(Flag3,length,false);\n"+
				
				"#metrics\n"+
				"score: float;\n"+
				"complex: float;\n"+
				"coupling: int;\n"+
				"loc: float;\n"+
				"depth: float;\n"+
				"efferent: int;\n"+
				"Decl_name: string;\n"+
				"var_type: string;\n"+
				"var_types: array of string;\n"+
				"var_types = new(var_types, 30, \"\");\n"+
				"pointer: int;\n"+
				"flag: bool;\n"+
				"var_names: array of string;\n"+
				"method1_var_access: array of bool;\n"+
				"method2_var_access: array of bool;\n"+
				"cohesion: int;\n"+
				
				"visit(p, visitor {\n"+
				"# only look at the latest snapshot\n"+
				"	before node: CodeRepository -> {\n"+
				"		snapshot := getsnapshot(node, \"SOURCE_JAVA_JLS\");\n"+
				"		foreach (i: int; def(snapshot[i]))\n"+
				"			visit(snapshot[i]);\n"+
				"		stop;\n"+
				"	}\n"+
				"	before node: ChangedFile -> {\n"+
				"		cur_file = node.name;\n"+
				"	}\n"+
				"	before node: Declaration -> {\n"+
				"		Decl_name = node.name;\n"+
				"		if (node.kind == TypeKind.CLASS && (class_name == \"-1\" || match(lowercase(class_name), lowercase(node.name)))){\n"+
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
				"	before node: Method -> {\n"+
				"		for (i:=0; i < length ; i++){\n"+
				"			if (match(lowercase(method_name[i]), lowercase(node.name))){\n"+
				"				Flag1[i] = true;\n"+
				"				Flag2[i] = false;\n"+
				"				Flag3[i] = false;\n"+
				"				if (lowercase(method_name[i]) != lowercase(node.name)) Flag2[i] = true;\n"+
				"				if (method_type[i] == \"-1\" || lowercase(node.return_type.name) == lowercase(method_type[i])) stop;\n"+
				"				else{\n"+
				"					Flag3[i] = true;\n"+
				"					stop;\n"+
				"				}\n"+
				"			}\n"+
				"		}\n"+
				"		stop;\n"+
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
				"		if (counter1 > (length - 2)){\n"+
				"		out = format(\"%s/blob/master/%s\",p.project_url,cur_file);\n"+
				"		#calculate score\n"+
				"		score=0;\n"+
				"		for (l:=0; l < (length - 1) ; l++){\n"+
				"			if (counter1 == (length - l)){\n"+
				"				if (counter2>0 && counter3>0) score = 80-(30*l)-(5*counter2/length)-(5*counter3/length);\n"+
				"				else if (counter2>0) score = 100-(30*l)-(10*counter2/length);\n"+
				"				else if (counter3>0) score = 90-(30*l)-(10*counter3/length);\n"+
				"				else score = 100-(30*l);\n"+
				"				break;\n"+
				"			}\n"+
				"		}\n"+
					
				"		#calculate complexity, coupling and LoC and part of efferent coupling\n"+
				"		complex = 0;\n"+
				"		coupling = 0;\n"+
				"		loc = 0;\n"+
				"		efferent = 0;\n"+
				"		pointer=0;\n"+
				"		foreach (a: int; var_types[a]) var_types[a]=\"\";\n"+
					
				"		foreach (a: int; def(node.methods[a])){\n"+
				"		    foreach (b: int; def(node.methods[a].statements[b])){\n"+
				"			    visit(node.methods[a].statements[b]);\n"+
				"			}\n"+
				"		}\n"+
				"		if (len(node.methods) != 0){\n"+
				"    		complex = complex/len(node.methods) + 1;\n"+
				"			loc = loc/len(node.methods);\n"+
				"   	}\n"+
				"		if (def(node.fields)) coupling = coupling + len(node.fields);\n"+
					
				"		out = format(\"%s, Average Cyclomatic Complexity = %f\",out,complex);\n"+
				"		out = format(\"%s, Coupling = %d\",out,coupling);\n"+
				"		out = format(\"%s, LOC per Method = %f\",out,loc);\n"+
				
				"		#calculate the rest efferent coupling\n"+
				"		foreach (a: int; def(node.fields[a])){\n"+
				"			var_type = node.fields[a].variable_type.name;\n"+
				"			if (var_type != \"int\" && var_type != \"int[]\" && var_type != \"float\" && var_type != \"float[]\" \n"+
				"				&& var_type != \"String\" && var_type != \"String[]\" && var_type != \"char\" && var_type != \"char[]\" \n"+
				"				&& var_type != \"long\" && var_type != \"long[]\" && var_type != \"double\" && var_type != \"double[]\" \n"+
				"				&& var_type != \"byte\" && var_type != \"byte[]\" && var_type != \"short\" && var_type != \"short[]\" \n"+
				"				&& var_type != \"boolean\" && var_type != \"boolean[]\" && var_type != node.name && var_type != (node.name + \"[]\")){\n"+
				"					flag = false;\n"+
				"					for (n:=0; n < pointer ; n++){\n"+
				"					    if (var_type == var_types[n]) flag = true;\n"+
				"					}\n"+
				"					if (flag == false){\n"+
				"					    efferent++;\n"+
				"				    	if (pointer < 30){\n"+
				"				        	var_types[pointer]=var_type;\n"+
				"					        pointer++;\n"+
				"					    }\n"+
				"					}\n"+
				"			}\n"+
				"		}\n"+
				"		out = format(\"%s, Efferent couplings = %d\",out,efferent);\n"+
				
				"		#calculate block depth\n"+
				"		max: float;\n"+
				"		sum_max: float;\n"+
				"		inner_max: float;\n"+
				"		sum_max=0;\n"+
				"		foreach (a: int; def(node.methods[a])){\n"+
				"			max=0;\n"+
				"	 		foreach (b: int; def(node.methods[a].statements[b])){\n"+
				"				depth=0;\n"+
				"	    		inner_max=0;\n"+
				"			    visit(node.methods[a].statements[b], visitor{\n"+
				"		 		   	before node:Statement -> {\n"+
				"		    			if (node.kind != StatementKind.OTHER && node.kind != StatementKind.LABEL){\n"+
				"		    				if (node.kind != StatementKind.BLOCK) depth = depth + 1;\n"+
				"		    				foreach(c: int; def(node.statements[c])){\n"+
				"		    					visit(node.statements[c]);\n"+
				"		    				}\n"+
				"		    			}\n"+
				"		    			if (depth > inner_max) inner_max = depth;\n"+
				"		    			depth = depth - 1;\n"+
				"		    			stop;\n"+
				"		    		}\n"+
				"		    	});\n"+
				"		    	if (inner_max > max) max=inner_max;\n"+
				"			}\n"+
				"			sum_max=sum_max+max;\n"+
				"		}\n"+
				"		depth = sum_max/len(node.methods);\n"+
				"		out = format(\"%s, Average Block depth = %f\",out,depth);\n"+
							
				"		#calculate cohesion in methods\n"+
				"		cohesion=0;\n"+
				"		var_names = new(var_names, len(node.fields), \"\");\n"+
				"		method1_var_access = new(method1_var_access, len(node.fields), false);\n"+
				"		method2_var_access = new(method2_var_access, len(node.fields), false);\n"+
					
				"		foreach (a: int; def(node.fields[a])){\n"+
				"			var_names[a] = node.fields[a].name;\n"+
				"		}\n"+
					
				"		for (z:=0; z < len(node.methods) - 1 ; z++){\n"+
				"			foreach (a: int; var_names[a]) method1_var_access[a] = false;\n"+
				"			foreach (a: int; def(node.methods[z].statements[a])){\n"+
				"		    	visit(node.methods[z].statements[a], visitor{\n"+
				"		    		before node:Expression -> {\n"+
				"		    			if (node.kind == ExpressionKind.VARACCESS){\n"+
				"							foreach (b: int; var_names[b]){\n"+
				"  								if (var_names[b] == node.variable)\n"+
				"									method1_var_access[b] = true;\n"+
				"	   						}\n"+
				" 	 					}\n"+
				"    					flag = false;\n"+
				"    					foreach (b: int; var_names[b]){\n"+
				"    						if (method1_var_access[b] == false)\n"+
				"    							flag = true;\n"+
				"    					}\n"+
				"    					if (flag == false) stop;\n"+
				"    				}\n"+
				"		    	});\n"+
				"			}\n"+
						
				"			for (x:= z + 1; x<len(node.methods); x++){\n"+
				"				foreach (a: int; var_names[a]) method2_var_access[a] = false;\n"+
				"				foreach (a: int; def(node.methods[x].statements[a])){\n"+
				"			    	visit(node.methods[x].statements[a], visitor{\n"+
				"		    			before node:Expression -> {\n"+
				"		    				if (node.kind == ExpressionKind.VARACCESS){\n"+
				"    							foreach (b: int; var_names[b]){\n"+
				"    								if (var_names[b] == node.variable)\n"+
				"    									method2_var_access[b] = true;\n"+
				"    							}\n"+
				"    						}\n"+
				"	    					flag = false;\n"+
				"   	 					foreach (b: int; var_names[b]){\n"+
				"   							if (method2_var_access[b] == false)\n"+
				"    								flag = true;\n"+
				"   						}\n"+
				"   						if (flag == false) stop;\n"+
				"   					}\n"+
				"		   			});\n"+
				"		   		}\n"+
				"			    flag = false;\n"+
				"			    foreach (a: int; var_names[a]){\n"+
				"			    	if (method1_var_access[a] == true && method2_var_access[a] == true){\n"+
				"		    			flag = true;\n"+
				"		    			break;\n"+
				"		 		   	}\n"+
				"		  		}\n"+
				"			    if (flag == true) cohesion = cohesion - 1;\n"+
				"		    	else cohesion = cohesion + 1;\n"+
				"			}\n"+
				"		}\n"+
				"		out = format(\"%s, Cohesion in Methods =  %d\",out,cohesion);\n"+
					
				"		#calculate public fields\n"+
				"		public_fields: int;\n"+
				"		is_public: int;\n"+
				"		public_fields=0;\n"+
				"		foreach (a: int; def(node.fields[a])){\n"+
				"			is_public=0;\n"+
				"			foreach (b: int; def(node.fields[a].modifiers[b])){\n"+
				"				if (node.fields[a].modifiers[b].kind == ModifierKind.VISIBILITY && node.fields[a].modifiers[b].visibility == Visibility.PUBLIC){\n"+
				"					is_public = 1;\n"+
				"					break;\n"+
				"				}\n"+
				"			}\n"+
				"			if (is_public == 1) public_fields++;\n"+
				"		}\n"+
				"		out = format(\"%s, Public Fields = %d\",out,public_fields);\n"+
					
				"		#calculate public methods\n"+
				"		public_methods: int;\n"+
				"		public_methods =0;\n"+
				"		foreach (a: int; def(node.methods[a])){\n"+
				"			is_public=0;\n"+
				"			foreach (b: int; def(node.methods[a].modifiers[b])){\n"+
				"				if (node.methods[a].modifiers[b].kind == ModifierKind.VISIBILITY && node.methods[a].modifiers[b].visibility == Visibility.PUBLIC){\n"+
				"					is_public = 1;\n"+
				"					break;\n"+
				"				}\n"+
				"			}\n"+
				"			if (is_public == 1) public_methods++;\n"+
				"		}\n"+
				"		out = format(\"%s, Public Methods = %d\",out,public_methods);\n"+
				
				"		#FINALLY output everything\n"+
				"		Files << out weight score;\n"+
				"		}\n"+
				"		}\n"+
				"	}\n"+
				"	before node:Statement -> {\n"+
				"		if (node.kind == StatementKind.IF || node.kind == StatementKind.WHILE || node.kind == StatementKind.FOR || node.kind == StatementKind.CASE)\n"+
				"			complex = complex + 1;\n"+
				"		if (node.kind == StatementKind.RETURN)\n"+
				"			coupling++;\n"+
				"		if (node.kind != StatementKind.OTHER && node.kind != StatementKind.LABEL && node.kind != StatementKind.BLOCK)\n"+
				"			loc = loc + 1;\n"+
				"	}\n"+
				"	before node: Expression -> {\n"+
				" 	  if (node.kind == ExpressionKind.VARDECL){\n"+
				" 	 	coupling++;\n"+
				    	
				"    	foreach (c: int; def(node.variable_decls[c])){\n"+
				"   		var_type = node.variable_decls[c].variable_type.name;\n"+
				"			if (var_type != \"int\" && var_type != \"int[]\" && var_type != \"float\" && var_type != \"float[]\" \n"+
				"				&& var_type != \"String\" && var_type != \"String[]\" && var_type != \"char\" && var_type != \"char[]\" \n"+
				"				&& var_type != \"long\" && var_type != \"long[]\" && var_type != \"double\" && var_type != \"double[]\" \n"+
				"				&& var_type != \"byte\" && var_type != \"byte[]\" && var_type != \"short\" && var_type != \"short[]\" \n"+
				"				&& var_type != \"boolean\" && var_type != \"boolean[]\" && var_type != Decl_name && var_type != (Decl_name + \"[]\")){\n"+
				"					flag = false;\n"+
				"				    for (m:=0; m < pointer ; m++){\n"+
				"			    	    if (var_type == var_types[m]) flag = true;\n"+
				"				    }\n"+
				"	    			if (flag == false){\n"+
				"		    		    efferent++;\n"+
				"			    	    if (pointer < 30){\n"+
				"				            var_types[pointer]=var_type;\n"+
				"				            pointer++;\n"+
				"				        }\n"+
				"					}\n"+
				"			}\n"+
				"   	}\n"+
				"    }\n"+
				"    stop;\n"+
				"	}\n"+
				"	before Modifier,Variable,Type -> stop;\n"+
				"});";
				
		return query;
	}
	public static String query_one_method(String class_name, String method_names, String method_types){
		
		String query = "p: Project = input;\n"+
				"Files: output top(150) of string weight float;\n"+
				"out: string;\n"+
				"cur_file: string;\n"+
				"Flag1: bool;\n"+
				"Flag2: bool;\n"+
				"Flag3: bool;\n"+
				
				"class_name:= "+class_name+";\n"+
				"method_name:= "+method_names+";\n"+
				"method_type:= "+method_types+";\n"+

				"#metrics\n"+
				"score: float;\n"+
				"complex: float;\n"+
				"coupling: int;\n"+
				"loc: float;\n"+
				"depth: float;\n"+
				"efferent: int;\n"+
				"Decl_name: string;\n"+
				"var_type: string;\n"+
				"var_types: array of string;\n"+
				"var_types = new(var_types, 30, \"\");\n"+
				"pointer: int;\n"+
				"flag: bool;\n"+
				"var_names: array of string;\n"+
				"method1_var_access: array of bool;\n"+
				"method2_var_access: array of bool;\n"+
				"cohesion: int;\n"+

				"visit(p, visitor {\n"+
				"	# only look at the latest snapshot\n"+
				"	before node: CodeRepository -> {\n"+
				"		snapshot := getsnapshot(node, \"SOURCE_JAVA_JLS\");\n"+
				"		foreach (i: int; def(snapshot[i]))\n"+
				"			visit(snapshot[i]);\n"+
				"		stop;\n"+
				"	}\n"+
				"	before node: ChangedFile -> {\n"+
				"		cur_file = node.name;\n"+
				"	}\n"+
				"	before node: Declaration -> {\n"+
				"		if (node.kind == TypeKind.CLASS && (class_name == \"-1\" || match(lowercase(class_name), lowercase(node.name)))){\n"+
				"			Flag1 = false;\n"+
				"			Flag2 = false;\n"+
				"			Flag3 = false;\n"+
				"			foreach (i: int; node.methods[i])\n"+
				"				visit(node.methods[i]);\n"+
				"		}else\n"+
				"			stop;\n"+
				"	}\n"+
				"	before node: Method -> {\n"+
				"		if (match(lowercase(method_name), lowercase(node.name))){\n"+
				"			Flag1 = true;\n"+
				"			Flag2 = false;\n"+
				"			Flag3 = false;\n"+
				"			if (lowercase(method_name) != lowercase(node.name)) Flag2 = true;\n"+
				"			if (method_type == \"-1\" || lowercase(node.return_type.name) == lowercase(method_type)) stop;\n"+
				"			else{\n"+
				"				Flag3 = true;\n"+
				"				stop;\n"+
				"			}\n"+
				"		}\n"+
				"		stop;\n"+
				"	}\n"+
				"	after node: Declaration -> {\n"+
				"		if (Flag1 == true){\n"+
				"			out = format(\"%s/blob/master/%s\",p.project_url,cur_file);\n"+
				"			#calculate score\n"+
				"			score=0;\n"+
				"			if (Flag2 == true && Flag3 == true) score = 70;\n"+
				"			else if (Flag2 == true) score = 90;\n"+
				"			else if (Flag3 == true) score = 80;\n"+
				"			else score = 100;\n"+
				
				"		#calculate complexity, coupling and LoC and part of efferent coupling\n"+
				"		complex = 0;\n"+
				"		coupling = 0;\n"+
				"		loc = 0;\n"+
				"		efferent = 0;\n"+
				"		pointer=0;\n"+
				"		foreach (a: int; var_types[a]) var_types[a]=\"\";\n"+
					
				"		foreach (a: int; def(node.methods[a])){\n"+
				"		    foreach (b: int; def(node.methods[a].statements[b])){\n"+
				"			    visit(node.methods[a].statements[b]);\n"+
				"			}\n"+
				"		}\n"+
				"		if (len(node.methods) != 0){\n"+
				"    		complex = complex/len(node.methods) + 1;\n"+
				"			loc = loc/len(node.methods);\n"+
				"   	}\n"+
				"		if (def(node.fields)) coupling = coupling + len(node.fields);\n"+
					
				"		out = format(\"%s, Average Cyclomatic Complexity = %f\",out,complex);\n"+
				"		out = format(\"%s, Coupling = %d\",out,coupling);\n"+
				"		out = format(\"%s, LOC per Method = %f\",out,loc);\n"+
				
				"		#calculate the rest efferent coupling\n"+
				"		foreach (a: int; def(node.fields[a])){\n"+
				"			var_type = node.fields[a].variable_type.name;\n"+
				"			if (var_type != \"int\" && var_type != \"int[]\" && var_type != \"float\" && var_type != \"float[]\" \n"+
				"				&& var_type != \"String\" && var_type != \"String[]\" && var_type != \"char\" && var_type != \"char[]\" \n"+
				"				&& var_type != \"long\" && var_type != \"long[]\" && var_type != \"double\" && var_type != \"double[]\" \n"+
				"				&& var_type != \"byte\" && var_type != \"byte[]\" && var_type != \"short\" && var_type != \"short[]\" \n"+
				"				&& var_type != \"boolean\" && var_type != \"boolean[]\" && var_type != node.name && var_type != (node.name + \"[]\")){\n"+
				"					flag = false;\n"+
				"					for (n:=0; n < pointer ; n++){\n"+
				"					    if (var_type == var_types[n]) flag = true;\n"+
				"					}\n"+
				"					if (flag == false){\n"+
				"					    efferent++;\n"+
				"				    	if (pointer < 30){\n"+
				"				        	var_types[pointer]=var_type;\n"+
				"					        pointer++;\n"+
				"					    }\n"+
				"					}\n"+
				"			}\n"+
				"		}\n"+
				"		out = format(\"%s, Efferent couplings = %d\",out,efferent);\n"+
				
				"		#calculate block depth\n"+
				"		max: float;\n"+
				"		sum_max: float;\n"+
				"		inner_max: float;\n"+
				"		sum_max=0;\n"+
				"		foreach (a: int; def(node.methods[a])){\n"+
				"			max=0;\n"+
				"	 		foreach (b: int; def(node.methods[a].statements[b])){\n"+
				"				depth=0;\n"+
				"	    		inner_max=0;\n"+
				"			    visit(node.methods[a].statements[b], visitor{\n"+
				"		 		   	before node:Statement -> {\n"+
				"		    			if (node.kind != StatementKind.OTHER && node.kind != StatementKind.LABEL){\n"+
				"		    				if (node.kind != StatementKind.BLOCK) depth = depth + 1;\n"+
				"		    				foreach(c: int; def(node.statements[c])){\n"+
				"		    					visit(node.statements[c]);\n"+
				"		    				}\n"+
				"		    			}\n"+
				"		    			if (depth > inner_max) inner_max = depth;\n"+
				"		    			depth = depth - 1;\n"+
				"		    			stop;\n"+
				"		    		}\n"+
				"		    	});\n"+
				"		    	if (inner_max > max) max=inner_max;\n"+
				"			}\n"+
				"			sum_max=sum_max+max;\n"+
				"		}\n"+
				"		depth = sum_max/len(node.methods);\n"+
				"		out = format(\"%s, Average Block depth = %f\",out,depth);\n"+
				
				"		#calculate cohesion in methods\n"+
				"		cohesion=0;\n"+
				"		var_names = new(var_names, len(node.fields), \"\");\n"+
				"		method1_var_access = new(method1_var_access, len(node.fields), false);\n"+
				"		method2_var_access = new(method2_var_access, len(node.fields), false);\n"+
					
				"		foreach (a: int; def(node.fields[a])){\n"+
				"			var_names[a] = node.fields[a].name;\n"+
				"		}\n"+
					
				"		for (z:=0; z < len(node.methods) - 1 ; z++){\n"+
				"			foreach (a: int; var_names[a]) method1_var_access[a] = false;\n"+
				"			foreach (a: int; def(node.methods[z].statements[a])){\n"+
				"		    	visit(node.methods[z].statements[a], visitor{\n"+
				"		    		before node:Expression -> {\n"+
				"		    			if (node.kind == ExpressionKind.VARACCESS){\n"+
				"							foreach (b: int; var_names[b]){\n"+
				"  								if (var_names[b] == node.variable)\n"+
				"									method1_var_access[b] = true;\n"+
				"	   						}\n"+
				" 	 					}\n"+
				"    					flag = false;\n"+
				"    					foreach (b: int; var_names[b]){\n"+
				"    						if (method1_var_access[b] == false)\n"+
				"    							flag = true;\n"+
				"    					}\n"+
				"    					if (flag == false) stop;\n"+
				"    				}\n"+
				"		    	});\n"+
				"			}\n"+
						
				"			for (x:= z + 1; x<len(node.methods); x++){\n"+
				"				foreach (a: int; var_names[a]) method2_var_access[a] = false;\n"+
				"				foreach (a: int; def(node.methods[x].statements[a])){\n"+
				"			    	visit(node.methods[x].statements[a], visitor{\n"+
				"		    			before node:Expression -> {\n"+
				"		    				if (node.kind == ExpressionKind.VARACCESS){\n"+
				"    							foreach (b: int; var_names[b]){\n"+
				"    								if (var_names[b] == node.variable)\n"+
				"    									method2_var_access[b] = true;\n"+
				"    							}\n"+
				"    						}\n"+
				"	    					flag = false;\n"+
				"   	 					foreach (b: int; var_names[b]){\n"+
				"   							if (method2_var_access[b] == false)\n"+
				"    								flag = true;\n"+
				"   						}\n"+
				"   						if (flag == false) stop;\n"+
				"   					}\n"+
				"		   			});\n"+
				"		   		}\n"+
				"			    flag = false;\n"+
				"			    foreach (a: int; var_names[a]){\n"+
				"			    	if (method1_var_access[a] == true && method2_var_access[a] == true){\n"+
				"		    			flag = true;\n"+
				"		    			break;\n"+
				"		 		   	}\n"+
				"		  		}\n"+
				"			    if (flag == true) cohesion = cohesion - 1;\n"+
				"		    	else cohesion = cohesion + 1;\n"+
				"			}\n"+
				"		}\n"+
				"		out = format(\"%s, Cohesion in Methods =  %d\",out,cohesion);\n"+
					
				"		#calculate public fields\n"+
				"		public_fields: int;\n"+
				"		is_public: int;\n"+
				"		public_fields=0;\n"+
				"		foreach (a: int; def(node.fields[a])){\n"+
				"			is_public=0;\n"+
				"			foreach (b: int; def(node.fields[a].modifiers[b])){\n"+
				"				if (node.fields[a].modifiers[b].kind == ModifierKind.VISIBILITY && node.fields[a].modifiers[b].visibility == Visibility.PUBLIC){\n"+
				"					is_public = 1;\n"+
				"					break;\n"+
				"				}\n"+
				"			}\n"+
				"			if (is_public == 1) public_fields++;\n"+
				"		}\n"+
				"		out = format(\"%s, Public Fields = %d\",out,public_fields);\n"+
					
				"		#calculate public methods\n"+
				"		public_methods: int;\n"+
				"		public_methods =0;\n"+
				"		foreach (a: int; def(node.methods[a])){\n"+
				"			is_public=0;\n"+
				"			foreach (b: int; def(node.methods[a].modifiers[b])){\n"+
				"				if (node.methods[a].modifiers[b].kind == ModifierKind.VISIBILITY && node.methods[a].modifiers[b].visibility == Visibility.PUBLIC){\n"+
				"					is_public = 1;\n"+
				"					break;\n"+
				"				}\n"+
				"			}\n"+
				"			if (is_public == 1) public_methods++;\n"+
				"		}\n"+
				"		out = format(\"%s, Public Methods = %d\",out,public_methods);\n"+
				
				"		#FINALLY output everything\n"+
				"		Files << out weight score;\n"+
				"		}\n"+
				"	}\n"+
				"	before node:Statement -> {\n"+
				"		if (node.kind == StatementKind.IF || node.kind == StatementKind.WHILE || node.kind == StatementKind.FOR || node.kind == StatementKind.CASE)\n"+
				"			complex = complex + 1;\n"+
				"		if (node.kind == StatementKind.RETURN)\n"+
				"			coupling++;\n"+
				"		if (node.kind != StatementKind.OTHER && node.kind != StatementKind.LABEL && node.kind != StatementKind.BLOCK)\n"+
				"			loc = loc + 1;\n"+
				"	}\n"+
				"	before node: Expression -> {\n"+
				" 	  if (node.kind == ExpressionKind.VARDECL){\n"+
				" 	 	coupling++;\n"+
				    	
				"    	foreach (c: int; def(node.variable_decls[c])){\n"+
				"   		var_type = node.variable_decls[c].variable_type.name;\n"+
				"			if (var_type != \"int\" && var_type != \"int[]\" && var_type != \"float\" && var_type != \"float[]\" \n"+
				"				&& var_type != \"String\" && var_type != \"String[]\" && var_type != \"char\" && var_type != \"char[]\" \n"+
				"				&& var_type != \"long\" && var_type != \"long[]\" && var_type != \"double\" && var_type != \"double[]\" \n"+
				"				&& var_type != \"byte\" && var_type != \"byte[]\" && var_type != \"short\" && var_type != \"short[]\" \n"+
				"				&& var_type != \"boolean\" && var_type != \"boolean[]\" && var_type != Decl_name && var_type != (Decl_name + \"[]\")){\n"+
				"					flag = false;\n"+
				"				    for (m:=0; m < pointer ; m++){\n"+
				"			    	    if (var_type == var_types[m]) flag = true;\n"+
				"				    }\n"+
				"	    			if (flag == false){\n"+
				"		    		    efferent++;\n"+
				"			    	    if (pointer < 30){\n"+
				"				            var_types[pointer]=var_type;\n"+
				"				            pointer++;\n"+
				"				        }\n"+
				"					}\n"+
				"			}\n"+
				"   	}\n"+
				"    }\n"+
				"    stop;\n"+
				"	}\n"+
				"	before Modifier,Variable,Type -> stop;\n"+
				"});";
		
		return query;
	}
}

