package sca_pas.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import sca_pas.asynch.ParseTask;
import sca_pas.asynch.SearchTask;
import sca_pas.util.Reporter;
import sca_pas.util.Resources;

public class PageParse {
	public static final Reporter r = new Reporter(PageParse.class.getName());

	// Parallel data mining operation.
	public static Set<String> mine(int limit) {
		r.log("Collecting all IDs...");
		Set<String> base = new HashSet<String>();
		ExecutorService exec = Executors.newFixedThreadPool(8);
		try {
			Map<Integer, Future<List<String>>> taskMap = new ConcurrentHashMap<Integer, Future<List<String>>>();
			for (int i = 1; i <= limit; i++) {
				Callable<List<String>> pTask = new ParseTask(i);
				Future<List<String>> pRes = exec.submit(pTask);
				taskMap.put(i, pRes);
			}
			
			try {
				for (Entry<Integer, Future<List<String>>> entry : taskMap.entrySet()) {
					List<String> ids = entry.getValue().get(10000, TimeUnit.MILLISECONDS);
					base.addAll(ids);
				}
			} catch (InterruptedException e) {
				r.log("Task interrupted", e);
			} catch (ExecutionException e) {
				r.log("Task not executed", e);
			} catch (TimeoutException e) {
				r.log("Task timed out", e);
			}
		} finally {
			exec.shutdown();
		}
		
		return base;
	}
	
	public static List<List<String>> mine(Set<String> ids) {
		r.log("Getting datasets...");
		List<List<String>> rows = new ArrayList<List<String>>();
		ExecutorService exec = Executors.newFixedThreadPool(8);
		try {
			Map<String, Future<List<String>>> taskMap = new ConcurrentHashMap<String, Future<List<String>>>();
			for (String id : ids) {
				Callable<List<String>> sTask = new SearchTask(id);
				Future<List<String>> sRes = exec.submit(sTask);
				taskMap.put(id, sRes);
			}
			
			try {
				for (Entry<String, Future<List<String>>> entry : taskMap.entrySet()) {
					List<String> data = entry.getValue().get(10000, TimeUnit.MILLISECONDS);
					rows.add(data);
				}
			} catch (InterruptedException e) {
				r.log("Task interrupted", e);
			} catch (ExecutionException e) {
				r.log("Task not executed", e);
			} catch (TimeoutException e) {
				r.log("Task timed out", e);
			}
		} finally {
			exec.shutdown();
		}
		return rows;
	}
	
	public static String visit(String pageLink) {
		// Get page.
		String listing = "";
		URL ref;
		try {
			ref = new URL(pageLink);
		} catch (MalformedURLException e) {
			r.log("Internet connection issue encountered.", e);
			return listing;
		}
		
		// Connect to the host.
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
		} catch (IOException e) {
			r.log("Error reading data stream from site.", e);
		}
		return listing;
	}
	
	public static String search(String pid) {
		String serialPid = pid.replaceAll("-", "");
		serialPid = serialPid.replaceAll("\\.", "");
		
		int s = serialPid.length();
		StringBuilder codeBuild = new StringBuilder();
		codeBuild.append("55280000");
		codeBuild.append(serialPid.substring(0, 4));
		codeBuild.append("0000");
		String minPid = serialPid.substring(4, s);
		int lim = 3;
		if (pid.split("-")[1].split("\\.").length > 1) {
			codeBuild.append('0');
		}
		for (int i = minPid.length(); i < lim; i++) {
			codeBuild.append('0');
		}
		codeBuild.append(minPid);
		//r.log(codeBuild.toString());
		String htmlPage = "";
		URL ref;
		try {
			ref = new URL(Resources.entryLink + codeBuild.toString());
		} catch (MalformedURLException e) {
			r.log("Internet connection issue encountered.", e);
			return htmlPage;
		}
		
		// Connect to the target
		try {
			HttpURLConnection portal = (HttpURLConnection) ref.openConnection();
			portal.setRequestMethod("GET");
			portal.setRequestProperty("Accept", "text/html");
			//portal.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			portal.setRequestProperty("charset", "utf-8");
			//portal.setDoOutput(true);
			//portal.getOutputStream().write(query.getBytes());
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
	
	public static String cutToField(String src, int i) {
		StringBuilder field = new StringBuilder();
		do {
			char c = src.charAt(i);
			if (c == '<') {
				break;
			} else {
				field.append(c);
			}
			i++;
		} while (i < src.length());
		return  field.toString().trim();
	}
}
