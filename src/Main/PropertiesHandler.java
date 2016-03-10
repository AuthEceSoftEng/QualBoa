package Main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesHandler {
	public static String GitHubUsername;
	public static String GitHubPassword;
	public static String BoaUsername;
	public static String BoaPassword;
	static {
		InputStream in;
		try {
			in = new FileInputStream("accounts.properties");
			Properties configProp = new Properties();
			configProp.load(in);
			GitHubUsername = configProp.getProperty("GitHubUsername");
			GitHubPassword = configProp.getProperty("GitHubPassword");
			BoaUsername = configProp.getProperty("BoaUsername");
			BoaPassword = configProp.getProperty("BoaPassword");
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
