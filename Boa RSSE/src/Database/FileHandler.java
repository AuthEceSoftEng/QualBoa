package Database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import Miner.Result;

public class FileHandler {

	private String rootPath;
	protected String downloadedFilesPath;
	protected String resultsPath;

	public FileHandler(String rootPath) {
		this.rootPath = rootPath + "/";
		downloadedFilesPath = this.rootPath + "DownloadedFiles/";
		resultsPath = this.rootPath + "Results/";
		try {
			if (!Files.exists(Paths.get(this.rootPath)))
				Files.createDirectories(Paths.get(this.rootPath));
			if (!Files.exists(Paths.get(downloadedFilesPath)))
				Files.createDirectories(Paths.get(downloadedFilesPath));
			if (!Files.exists(Paths.get(resultsPath)))
				Files.createDirectories(Paths.get(resultsPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String readFileFromPath(String path) {
		String content = "";
		try {
			content = new String(Files.readAllBytes(Paths.get(path)), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	private void writeFileToPath(String path, String content) {
		try {
			Files.write(Paths.get(path), content.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String readFile(String filename) {
		return readFileFromPath(rootPath + filename);
	}

	public void writeFile(String filename, String content) {
		writeFileToPath(rootPath + filename, content);
	}

	public DownloadedFile readDownloadedFile(String filename) {
		String fullcontent = readFileFromPath(downloadedFilesPath + filename);
		String path = fullcontent.split("\\n_________________________________\\n")[0];
		String content = fullcontent.split("\\n_________________________________\\n")[1];
		return new DownloadedFile(path, content);
	}

	public void writeDownloadedFile(String filename, DownloadedFile file) {
		writeFileToPath(downloadedFilesPath + filename, file.toString());
	}

	public Result readResult(String filename) {
		String fullcontent = readFileFromPath(resultsPath + filename);
		String path = fullcontent.split("\\n_________________________________\\n")[0];
		String content = fullcontent.split("\\n_________________________________\\n")[1];
		String score = fullcontent.split("\\n_________________________________\\n")[1];
		String loc = fullcontent.split("\\n_________________________________\\n")[1];
		return new Result(path, content, Double.parseDouble(score), Float.parseFloat(loc));
	}

	public void writeResult(String filename, Result result) {
		writeFileToPath(resultsPath + filename, result.toString());
	}

	public ArrayList<DownloadedFile> readAllDownloadedFiles() {
		ArrayList<DownloadedFile> files = new ArrayList<DownloadedFile>();
		for (String filename : new java.io.File(downloadedFilesPath).list()) {
			files.add(readDownloadedFile(filename));
		}
		return files;
	}

	public void writeAllDownloadedFiles(ArrayList<DownloadedFile> files) {
		for (int i = 0; i < files.size(); i++) {
			writeDownloadedFile("file" + i + ".java", files.get(i));
		}
	}

	public ArrayList<Result> readAllResults() {
		ArrayList<Result> results = new ArrayList<Result>();
		for (String filename : new java.io.File(resultsPath).list()) {
			results.add(readResult(filename));
		}
		return results;
	}

	public void writeAllResults(ArrayList<Result> results) {
		for (int i = 0; i < results.size(); i++) {
			writeResult("result" + i + ".java", results.get(i));
		}
	}
}
