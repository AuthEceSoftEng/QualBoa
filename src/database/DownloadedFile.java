package database;

public class DownloadedFile {

	String path;
	private String content;

	public DownloadedFile(String path, String content) {
		this.path = path;
		this.content = content;
	}

	public String serialize() {
		return path + "\n_________________________________\n" + content;
	}

	@Override
	public String toString() {
		return path;
	}

	public String getContent() {
		return content;
	}

	public String getPath() {
		return path;
	}
}
