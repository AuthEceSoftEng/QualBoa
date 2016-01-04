package Database;

public class DownloadedFile {

	String path;
	private String content;

	public DownloadedFile(String path, String content) {
		this.path = path;
		this.content = content;
	}

	@Override
	public String toString() {
		return path + "\n_________________________________\n" + content;
	}

	public String getContent() {
		return content;
	}

	public String getPath() {
		return path;
	}
}
