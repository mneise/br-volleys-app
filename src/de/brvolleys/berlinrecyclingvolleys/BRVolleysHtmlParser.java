package de.brvolleys.berlinrecyclingvolleys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BRVolleysHtmlParser {

	public List<Entry> parse(String url) {
		List<Entry> entries = new ArrayList<Entry>();

		try {
			Document doc = Jsoup.connect(url).get();
			Elements articletable = doc.getElementsByClass("zebra");

			for (Element table : articletable) {

				Elements oddentries = table.getElementsByClass("odd");
				Elements evenentries = table.getElementsByClass("even");
				entries.addAll(extractContents(oddentries));
				entries.addAll(extractContents(evenentries));
			}
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		}
		return entries;
	}

	public static class Entry {
		public final String title;
		public final String link;
		public final String date;

		private Entry(String title, String link, String date) {
			this.title = title;
			this.link = link;
			this.date = date;
		}
	}

	private List<Entry> extractContents(Elements entries) {
		List<Entry> result = new ArrayList<Entry>();
		for (Element entry : entries) {
			Element entrydetails = entry.child(0).child(0);
			String link = MainActivity.DOMAIN + entrydetails.attr("href");
			String title = entrydetails.text();

			String date = entry.child(1).text();
			result.add(new Entry(title, link, date));
		}
		return result;
	}
}
