package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
	public static List<String> getLines(File file) throws IOException {
		List<String> lines = new ArrayList<String>();
		
		try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}
		
		return lines;
	}
}
