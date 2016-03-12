package downloader;

public class Queries {	
	
	public static String query(String class_name, String method_names, String method_types){
		
		String query = "p: Project = input;\n"+
				"Files: output top(150) of string weight float;\n"+
				"\n"+
				"out: string;\n"+
				"cur_file: string;\n"+
				"method_name: array of string;\n"+
				"method_type: array of string;\n"+
				"\n"+
				"class_name:= "+class_name+";\n"+
				"method_name = {"+method_names+"};\n"+
				"method_type = {"+method_types+"};\n"+
				"length:= len(method_name);\n"+
				"\n"+
				"#flag for a match in method name\n"+
				"Flag_method_name: array of bool;\n"+
				"#flag for an exact match in method name\n"+
				"Flag_exact_match: array of bool;\n"+
				"#flag for a match in method type\n"+
				"Flag_method_type: array of bool;\n"+
				"\n"+
				"Flag_method_name = new(Flag_method_name, length, false);\n"+
				"Flag_exact_match = new(Flag_exact_match, length, false);\n"+
				"Flag_method_type = new(Flag_method_type, length, false);\n"+
				"\n"+
				"#Variables used for metric calculations\n"+
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
				"\n"+
				"visit(p, visitor {\n"+
				"	# only look at the latest snapshot of java files\n"+
				"	before node: CodeRepository -> {\n"+
				"		snapshot := getsnapshot(node, \"SOURCE_JAVA_JLS\");\n"+
				"		foreach (i: int; def(snapshot[i]))\n"+
				"			visit(snapshot[i]);\n"+
				"		stop;\n"+
				"	}\n"+
				"	before node: ChangedFile -> {\n"+
				"		cur_file = node.name;\n"+
				"	}\n"+
				"	#Check the class name and visit the methods\n"+
				"	before node: Declaration -> {\n"+
				"	    Decl_name = node.name;\n"+
				"		if (node.kind == TypeKind.CLASS && (class_name == \"-1\" || match(lowercase(class_name), lowercase(node.name)))){\n"+
				"			for (j:= 0; j < length; j++){\n"+
				"				Flag_method_name[j] = false;\n"+
				"				Flag_exact_match[j] = false;\n"+
				"				Flag_method_type[j] = false;\n"+
				"			}\n"+
				"			foreach (i: int; def(node.methods[i]))\n"+
				"				visit(node.methods[i]);\n"+
				"		}else\n"+
				"			stop;\n"+
				"	}\n"+
				"	#Set the flags depending on the method names and types\n"+
				"	before node: Method -> {\n"+
				"		for (i:= 0; i < length ; i++){\n"+
				"			if (match(lowercase(method_name[i]), lowercase(node.name))){\n"+
				"				Flag_method_name[i] = true;\n"+
				"				if (lowercase(method_name[i]) == lowercase(node.name))\n"+
				"					Flag_exact_match[i] = true;\n"+
				"				if (method_type[i] == \"-1\" || lowercase(node.return_type.name) == lowercase(method_type[i])){\n"+
				"					Flag_method_type[i] = true;\n"+
				"					stop;\n"+
				"				}else\n"+
				"					stop;\n"+
				"			}\n"+
				"		}\n"+
				"		stop;\n"+
				"	}\n"+
				"	#Calculate every metric\n"+
				"	after node: Declaration -> {\n"+
				"		#proceed if there is at least one match\n"+
				"		exists (i: int; (Flag_method_name[i] == true)){\n"+
				"			#counters to count the Flags\n"+
				"			counter1:= 0;\n"+
				"			counter2:= 0;\n"+
				"			counter3:= 0;\n"+
				"			for (k:= 0; k < length ; k++){\n"+
				"				if (Flag_method_name[k] == true) counter1++;\n"+
				"				if (Flag_exact_match[k] == false) counter2++;\n"+
				"				if (Flag_method_type[k] == false) counter3++;\n"+
				"			}\n"+
				"			#Continue only for files that miss at most 1 method\n"+
				"			if (counter1 > (length - 2)){\n"+
				"				out = format(\"%s/blob/master/%s\", p.project_url, cur_file);\n"+
				"				#calculate score\n"+
				"				score = 0;\n"+
				"				for (l:= 0; l < length ; l++){\n"+
				"					if (counter1 == (length - l)){\n"+
				"						if (counter2 > 0 && counter3 > 0)\n"+
				"							score = 80 - (30*l) - (5*counter2/length) - (5*counter3/length);\n"+
				"						else if (counter2 > 0)\n"+
				"							score = 100 - (30*l) - (10*counter2/length);\n"+
				"						else if (counter3 > 0)\n"+
				"							score = 90 - (30*l) - (10*counter3/length);\n"+
				"						else\n"+
				"							score = 100 - (30*l);\n"+
				"						break;\n"+
				"					}\n"+
				"				}\n"+
				"		\n"+
				"				#Calculate complexity, coupling, LoC and part of efferent coupling.\n"+
				"				#Visit the statements node for each method.\n"+
				"				complex = 0;\n"+
				"				coupling = 0;\n"+
				"				loc = 0;\n"+
				"				efferent = 0;\n"+
				"				pointer = 0;\n"+
				"				\n"+
				"				foreach (a: int; var_types[a])\n"+
				"					var_types[a] = \"\";\n"+
				"		\n"+
				"				foreach (a: int; def(node.methods[a])){\n"+
				"		    		foreach (b: int; def(node.methods[a].statements[b]))\n"+
				"			    		visit(node.methods[a].statements[b]);\n"+
				"				}\n"+
				"				if (len(node.methods) != 0){\n"+
				"	    			complex = complex / len(node.methods) + 1;\n"+
				"	    			loc = loc / len(node.methods);\n"+
				"	    		} \n"+
				"				if (def(node.fields))\n"+
				"					coupling = coupling + len(node.fields);\n"+
				"		\n"+
				"				out = format(\"%s, Average Cyclomatic Complexity = %f\", out, complex);\n"+
				"				out = format(\"%s, Coupling = %d\", out, coupling);\n"+
				"				out = format(\"%s, LOC per Method = %f\", out, loc);\n"+
				"		\n"+
				"				#Calculate the rest efferent coupling.\n"+
				"				#Finding all the variable types that are depending on an external package or class.\n"+
				"				#Maximum 30 different variable types. (Upper threshold is 20)\n"+
				"				foreach (a: int; def(node.fields[a])){\n"+
				"					var_type = node.fields[a].variable_type.name;\n"+
				"					if (var_type != \"int\" && var_type != \"int[]\" && var_type != \"float\" && var_type != \"float[]\" \n"+
				"						&& var_type != \"String\" && var_type != \"String[]\" && var_type != \"char\" && var_type != \"char[]\" \n"+
				"						&& var_type != \"long\" && var_type != \"long[]\" && var_type != \"double\" && var_type != \"double[]\" \n"+
				"						&& var_type != \"byte\" && var_type != \"byte[]\" && var_type != \"short\" && var_type != \"short[]\" \n"+
				"						&& var_type != \"boolean\" && var_type != \"boolean[]\" && var_type != node.name && var_type != (node.name + \"[]\")){\n"+
				"							flag = false;\n"+
				"							for (n:=0; n < pointer ; n++){\n"+
				"					    		if (var_type == var_types[n]) flag = true;\n"+
				"							}\n"+
				"							if (flag == false){\n"+
				"					    		efferent++;\n"+
				"					    		if (pointer < 30){\n"+
				"					        		var_types[pointer] = var_type;\n"+
				"					       			pointer++;\n"+
				"					    		}\n"+
				"							}\n"+
				"					}\n"+
				"				}\n"+
				"				out = format(\"%s, Efferent couplings = %d\", out, efferent);\n"+
				"		\n"+
				"				#Calculate average block depth per method\n"+
				"				# using a recursive visitor pattern to find the maximum block depth.\n"+
				"				max: float;\n"+
				"				sum_max: float;\n"+
				"				inner_max: float;\n"+
				"				sum_max = 0;\n"+
				"				foreach (a: int; def(node.methods[a])){\n"+
				"					max = 0;\n"+
				"		    		foreach (b: int; def(node.methods[a].statements[b])){\n"+
				"		    			depth = 0;\n"+
				"		    			inner_max = 0;\n"+
				"			    		visit(node.methods[a].statements[b], visitor{\n"+
				"			    			before node:Statement -> {\n"+
				"			    				if (node.kind != StatementKind.OTHER && node.kind != StatementKind.LABEL){\n"+
				"			    					if (node.kind != StatementKind.BLOCK)\n"+
				"			    						depth = depth + 1;\n"+
				"			    					foreach(c: int; def(node.statements[c])){\n"+
				"			    						visit(node.statements[c]);\n"+
				"			    					}\n"+
				"			    				}\n"+
				"			    				if (depth > inner_max)\n"+
				"			    					inner_max = depth;\n"+
				"			    				depth = depth - 1;\n"+
				"			    				stop;\n"+
				"			    			}\n"+
				"			   			});\n"+
				"			    		if (inner_max > max)\n"+
				"			    			max = inner_max;\n"+
				"					}\n"+
				"					sum_max = sum_max + max;\n"+
				"				}\n"+
				"				depth = sum_max / len(node.methods);\n"+
				"				out = format(\"%s, Average Block depth = %f\", out, depth);\n"+
				"				\n"+
				"				#Calculate cohesion in methods.\n"+
				"				#Searching for method couples that access at least one common variable.\n"+
				"				#Cohesion equals to method couples that don't have a variable access in common\n"+
				"				# minus method couples that have a variable access in common\n"+
				"				cohesion = 0;\n"+
				"				var_names = new(var_names, len(node.fields), \"\");\n"+
				"				\n"+
				"				#create boolean variable access arrays to check every possible method couple\n"+
				"				method1_var_access = new(method1_var_access, len(node.fields), false);\n"+
				"				method2_var_access = new(method2_var_access, len(node.fields), false);\n"+
				"				\n"+
				"				#get all the variable names\n"+
				"				foreach (a: int; def(node.fields[a]))\n"+
				"					var_names[a] = node.fields[a].name;\n"+
				"					\n"+
				"				#Start to go through the methods\n"+
				"				for (z:=0; z < len(node.methods) - 1 ; z++){\n"+
				"					foreach (a: int; var_names[a])\n"+
				"						method1_var_access[a] = false;\n"+
				"					#search which variables are accessed by method 1\n"+
				"					foreach (a: int; def(node.methods[z].statements[a])){\n"+
				"			    		visit(node.methods[z].statements[a], visitor{\n"+
				"			    			before node:Expression -> {\n"+
				"			    				if (node.kind == ExpressionKind.VARACCESS){\n"+
				"	    							foreach (b: int; var_names[b]){\n"+
				"	    								if (var_names[b] == node.variable)\n"+
				"	    									method1_var_access[b] = true;\n"+
				"	    							}\n"+
				"	    						}\n"+
				"	    						#stop if all variables are accessed by method 1\n"+
				"	    						flag = false;\n"+
				"	    						foreach (b: int; var_names[b]){\n"+
				"	    							if (method1_var_access[b] == false)\n"+
				"	    								flag = true;\n"+
				"	    						}\n"+
				"	    						if (flag == false)\n"+
				"	    							stop;\n"+
				"	    					}\n"+
				"			    		});\n"+
				"					}\n"+
				"			\n"+
				"					for (x:= z + 1; x < len(node.methods); x++){\n"+
				"						foreach (a: int; var_names[a])\n"+
				"							method2_var_access[a] = false;\n"+
				"						#search which variables are accessed by method 2\n"+
				"						foreach (a: int; def(node.methods[x].statements[a])){\n"+
				"			    			visit(node.methods[x].statements[a], visitor{\n"+
				"			    				before node:Expression -> {\n"+
				"			    					if (node.kind == ExpressionKind.VARACCESS){\n"+
				"	    								foreach (b: int; var_names[b]){\n"+
				"	    									if (var_names[b] == node.variable)\n"+
				"	    										method2_var_access[b] = true;\n"+
				"	    								}\n"+
				"	    							}\n"+
				"	    							#stop if all variables are accessed by method 2\n"+
				"	    							flag = false;\n"+
				"	    							foreach (b: int; var_names[b]){\n"+
				"	    								if (method2_var_access[b] == false)\n"+
				"	    									flag = true;\n"+
				"	    							}\n"+
				"	    							if (flag == false)\n"+
				"	    								stop;\n"+
				"	    						}\n"+
				"			   				});\n"+
				"			    		}\n"+
				"			    		#Check if there is a variable that both methods access\n"+
				"			    		flag = false;\n"+
				"			    		foreach (a: int; var_names[a]){\n"+
				"			    			if (method1_var_access[a] == true && method2_var_access[a] == true){\n"+
				"			    				flag = true;\n"+
				"			    				break;\n"+
				"			    			}\n"+
				"			    		}\n"+
				"			    		if (flag == true)\n"+
				"			    			cohesion = cohesion - 1;\n"+
				"			    		else\n"+
				"			    			cohesion = cohesion + 1;\n"+
				"					}\n"+
				"				}\n"+
				"				out = format(\"%s, Cohesion in Methods =  %d\", out, cohesion);\n"+
				"		\n"+
				"				#Calculate public fields\n"+
				"				public_fields: int;\n"+
				"				is_public: int;\n"+
				"				public_fields = 0;\n"+
				"				foreach (a: int; def(node.fields[a])){\n"+
				"					is_public = 0;\n"+
				"					foreach (b: int; def(node.fields[a].modifiers[b])){\n"+
				"						if (node.fields[a].modifiers[b].kind == ModifierKind.VISIBILITY && node.fields[a].modifiers[b].visibility == Visibility.PUBLIC){\n"+
				"							is_public = 1;\n"+
				"							break;\n"+
				"						}\n"+
				"					}\n"+
				"					if (is_public == 1)\n"+
				"						public_fields++;\n"+
				"				}\n"+
				"				out = format(\"%s, Public Fields = %d\", out, public_fields);\n"+
				"		\n"+
				"				#Calculate public methods\n"+
				"				public_methods: int;\n"+
				"				public_methods = 0;\n"+
				"				foreach (a: int; def(node.methods[a])){\n"+
				"					is_public = 0;\n"+
				"					foreach (b: int; def(node.methods[a].modifiers[b])){\n"+
				"						if (node.methods[a].modifiers[b].kind == ModifierKind.VISIBILITY && node.methods[a].modifiers[b].visibility == Visibility.PUBLIC){\n"+
				"							is_public = 1;\n"+
				"							break;\n"+
				"						}\n"+
				"					}\n"+
				"					if (is_public == 1)\n"+
				"						public_methods++;\n"+
				"				}\n"+
				"				out = format(\"%s, Public Methods = %d\", out, public_methods);\n"+
				"		\n"+
				"				#Finally, output everything\n"+
				"				Files << out weight score;\n"+
				"			}\n"+
				"		}\n"+
				"	}\n"+
				"	#Visiting the Statement node to calculate complexity, coupling, LoC and efferent coupling\n"+
				"	before node:Statement -> {\n"+
				"		if (node.kind == StatementKind.IF || node.kind == StatementKind.WHILE || node.kind == StatementKind.FOR || node.kind == StatementKind.CASE)\n"+
				"			complex = complex + 1;\n"+
				"		if (node.kind == StatementKind.RETURN)\n"+
				"			coupling++;\n"+
				"		if (node.kind != StatementKind.OTHER && node.kind != StatementKind.LABEL && node.kind != StatementKind.BLOCK)\n"+
				"			loc = loc + 1;\n"+
				"	}\n"+
				"	#Visiting the Expression node to calculate coupling and efferent coupling\n"+
				"	before node: Expression -> {\n"+
				"	    if (node.kind == ExpressionKind.VARDECL){\n"+
				"	    	coupling++;\n"+
				"	    	\n"+
				"	    	foreach (c: int; def(node.variable_decls[c])){\n"+
				"	    		var_type = node.variable_decls[c].variable_type.name;\n"+
				"				if (var_type != \"int\" && var_type != \"int[]\" && var_type != \"float\" && var_type != \"float[]\" \n"+
				"					&& var_type != \"String\" && var_type != \"String[]\" && var_type != \"char\" && var_type != \"char[]\" \n"+
				"					&& var_type != \"long\" && var_type != \"long[]\" && var_type != \"double\" && var_type != \"double[]\" \n"+
				"					&& var_type != \"byte\" && var_type != \"byte[]\" && var_type != \"short\" && var_type != \"short[]\" \n"+
				"					&& var_type != \"boolean\" && var_type != \"boolean[]\" && var_type != Decl_name && var_type != (Decl_name + \"[]\")){\n"+
				"						flag = false;\n"+
				"					    for (m:=0; m < pointer ; m++){\n"+
				"				    	    if (var_type == var_types[m]) flag = true;\n"+
				"					    }\n"+
				"		    			if (flag == false){\n"+
				"			    		    efferent++;\n"+
				"				    	    if (pointer < 30){\n"+
				"					            var_types[pointer]=var_type;\n"+
				"					            pointer++;\n"+
				"					        }\n"+
				"						}\n"+
				"				}\n"+
				"	    	}\n"+
				"	    }\n"+
				"	    stop;\n"+
				"	}\n"+
				"	before Modifier,Variable,Type -> stop;\n"+
				"});";
		
		return query;
	}
	public static String query_one_method(String class_name, String method_names, String method_types){
		
		String query = "p: Project = input;\n"+
				"Files: output top(150) of string weight float;\n"+
				"\n"+
				"out: string;\n"+
				"cur_file: string;\n"+
				"\n"+
				"#flag for a match in method name\n"+
				"Flag_method_name: bool;\n"+
				"#flag for an exact match in method name\n"+
				"Flag_exact_match: bool;\n"+
				"#flag for a match in method type\n"+
				"Flag_method_type: bool;\n"+
				"\n"+
				"class_name:= "+class_name+";\n"+
				"method_name:= "+method_names+";\n"+
				"method_type:= "+method_types+";\n"+
				"\n"+
				"#Variables used for metric calculations\n"+
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
				"\n"+
				"visit(p, visitor {\n"+
				"	# only look at the latest snapshot of java files\n"+
				"	before node: CodeRepository -> {\n"+
				"		snapshot := getsnapshot(node, \"SOURCE_JAVA_JLS\");\n"+
				"		foreach (i: int; def(snapshot[i]))\n"+
				"			visit(snapshot[i]);\n"+
				"		stop;\n"+
				"	}\n"+
				"	before node: ChangedFile -> {\n"+
				"		cur_file = node.name;\n"+
				"	}\n"+
				"	#Check the class name and visit the methods\n"+
				"	before node: Declaration -> {\n"+
				"	    Decl_name = node.name;\n"+
				"		if (node.kind == TypeKind.CLASS && (class_name == \"-1\" || match(lowercase(class_name), lowercase(node.name)))){\n"+
				"			Flag_method_name = false;\n"+
				"			Flag_exact_match = false;\n"+
				"			Flag_method_type = false;\n"+
				"			foreach (i: int; def(node.methods[i]))\n"+
				"				visit(node.methods[i]);\n"+
				"		}else\n"+
				"			stop;\n"+
				"	}\n"+
				"	#Set the flags depending on the method names and types\n"+
				"	before node: Method -> {\n"+
				"		if (match(lowercase(method_name), lowercase(node.name))){\n"+
				"			Flag_method_name = true;\n"+
				"			if (lowercase(method_name) == lowercase(node.name))\n"+
				"				Flag_exact_match = true;\n"+
				"			if (method_type == \"-1\" || lowercase(node.return_type.name) == lowercase(method_type)){\n"+
				"				Flag_method_type = true;\n"+
				"				stop;\n"+
				"			}else\n"+
				"				stop;\n"+
				"		}\n"+
				"		stop; \n"+
				"	}\n"+
				"	#Calculate every metric\n"+
				"	after node: Declaration -> {\n"+
				"		#proceed if there is at least one match\n"+
				"		if (Flag_method_name == true){\n"+
				"	       	out = format(\"%s/blob/master/%s\",p.project_url,cur_file);\n"+
				"	    	#calculate score\n"+
				"	    	score=0;\n"+
				"	    	if (Flag_exact_match == false && Flag_method_type == false) score = 70;\n"+
				"	    	else if (Flag_exact_match == false) score = 90;\n"+
				"	    	else if (Flag_method_type == false) score = 80;\n"+
				"	    	else score = 100;\n"+
				"		\n"+
				"			#Calculate complexity, coupling, LoC and part of efferent coupling.\n"+
				"			#Visit the statements node for each method.\n"+
				"			complex = 0;\n"+
				"			coupling = 0;\n"+
				"			loc = 0;\n"+
				"			efferent = 0;\n"+
				"			pointer = 0;\n"+
				"			\n"+
				"			foreach (a: int; var_types[a])\n"+
				"				var_types[a] = \"\";\n"+
				"		\n"+
				"			foreach (a: int; def(node.methods[a])){\n"+
				"		    	foreach (b: int; def(node.methods[a].statements[b]))\n"+
				"			   		visit(node.methods[a].statements[b]);\n"+
				"			}\n"+
				"			if (len(node.methods) != 0){\n"+
				"	    		complex = complex / len(node.methods) + 1;\n"+
				"	    		loc = loc / len(node.methods);\n"+
				"	    	} \n"+
				"			if (def(node.fields))\n"+
				"				coupling = coupling + len(node.fields);\n"+
				"		\n"+
				"			out = format(\"%s, Average Cyclomatic Complexity = %f\", out, complex);\n"+
				"			out = format(\"%s, Coupling = %d\", out, coupling);\n"+
				"			out = format(\"%s, LOC per Method = %f\", out, loc);\n"+
				"		\n"+
				"			#Calculate the rest efferent coupling.\n"+
				"			#Finding all the variable types that are depending on an external package or class.\n"+
				"			#Maximum 30 different variable types. (Upper threshold is 20)\n"+
				"			foreach (a: int; def(node.fields[a])){\n"+
				"				var_type = node.fields[a].variable_type.name;\n"+
				"				if (var_type != \"int\" && var_type != \"int[]\" && var_type != \"float\" && var_type != \"float[]\" \n"+
				"					&& var_type != \"String\" && var_type != \"String[]\" && var_type != \"char\" && var_type != \"char[]\" \n"+
				"					&& var_type != \"long\" && var_type != \"long[]\" && var_type != \"double\" && var_type != \"double[]\" \n"+
				"					&& var_type != \"byte\" && var_type != \"byte[]\" && var_type != \"short\" && var_type != \"short[]\" \n"+
				"					&& var_type != \"boolean\" && var_type != \"boolean[]\" && var_type != node.name && var_type != (node.name + \"[]\")){\n"+
				"						flag = false;\n"+
				"						for (n:=0; n < pointer ; n++){\n"+
				"				    		if (var_type == var_types[n]) flag = true;\n"+
				"						}\n"+
				"						if (flag == false){\n"+
				"				    		efferent++;\n"+
				"				    		if (pointer < 30){\n"+
				"				        		var_types[pointer] = var_type;\n"+
				"				       			pointer++;\n"+
				"				    		}\n"+
				"						}\n"+
				"				}\n"+
				"			}\n"+
				"			out = format(\"%s, Efferent couplings = %d\", out, efferent);\n"+
				"		\n"+
				"			#Calculate average block depth per method\n"+
				"			# using a recursive visitor pattern to find the maximum block depth.\n"+
				"			max: float;\n"+
				"			sum_max: float;\n"+
				"			inner_max: float;\n"+
				"			sum_max = 0;\n"+
				"			foreach (a: int; def(node.methods[a])){\n"+
				"				max = 0;\n"+
				"		    	foreach (b: int; def(node.methods[a].statements[b])){\n"+
				"		    		depth = 0;\n"+
				"		    		inner_max = 0;\n"+
				"			   		visit(node.methods[a].statements[b], visitor{\n"+
				"			   			before node:Statement -> {\n"+
				"			   				if (node.kind != StatementKind.OTHER && node.kind != StatementKind.LABEL){\n"+
				"			   					if (node.kind != StatementKind.BLOCK)\n"+
				"			   						depth = depth + 1;\n"+
				"			   					foreach(c: int; def(node.statements[c])){\n"+
				"			   						visit(node.statements[c]);\n"+
				"			   					}\n"+
				"			   				}\n"+
				"			   				if (depth > inner_max)\n"+
				"			   					inner_max = depth;\n"+
				"			   				depth = depth - 1;\n"+
				"			   				stop;\n"+
				"			   			}\n"+
				"					});\n"+
				"			   		if (inner_max > max)\n"+
				"			   			max = inner_max;\n"+
				"				}\n"+
				"				sum_max = sum_max + max;\n"+
				"			}\n"+
				"			depth = sum_max / len(node.methods);\n"+
				"			out = format(\"%s, Average Block depth = %f\", out, depth);\n"+
				"			\n"+
				"			#Calculate cohesion in methods.\n"+
				"			#Searching for method couples that access at least one common variable.\n"+
				"			#Cohesion equals to method couples that don't have a variable access in common\n"+
				"			# minus method couples that have a variable access in common\n"+
				"			cohesion = 0;\n"+
				"			var_names = new(var_names, len(node.fields), \"\");\n"+
				"			\n"+
				"			#create boolean variable access arrays to check every possible method couple\n"+
				"			method1_var_access = new(method1_var_access, len(node.fields), false);\n"+
				"			method2_var_access = new(method2_var_access, len(node.fields), false);\n"+
				"			\n"+
				"			#get all the variable names\n"+
				"			foreach (a: int; def(node.fields[a]))\n"+
				"				var_names[a] = node.fields[a].name;\n"+
				"				\n"+
				"			#Start to go through the methods\n"+
				"			for (z:=0; z < len(node.methods) - 1 ; z++){\n"+
				"				foreach (a: int; var_names[a])\n"+
				"					method1_var_access[a] = false;\n"+
				"				#search which variables are accessed by method 1\n"+
				"				foreach (a: int; def(node.methods[z].statements[a])){\n"+
				"			   		visit(node.methods[z].statements[a], visitor{\n"+
				"			   			before node:Expression -> {\n"+
				"			   				if (node.kind == ExpressionKind.VARACCESS){\n"+
				"	    						foreach (b: int; var_names[b]){\n"+
				"	    							if (var_names[b] == node.variable)\n"+
				"	    								method1_var_access[b] = true;\n"+
				"	    						}\n"+
				"	    					}\n"+
				"	    					#stop if all variables are accessed by method 1\n"+
				"	    					flag = false;\n"+
				"	    					foreach (b: int; var_names[b]){\n"+
				"	    						if (method1_var_access[b] == false)\n"+
				"	    							flag = true;\n"+
				"	    					}\n"+
				"	    					if (flag == false)\n"+
				"	    						stop;\n"+
				"	    				}\n"+
				"			   		});\n"+
				"				}\n"+
				"			\n"+
				"				for (x:= z + 1; x < len(node.methods); x++){\n"+
				"					foreach (a: int; var_names[a])\n"+
				"						method2_var_access[a] = false;\n"+
				"					#search which variables are accessed by method 2\n"+
				"					foreach (a: int; def(node.methods[x].statements[a])){\n"+
				"			   			visit(node.methods[x].statements[a], visitor{\n"+
				"			   				before node:Expression -> {\n"+
				"			   					if (node.kind == ExpressionKind.VARACCESS){\n"+
				"	    							foreach (b: int; var_names[b]){\n"+
				"	    								if (var_names[b] == node.variable)\n"+
				"	    									method2_var_access[b] = true;\n"+
				"	    							}\n"+
				"	    						}\n"+
				"	    						#stop if all variables are accessed by method 2\n"+
				"	    						flag = false;\n"+
				"	    						foreach (b: int; var_names[b]){\n"+
				"	    							if (method2_var_access[b] == false)\n"+
				"	    								flag = true;\n"+
				"	    						}\n"+
				"	    						if (flag == false)\n"+
				"	    							stop;\n"+
				"	    					}\n"+
				"						});\n"+
				"			   		}\n"+
				"			   		#Check if there is a variable that both methods access\n"+
				"			   		flag = false;\n"+
				"			   		foreach (a: int; var_names[a]){\n"+
				"			   			if (method1_var_access[a] == true && method2_var_access[a] == true){\n"+
				"			   				flag = true;\n"+
				"			   				break;\n"+
				"			   			}\n"+
				"			   		}\n"+
				"			   		if (flag == true)\n"+
				"			   			cohesion = cohesion - 1;\n"+
				"			   		else\n"+
				"			   			cohesion = cohesion + 1;\n"+
				"				}\n"+
				"			}\n"+
				"			out = format(\"%s, Cohesion in Methods =  %d\", out, cohesion);\n"+
				"		\n"+
				"			#Calculate public fields\n"+
				"			public_fields: int;\n"+
				"			is_public: int;\n"+
				"			public_fields = 0;\n"+
				"			foreach (a: int; def(node.fields[a])){\n"+
				"				is_public = 0;\n"+
				"				foreach (b: int; def(node.fields[a].modifiers[b])){\n"+
				"					if (node.fields[a].modifiers[b].kind == ModifierKind.VISIBILITY && node.fields[a].modifiers[b].visibility == Visibility.PUBLIC){\n"+
				"						is_public = 1;\n"+
				"						break;\n"+
				"					}\n"+
				"				}\n"+
				"				if (is_public == 1)\n"+
				"					public_fields++;\n"+
				"			}\n"+
				"			out = format(\"%s, Public Fields = %d\", out, public_fields);\n"+
				"		\n"+
				"			#Calculate public methods\n"+
				"			public_methods: int;\n"+
				"			public_methods = 0;\n"+
				"			foreach (a: int; def(node.methods[a])){\n"+
				"				is_public = 0;\n"+
				"				foreach (b: int; def(node.methods[a].modifiers[b])){\n"+
				"					if (node.methods[a].modifiers[b].kind == ModifierKind.VISIBILITY && node.methods[a].modifiers[b].visibility == Visibility.PUBLIC){\n"+
				"						is_public = 1;\n"+
				"						break;\n"+
				"					}\n"+
				"				}\n"+
				"				if (is_public == 1)\n"+
				"					public_methods++;\n"+
				"			}\n"+
				"			out = format(\"%s, Public Methods = %d\", out, public_methods);\n"+
				"		\n"+
				"			#Finally, output everything\n"+
				"			Files << out weight score;\n"+
				"		}\n"+
				"	}\n"+
				"	#Visiting the Statement node to calculate complexity, coupling, LoC and efferent coupling\n"+
				"	before node:Statement -> {\n"+
				"		if (node.kind == StatementKind.IF || node.kind == StatementKind.WHILE || node.kind == StatementKind.FOR || node.kind == StatementKind.CASE)\n"+
				"			complex = complex + 1;\n"+
				"		if (node.kind == StatementKind.RETURN)\n"+
				"			coupling++;\n"+
				"		if (node.kind != StatementKind.OTHER && node.kind != StatementKind.LABEL && node.kind != StatementKind.BLOCK)\n"+
				"			loc = loc + 1;\n"+
				"	}\n"+
				"	#Visiting the Expression node to calculate coupling and efferent coupling\n"+
				"	before node: Expression -> {\n"+
				"	    if (node.kind == ExpressionKind.VARDECL){\n"+
				"	    	coupling++;\n"+
				"	    	\n"+
				"	    	foreach (c: int; def(node.variable_decls[c])){\n"+
				"	    		var_type = node.variable_decls[c].variable_type.name;\n"+
				"				if (var_type != \"int\" && var_type != \"int[]\" && var_type != \"float\" && var_type != \"float[]\" \n"+
				"					&& var_type != \"String\" && var_type != \"String[]\" && var_type != \"char\" && var_type != \"char[]\" \n"+
				"					&& var_type != \"long\" && var_type != \"long[]\" && var_type != \"double\" && var_type != \"double[]\" \n"+
				"					&& var_type != \"byte\" && var_type != \"byte[]\" && var_type != \"short\" && var_type != \"short[]\" \n"+
				"					&& var_type != \"boolean\" && var_type != \"boolean[]\" && var_type != Decl_name && var_type != (Decl_name + \"[]\")){\n"+
				"						flag = false;\n"+
				"					    for (m:=0; m < pointer ; m++){\n"+
				"				    	    if (var_type == var_types[m]) flag = true;\n"+
				"					    }\n"+
				"		    			if (flag == false){\n"+
				"			    		    efferent++;\n"+
				"				    	    if (pointer < 30){\n"+
				"					            var_types[pointer]=var_type;\n"+
				"					            pointer++;\n"+
				"					        }\n"+
				"						}\n"+
				"				}\n"+
				"	    	}\n"+
				"	    }\n"+
				"	    stop;\n"+
				"	}\n"+
				"	before Modifier,Variable,Type -> stop;\n"+
				"});";
		
		return query;
	}
}

