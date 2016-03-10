package Miner;

import Main.MetricsThresholdsHandler;

public class Metrics {
	public float Average_Lines_of_Code_per_Method;
	public float Average_Cyclomatic_Complexity;
	public float Coupling_Between_Objects;
	public float Lack_of_Cohesion_in_Methods;
	public float Average_Block_Depth;
	public float Efferent_Couplings;
	public float Number_of_Public_Fields;
	public float Number_of_Public_Methods;

	public Metrics(String input, boolean readFromFile) {
		if (readFromFile) {
			String[] splitInput = input.split("\n");
			Average_Lines_of_Code_per_Method = Float.parseFloat(splitInput[0].split(" = ")[1]);
			Average_Cyclomatic_Complexity = Float.parseFloat(splitInput[1].split(" = ")[1]);
			Coupling_Between_Objects = Float.parseFloat(splitInput[2].split(" = ")[1]);
			Lack_of_Cohesion_in_Methods = Float.parseFloat(splitInput[3].split(" = ")[1]);
			Average_Block_Depth = Float.parseFloat(splitInput[4].split(" = ")[1]);
			Efferent_Couplings = Float.parseFloat(splitInput[5].split(" = ")[1]);
			Number_of_Public_Fields = Float.parseFloat(splitInput[6].split(" = ")[1]);
			Number_of_Public_Methods = Float.parseFloat(splitInput[7].split(" = ")[1]);
		} else {
			Average_Lines_of_Code_per_Method = Float.parseFloat(input.split("LOC per Method = ")[1].split(",")[0]);
			Average_Cyclomatic_Complexity = Float.parseFloat(input.split("Average Cyclomatic Complexity = ")[1]
					.split(",")[0]);
			Coupling_Between_Objects = Float.parseFloat(input.split("Coupling = ")[1].split(",")[0]);
			Lack_of_Cohesion_in_Methods = Float.parseFloat(input.split("Cohesion in Methods = ")[1].split(",")[0]);
			Average_Block_Depth = Float.parseFloat(input.split("Average Block depth = ")[1].split(",")[0]);
			Efferent_Couplings = Float.parseFloat(input.split("Efferent couplings = ")[1].split(",")[0]);
			Number_of_Public_Fields = Float.parseFloat(input.split("Public Fields = ")[1].split(",")[0]);
			Number_of_Public_Methods = Float.parseFloat(input.split("Public Methods = ")[1].split(",")[0]);
		}
	}

	public Metrics(String input) {
		this(input, false);
	}

	public double calculateQualityScore(Metrics metrics) {
		int score = 0;
		if (Average_Lines_of_Code_per_Method <= MetricsThresholdsHandler.Average_Lines_of_Code_per_Method)
			score++;
		if (Average_Cyclomatic_Complexity <= MetricsThresholdsHandler.Average_Cyclomatic_Complexity)
			score++;
		if (Coupling_Between_Objects <= MetricsThresholdsHandler.Coupling_Between_Objects)
			score++;
		if (Lack_of_Cohesion_in_Methods <= MetricsThresholdsHandler.Lack_of_Cohesion_in_Methods)
			score++;
		if (Average_Block_Depth <= MetricsThresholdsHandler.Average_Block_Depth)
			score++;
		if (Efferent_Couplings <= MetricsThresholdsHandler.Efferent_Couplings)
			score++;
		if (Number_of_Public_Fields <= MetricsThresholdsHandler.Number_of_Public_Fields)
			score++;
		if (Number_of_Public_Methods <= MetricsThresholdsHandler.Number_of_Public_Methods)
			score++;
		return score / 8.0;
	}

	public String serialize() {
		return "Average_Lines_of_Code_per_Method = " + Average_Lines_of_Code_per_Method
				+ "\nAverage_Cyclomatic_Complexity = " + Average_Cyclomatic_Complexity
				+ "\nCoupling_Between_Objects = " + Coupling_Between_Objects + "\nLack_of_Cohesion_in_Methods = "
				+ Lack_of_Cohesion_in_Methods + "\nAverage_Block_Depth = " + Average_Block_Depth
				+ "\nEfferent_Couplings = " + Efferent_Couplings + "\nNumber_of_Public_Fields = "
				+ Number_of_Public_Fields + "\nNumber_of_Public_Methods = " + Number_of_Public_Methods;
	}

}
