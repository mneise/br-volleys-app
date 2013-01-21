package de.brvolleys.berlinrecyclingvolleys;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class FullArticleHtmlParser implements HtmlParser<FullArticle> {

	public FullArticle parse(InputStream input) {
		Document doc = null;
		try {
			doc = Jsoup.parse(input, "UTF-8", "");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return parse(doc);
	}

	public FullArticle parse(String htmlContent) {
		return parse(Jsoup.parse(htmlContent));
	}

	@Override
	public FullArticle parse(URL url) {

		Document doc = null;
		try {
			doc = Jsoup.connect(url.toString()).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return parse(doc);
	}

	public FullArticle parse(Document doc) {
		if (doc == null) {
			return null;
		}
		FullArticle article = null;

		// Get title
		Element articleelement = doc.getElementsByTag("article").first();
		String title = articleelement.getElementsByTag("header").first().text();

		// Get teaser
		Element content = articleelement.getElementsByClass("content").first();
		String teaser = content.child(0).text();

		// Get image and image description
		Element img = content.getElementsByTag("img").first();
		String imgsrc = ArticleOverviewActivity.DOMAIN + img.attr("src");
		String imgdescription = img.siblingElements().last().text();

		// Get article text
		StringBuilder sb = new StringBuilder();
		Element articletext = content.getElementsByTag("tbody").last()
				.getElementsByTag("td").last();
		for (Element paragraph : articletext.children()) {
			sb.append(paragraph.text());
			sb.append("\n\n");
		}
		article = new FullArticle(title, teaser, imgsrc, imgdescription,
				sb.toString());

		return article;
	}
}
