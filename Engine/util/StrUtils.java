package util;

import java.util.ArrayList;
import java.util.List;

public class StrUtils {
	public static String[] parseCommand(String string) {
		String read = "";
		final List<String> getArgs = new ArrayList<String>();
		boolean insideQuotes = false;
		for (int i = 0; i < string.length(); i++) {
			final char c = string.charAt(i);

			if (c == '\"') {
				insideQuotes = !insideQuotes;

				if (!insideQuotes) {
					getArgs.add(read);
					read = "";
				}
			}

			else if (c == ' ' && !insideQuotes) {
				if (read.length() > 0) {
					getArgs.add(read);
				}
				read = "";
			}

			else {
				read += c;
			}
		}

		if (read.length() > 0) {
			getArgs.add(read);
		}

		String[] strs = new String[getArgs.size()];
		strs = getArgs.toArray(strs);
		return strs;
	}

	public static String[] argsOnly(String[] strs) {
		String[] args = new String[strs.length - 1];
		if (strs.length > 1) {
			for (int i = 1; i < strs.length; i++) {
				args[i - 1] = strs[i];
			}
		}
		return args;
	}
}
