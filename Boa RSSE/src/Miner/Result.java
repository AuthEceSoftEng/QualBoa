package Miner;

public class Result {

	double score;
	String content;
	float loc;

	public Result(String content, double score, float loc) {
		this.content = content;
		this.score = score;
		this.loc = loc;
	}

	@Override
	public String toString() {
		return content;
	}
}
