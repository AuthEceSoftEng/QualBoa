package Main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MetricsThresholdsHandler {
	public static float Average_Lines_of_Code_per_Method;
	public static float Average_Cyclomatic_Complexity;
	public static float Coupling_Between_Objects;
	public static float Lack_of_Cohesion_in_Methods;
	public static float Average_Block_Depth;
	public static float Efferent_Couplings;
	public static float Number_of_Public_Fields;
	public static float Number_of_Public_Methods;
	public static float Percentage_of_Functional_Score;
	static {
		InputStream in;
		try {
			in = new FileInputStream("metrics.properties");
			Properties configProp = new Properties();
			configProp.load(in);
			Average_Lines_of_Code_per_Method = Float.parseFloat(configProp
					.getProperty("Average_Lines_of_Code_per_Method"));
			Average_Cyclomatic_Complexity = Float.parseFloat(configProp.getProperty("Average_Cyclomatic_Complexity"));
			Coupling_Between_Objects = Float.parseFloat(configProp.getProperty("Coupling_Between_Objects"));
			Lack_of_Cohesion_in_Methods = Float.parseFloat(configProp.getProperty("Lack_of_Cohesion_in_Methods"));
			Average_Block_Depth = Float.parseFloat(configProp.getProperty("Average_Block_Depth"));
			Efferent_Couplings = Float.parseFloat(configProp.getProperty("Efferent_Couplings"));
			Number_of_Public_Fields = Float.parseFloat(configProp.getProperty("Number_of_Public_Fields"));
			Number_of_Public_Methods = Float.parseFloat(configProp.getProperty("Number_of_Public_Methods"));
			Percentage_of_Functional_Score = Float.parseFloat(configProp.getProperty("Percentage_of_Functional_Score"));
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
