package de.brvolleys.berlinrecyclingvolleys;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

public class ArticleOverviewHtmlParser implements
		HtmlParser<List<ArticleOverviewEntry>> {
	private static final String TAG = "ArticleOverviewHtmlParser";

	public List<ArticleOverviewEntry> parse(String htmlContent) {
		return parse(Jsoup.parse(htmlContent));
	}

	@Override
	public List<ArticleOverviewEntry> parse(URL url) {
		Log.v(TAG, "Url: " + url);

		Document doc = null;
		try {
			doc = Jsoup.connect(url.toString()).timeout(5*1000).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.v(TAG, "Doc: " + doc);
		return parse(doc);
	}

	private List<ArticleOverviewEntry> parse(Document doc) {
		Log.v(TAG, "Doc " + doc);
		if (doc == null) {
			return null;
		}
		// Get archive table
		Elements archiveTables = doc.getElementsByClass("zebra");
		List<ArticleOverviewEntry> articleEntries = new ArrayList<ArticleOverviewEntry>();

		for (Element table : archiveTables) {

			// Get entries in table
			Elements articleEntries1 = table.getElementsByClass("odd");
			Elements articleEntries2 = table.getElementsByClass("even");

			// parse entries
			articleEntries.addAll(extractTitleDateAndLink(articleEntries1));
			articleEntries.addAll(extractTitleDateAndLink(articleEntries2));
		}

		return articleEntries;
	}

	private List<ArticleOverviewEntry> extractTitleDateAndLink(
			Elements articleEntries) {

		List<ArticleOverviewEntry> result = new ArrayList<ArticleOverviewEntry>();

		for (Element entry : articleEntries) {
			// Get date
			String dateContent = entry.child(1).text();
			Date date = DateConverter.getDate(dateContent);

			// Get link
			Element entrydetails = entry.child(0).child(0);
			String link = ArticleOverviewActivity.DOMAIN
					+ entrydetails.attr("href");

			// Get title
			String title = entrydetails.text();

			result.add(new ArticleOverviewEntry(title, link, date));
		}

		return result;
	}
}
