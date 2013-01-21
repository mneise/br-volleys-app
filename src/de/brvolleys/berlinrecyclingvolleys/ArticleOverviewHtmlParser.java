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

public class ArticleOverviewHtmlParser implements HtmlParser<List<ArticleOverviewEntry>> {

	public List<ArticleOverviewEntry> parse(String htmlContent) {
		return parse(Jsoup.parse(htmlContent));
	}

	@Override
	public List<ArticleOverviewEntry> parse(URL url) {

		Document doc = null;
		try {
			doc = Jsoup.connect(url.toString()).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return parse(doc);
	}

	private List<ArticleOverviewEntry> parse(Document doc) {
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
