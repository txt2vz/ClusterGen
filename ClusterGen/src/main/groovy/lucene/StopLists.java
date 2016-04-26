 package lucene;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class StopLists {

	public static Set<String> getStopSet() throws FileNotFoundException {

		final Set<String> stopSet = new TreeSet<String>();

		final Scanner sc = new Scanner(new File(

		// "src/cfg/stop_words_moderate.txt"));
				"src/cfg/stopwordsCarrot2.en"));

		// "src/cfg/stop_words_most.txt"));

		while (sc.hasNext()) {
			stopSet.add(sc.next());
		}
		sc.close();
		return stopSet;
	}
}