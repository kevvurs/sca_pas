package sca_pas.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import sca_pas.util.Reporter;
import sca_pas.util.Resources;

public class PersistTable {
	public static final Reporter r = new Reporter(PersistTable.class.getName());
	
	public static void output(List<List<String>> content) {
		r.log("Printing out data values");
		File file = new File("data.csv");
		try {
			FileWriter wr = new FileWriter(file);
			// Headers
			for (int j = 0; j < Resources.targets.length; j++) {
				wr.write(Resources.targets[j]);
				if (j + 1 != Resources.targets.length) {
					wr.write(',');
				}
			}
			wr.write('\n');
			
			// Content
			for (List<String> row : content) {
				for (int i = 0; i < row.size(); i++) {
					String cell = row.get(i).replaceAll(",", "_").trim();
					wr.write(cell);
					if (i + 1 != row.size()) {
						wr.write(',');
					}
				}
				wr.write('\n');
			}
			wr.flush();
			wr.close();
		} catch (IOException e) {
			r.log("Error outputing the file", e);
		}
		
	}
}
