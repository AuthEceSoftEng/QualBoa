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

	@Override
	public String toString() {
		return path + "\n_________________________________\nSCORE = " + score
				+ "\n_________________________________\nLOC = " + loc + "\n_________________________________\n"
				+ content;
	}

	public String toPrint() {
		return path + "   " + score + "   " + loc;
	}

}
