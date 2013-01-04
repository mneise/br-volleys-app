package de.brvolleys.berlinrecyclingvolleys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BRVolleysHtmlParser {

	public static List<ArticleOverviewEntry> parseArticleOverview(String url) {
		List<ArticleOverviewEntry> newsEntries = new ArrayList<ArticleOverviewEntry>();

		try {
			Document doc = Jsoup.connect(url).get();
			Elements archiveTables = doc.getElementsByClass("zebra");

			for (Element table : archiveTables) {

				Elements newsEntries1 = table.getElementsByClass("odd");
				Elements newsEntries2 = table.getElementsByClass("even");
				newsEntries.addAll(extractTitleDateAndLink(newsEntries1));
				newsEntries.addAll(extractTitleDateAndLink(newsEntries2));
			}
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		}
		return newsEntries;
	}

	public static class ArticleOverviewEntry {
		public final String title;
		public final String link;
		public final String date;

		private ArticleOverviewEntry(String title, String link, String date) {
			this.title = title;
			this.link = link;
			this.date = date;
		}
	}

	private static List<ArticleOverviewEntry> extractTitleDateAndLink(
			Elements newsEntries) {
		List<ArticleOverviewEntry> result = new ArrayList<ArticleOverviewEntry>();
		for (Element entry : newsEntries) {
			Element entrydetails = entry.child(0).child(0);
			String link = MainActivity.DOMAIN + entrydetails.attr("href");
			String title = entrydetails.text();
			String date = entry.child(1).text();
			result.add(new ArticleOverviewEntry(title, link, date));
		}
		return result;
	}

	public static FullArticle parseFullArticle(String url) {
		FullArticle article = null;
		try {
			Document doc = Jsoup.connect(url).get();

			// Get title
			Element articleelement = doc.getElementsByTag("article").first();
			String title = articleelement.getElementsByTag("header").first().child(0)
					.text();

			// Get teaser
			Element content = articleelement.getElementsByClass("content").first();
			String teaser = content
					.getElementsByClass("artikel-vorlage-spalte-2").first()
					.child(0).text();

			// Get image and image description
			Element img = content.getElementsByTag("img").first();
			String imgsrc = MainActivity.DOMAIN + img.attr("src");
			String imgdescription = img.siblingElements().last().text();

			// Get article text
			List<String> text = new ArrayList<String>();
			Element articletext = content.getElementsByTag("tbody").last()
					.getElementsByTag("td").last();
			for (Element paragraph : articletext.children()) {
				text.add(paragraph.text());
			}
			article = new FullArticle(title, teaser, imgsrc,
					imgdescription, text);

		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		}
		return article;
	}

	public static class FullArticle {
		public final String title;
		public final String teaser;
		public final String imgsrc;
		public final String imgdescription;
		public final List<String> text;

		private FullArticle(String title, String teaser, String imgsrc,
				String imgdescription, List<String> text) {
			this.title = title;
			this.teaser = teaser;
			this.imgsrc = imgsrc;
			this.imgdescription = imgdescription;
			this.text = text;
		}
	}
}
