package Miner;

public class Result {

	String path;
	double score;
	String content;
	Metrics metrics;
	double qualityScore;

	public Result(String path, String content, double score, Metrics metrics, double qualityScore) {
		this.path = path;
		this.content = content;
		this.score = score;
		this.metrics = metrics;
		this.qualityScore = qualityScore;
	}

	public String serialize() {
		return path + "\n_________________________________\nSCORE = " + score
				+ "\n_________________________________\nQUALITY SCORE = " + qualityScore
				+ "\n_________________________________\n" + metrics.serialize() + "\n_________________________________\n"
				+ content;
	}

	@Override
	public String toString() {
		return path + "   " + score + "   " + qualityScore;
	}

	public String getPath() {
		return path;
	}

	public double getScore() {
		return score;
	}

	public String getContent() {
		return content;
	}

	public Metrics getMetrics() {
		return metrics;
	}

	public double getQualityScore() {
		return qualityScore;
	}

}
