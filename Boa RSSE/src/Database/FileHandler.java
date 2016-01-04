package Database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import Miner.Result;

public class FileHandler {

	private String filesPath;

	public FileHandler(String filesPath) {
		this.filesPath = filesPath;
		if (!Files.exists(Paths.get(filesPath))) {
			try {
				Files.createDirectories(Paths.get(filesPath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void writeFile(String filename, Result result) {
		writeFile(filename, result.toString());
	}

	public void writeFile(String filename, DownloadedFile file) {
		writeFile(filename, file.toString());
	}

	public void writeFile(String filename, String content) {
		try {
			Files.write(Paths.get(filesPath + "/" + filename), content.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DownloadedFile readFile(String filename) {
		String content = "";
		try {
			content = new String(Files.readAllBytes(Paths.get(filesPath + "/" + filename)), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		String path = content.split("\\n_________________________________\\n")[0];
		content = content.split("\\n_________________________________\\n")[1];
		return new DownloadedFile(path, content);
	}

	public ArrayList<DownloadedFile> readAllFiles() {
		ArrayList<DownloadedFile> files = new ArrayList<DownloadedFile>();
		for (String filename : new java.io.File(filesPath).list()) {
			files.add(readFile(filename));
		}
		return files;
	}
}
