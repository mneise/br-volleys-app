package de.brvolleys.berlinrecyclingvolleys;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.util.SparseArray;

public class DownloadPaginationLinksTask extends
		AsyncTask<URL, Void, SparseArray<String>> {
	private volatile boolean running = true;

	private ArticleOverviewActivity mActivity;

	public DownloadPaginationLinksTask(ArticleOverviewActivity activity) {
		this.mActivity = activity;
	}

	@Override
	protected void onCancelled() {
		running = false;
	}

	@Override
	public SparseArray<String> doInBackground(URL... params) {
		while (running) {
			Document doc = null;
			SparseArray<String> links = new SparseArray<String>();
			links.append(1, params[0].toString());
			try {
				doc = Jsoup.connect(params[0].toString()).get();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (doc != null) {
				Elements pagination = doc.getElementsByClass("pagination");
				Elements children = pagination.get(0).getElementsByTag("a");
				for (Element child : children) {
					if (!(child.hasClass("next") || child.hasClass("last"))) {
						int index = Integer.parseInt(child.text());
						links.append(index, ArticleOverviewActivity.DOMAIN + child.attr("href"));
					}
				}
				return links;
			}
			return null;
		}
		return null;
	}

	@Override
	public void onPostExecute(SparseArray<String> result) {
		this.mActivity.onPostExecute(result);
	}
}
