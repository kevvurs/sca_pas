package sca_pas.asynch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import sca_pas.main.PageParse;
import sca_pas.util.Reporter;
import sca_pas.util.Resources;

public class SearchTask implements Callable<List<String>> {
	public static final Reporter r = new Reporter(SearchTask.class.getName());
	private final String id;
	
	public SearchTask(String id) {
		this.id = id;
	}
	
	@Override
	public List<String> call() {
		String pageAssessment = PageParse.search(this.id);
		List<String> data = new ArrayList<String>();
		data.add(this.id);
		data.addAll(marshal(pageAssessment));
		if (data.isEmpty()) {
			r.log(this.id);
		}
		return data;
	}
	
	private List<String> marshal(String html) {
		List<String> data = new ArrayList<String>();
		boolean access = false;
		if (html.isEmpty()) {
			return data;
		} else {
			for (int i = 0; i < html.length() - 12; i++) {
				StringBuilder val = new StringBuilder();
				for (int ii = 0; ii < 10; ii++) {
					val.append(html.charAt(i + ii));
				}
				if (val.toString().equals(Resources.flag1)) {
					int k = i + 12;
					String header = "";
					StringBuilder dataBuild = new StringBuilder();
					char sym;
					do {
						sym = html.charAt(k);
						if (sym == '<' || sym == '&') {
							header = dataBuild.toString().trim();
							break;
						} else {
							dataBuild.append(sym);
						}
						k++;
					} while (k < html.length());
					access = (header.equals("Address") || header.equals("Name") || header.equals("Type") || header.equals("Market Value"));
				}
				if (access) {
					if (val.toString().equals(Resources.flag2)) {
						int j = i + 12;
						StringBuilder dataBuild = new StringBuilder();
						char sym;
						do {
							sym = html.charAt(j);
							if (sym == '<' || sym == '&') {
								String ref = dataBuild.toString().trim();
								if (ref.contains(">")) {
									break;
								}
								data.add(ref);
								break;
							} else {
								dataBuild.append(sym);
							}
							j++;
						} while (j < html.length());
						access = false;
					}
				}
			}
		}
		return data;
	}
}
