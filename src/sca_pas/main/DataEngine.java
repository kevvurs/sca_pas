package sca_pas.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import sca_pas.util.Reporter;
import sca_pas.util.Resources;

//Read property data from: http://sca-corp.com/pasassessments/index.cfm?id=7&janowCentral=aaa.begin
//Write out a CSV table with results.
public class DataEngine {
	public static final Reporter r = new Reporter(DataEngine.class.getName());
	
	public static void main(String[] args) {
		Set<String> parcelIds = new HashSet<String>();
		char[] nums = Resources.decNums;
		for (int i = 0; i < nums.length; i++) {
			String page = search(nums[i]);
			Set<String> partialSet = PageParse.aggregate(page);
			parcelIds.addAll(partialSet);
		}
		r.log("IDs pulled: " + parcelIds.size());
	}
	
	// Search all decimal number page results.
	private static String search(char param) {
		r.log("Searching: " + param);
		String query = (Resources.requestTemplate + param);
		String htmlPage = "";
		URL ref;
		try {
			ref = mkURL(Resources.formLink);
		} catch (MalformedURLException e) {
			r.log("Internet connection issue encountered.", e);
			return null;
		}
		
		// Connect to the target
		try {
			HttpURLConnection portal = (HttpURLConnection) ref.openConnection();
			portal.setRequestMethod("POST");
			portal.setRequestProperty("Accept", "text/html");
			portal.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			portal.setRequestProperty("charset", "utf-8");
			portal.setDoOutput(true);
			portal.getOutputStream().write(query.getBytes());
			portal.connect();
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(portal.getInputStream()));
			StringBuilder reponseBuilder = new StringBuilder();	
			String line;
			while ((line = rd.readLine()) != null) {
				// Process each line (date) of data
				reponseBuilder.append(line);
			}
			rd.close();
			htmlPage = reponseBuilder.toString();
		} catch (IOException e) {
			r.log("Error reading data stream from site.", e);
		}
		return htmlPage;
	}
	
	private static URL mkURL(String hyperlink) throws MalformedURLException {
		URL url = new URL(hyperlink);
		return url;
	}
}
