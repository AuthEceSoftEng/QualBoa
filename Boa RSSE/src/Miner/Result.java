package Miner;

public class Result {

	String path;
	double score;
	String content;
	float loc;

	public Result(String path, String content, double score, float loc) {
		this.path = path;
		this.content = content;
		this.score = score;
		this.loc = loc;
	}

	public String serialize() {
		return path + "\n_________________________________\nSCORE = " + score
				+ "\n_________________________________\nLOC = " + loc + "\n_________________________________\n"
				+ content;
	}

	@Override
	public String toString() {
		return path + "   " + score + "   " + loc;
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

	public float getLoc() {
		return loc;
	}

}
