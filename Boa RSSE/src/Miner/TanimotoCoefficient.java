package Miner;

public class TanimotoCoefficient {

	public static float similarity(double[] features1, double[] features2) throws Exception {

		if (features1.length != features2.length) {
			throw new Exception("Features vectors must be of the same length");
		}

		int n = features1.length;
		double ab = 0.0;
		double a2 = 0.0;
		double b2 = 0.0;

		for (int i = 0; i < n; i++) {
			ab += features1[i] * features2[i];
			a2 += features1[i] * features1[i];
			b2 += features2[i] * features2[i];
		}
		return (float) ab / (float) (a2 + b2 - ab);
	}

}
