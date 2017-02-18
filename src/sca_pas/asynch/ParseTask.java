package sca_pas.asynch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import sca_pas.main.PageParse;
import sca_pas.util.Reporter;
import sca_pas.util.Resources;

public class ParseTask implements Callable<List<String>> {
	public static final Reporter r = new Reporter(ParseTask.class.getName());
	private final String link;
	
	public ParseTask(String link) {
		this.link = link;
	}
	
	@Override
	public List<String> call() {
		
		
		// Get page
		String listing = "";
		URL ref;
		try {
			ref = new URL(this.link);
		} catch (MalformedURLException e) {
			r.log("Internet connection issue encountered.", e);
			return new ArrayList<String>();
		}
		
		// Connect to the target
		try {
			HttpURLConnection portal = (HttpURLConnection) ref.openConnection();
			portal.setRequestMethod("GET");
			portal.setRequestProperty("Accept", "text/html");
			portal.setRequestProperty("charset", "utf-8");
			portal.connect();
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(portal.getInputStream()));
			StringBuilder reponseBuilder = new StringBuilder();	
			String line;
			while ((line = rd.readLine()) != null) {
				reponseBuilder.append(line);
			}
			rd.close();
			portal.disconnect();
			listing = reponseBuilder.toString();
			return marshal(listing);
		} catch (IOException e) {
			r.log("Error reading data stream from site.", e);
			return new ArrayList<String>();
		}
	}
	
	private List<String> marshal(String html) {
		List<String> ids = new ArrayList<String>();
		if (html.isEmpty()) {
			return ids;
		} else {
			int factor = 0;
			for (int i = 0; i < html.length() - 12; i++) {
				StringBuilder val = new StringBuilder();
				val.append(html.charAt(i));
				val.append(html.charAt(i + 1));
				val.append(html.charAt(i + 2));
				val.append(html.charAt(i + 3));
				if (val.toString().equals(Resources.token)) {
					if (html.charAt(i + 5) == '"') {
						int j = i + 6;
						StringBuilder linkBuild = new StringBuilder();
						char sym;
						do {
							sym = html.charAt(j);
							if (sym == '"') {
								String ref = linkBuild.toString();
								if (ref.contains(Resources.entryLink)) {
									factor++;
									if (factor % 3 == 0) {
										String pid = PageParse.cutToField(html, j + 2);
										if (!pid.isEmpty()) {
											ids.add(pid);
										}
									}
									break;
								} else {
									break;
								}
							} else {
								linkBuild.append(sym);
							}
							j++;
						} while (j < html.length());
					}
				}
			}
		}
		return ids;
	}
}
