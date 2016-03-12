package database;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import miner.Metrics;
import miner.Result;

public class FileHandler {

	private String rootPath;
	protected String downloadedFilesPath;
	protected String resultsPath;

	public FileHandler(String rootPath) {
		this(rootPath, false);
	}

	public FileHandler(String rootPath, boolean removeExistingFolder) {
		if (removeExistingFolder) {
			try {
				Files.walkFileTree(Paths.get(rootPath), new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.rootPath = rootPath + "/";
		downloadedFilesPath = this.rootPath + "DownloadedFiles/";
		resultsPath = this.rootPath + "Results/";
		try {
			Files.createDirectories(Paths.get(this.rootPath));
			Files.createDirectories(Paths.get(downloadedFilesPath));
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
		writeFileToPath(downloadedFilesPath + filename, file.serialize());
	}

	public Result readResult(String filename) {
		String fullcontent = readFileFromPath(resultsPath + filename);
		String path = fullcontent.split("\\n_________________________________\\n")[0];
		String score = fullcontent.split("\\n_________________________________\\n")[1].substring(8);
		String qualityScore = fullcontent.split("\\n_________________________________\\n")[2].substring(16);
		String metrics = fullcontent.split("\\n_________________________________\\n")[3];
		String content = fullcontent.split("\\n_________________________________\\n")[4];
		return new Result(path, content, Double.parseDouble(score), new Metrics(metrics, true), Double.parseDouble(qualityScore));
	}

	public void writeResult(String filename, Result result) {
		writeFileToPath(resultsPath + filename, result.serialize());
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
