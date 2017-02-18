package sca_pas.asynch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import sca_pas.main.PageParse;
import sca_pas.util.Reporter;
import sca_pas.util.Resources;

// Get exact parcel IDs
public class ParseTask implements Callable<List<String>> {
	public static final Reporter r = new Reporter(ParseTask.class.getName());
	private final String pageLink;
	
	public ParseTask(Integer pageNumber) {
		this.pageLink = new StringBuilder()
			.append(Resources.universalTemplate[0])
			.append(pageNumber)
			.append(Resources.universalTemplate[1])
			.toString();
		
	}
	
	@Override
	public List<String> call() {
		String listingPage = PageParse.visit(pageLink);
		return marshal(listingPage);
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
