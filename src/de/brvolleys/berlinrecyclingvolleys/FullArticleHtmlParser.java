package de.brvolleys.berlinrecyclingvolleys;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
		String title = "";
		String teaser = "";
		String imgsrc = "";
		String imgdescription = "";
		String text = "";

		Elements articles = doc.getElementsByTag("article");
		if (!articles.isEmpty()) {
			Element articleelement = doc.getElementsByTag("article").first();

			// Get title
			Elements header = articleelement.getElementsByTag("header");
			if (!header.isEmpty()) {
				title = articleelement.getElementsByTag("header").first()
						.text();
			}

			// Get teaser
			Elements contentElements = articleelement
					.getElementsByClass("content");
			if (!contentElements.isEmpty()) {
				Element content = contentElements.first();
				Element contentChild = content.child(0);
				if (contentChild != null) {
					teaser = contentChild.text();
				}

				// Get image and image description
				Elements imgs = content.getElementsByTag("img");
				if (!imgs.isEmpty()) {
					Element img = imgs.first();
					imgsrc = ArticleOverviewActivity.DOMAIN + img.attr("src");

					Elements imgSiblings = img.siblingElements();
					if (!imgSiblings.isEmpty()) {
						imgdescription = imgSiblings.last().text();
					}
				}

				// Get article text
				Elements tBodies = content.getElementsByTag("tbody");
				if (!tBodies.isEmpty()) {
					Elements tds = tBodies.last().getElementsByTag("td");
					if (!tds.isEmpty()) {
						StringBuilder sb = new StringBuilder();
						Element paragraphs = tds.last();
						for (Element paragraph : paragraphs.children()) {
							sb.append(paragraph.text());
							sb.append("\n\n");
						}
						text = sb.toString();
					}
				}
			}
		}

		article = new FullArticle(title, teaser, imgsrc, imgdescription, text);

		return article;
	}
}
