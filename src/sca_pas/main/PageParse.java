package sca_pas.main;

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
import sca_pas.util.Reporter;
import sca_pas.util.Resources;

public class PageParse {
	public static final Reporter r = new Reporter(PageParse.class.getName());

	public static Set<String> aggregate(String page) {
		Set<String> initPid = new HashSet<String>();
		List<String> allLinks = new ArrayList<String>();
		int factor = 0;
		for (int i = 0; i < page.length() - 12; i++) {
			StringBuilder val = new StringBuilder();
			val.append(page.charAt(i));
			val.append(page.charAt(i + 1));
			val.append(page.charAt(i + 2));
			val.append(page.charAt(i + 3));
			if (val.toString().equals(Resources.token)) {
				if (page.charAt(i + 5) == '"') {
					int j = i + 6;
					StringBuilder linkBuild = new StringBuilder();
					char sym;
					do {
						sym = page.charAt(j);
						if (sym == '"') {
							String ref = linkBuild.toString();
							if (ref.contains(Resources.pageLink)) {
								allLinks.add(ref);
								break;
							} else if (ref.contains(Resources.entryLink)) {
								factor++;
								if (factor % 3 == 0) {
									String pid = cutToField(page, j + 2);
									if (!pid.isEmpty()) {
										initPid.add(pid);
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
					} while (j < page.length());
				}
			}
		}
		return mine(initPid, allLinks);
	}
	
	// Parallel data mining operation.
	private static Set<String> mine(Set<String> base,List<String> links) {
		r.log("Collecting all IDs...");
		ExecutorService exec = Executors.newFixedThreadPool(8);
		try {
			Map<String, Future<List<String>>> taskMap = new ConcurrentHashMap<String, Future<List<String>>>();
			for (int i = 0; i < links.size(); i++) {
				String link = links.get(i);
				Callable<List<String>> pTask = new ParseTask(link);
				Future<List<String>> pRes = exec.submit(pTask);
				taskMap.put(link, pRes);
			}
			
			try {
				for (Entry<String, Future<List<String>>> entry : taskMap.entrySet()) {
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
