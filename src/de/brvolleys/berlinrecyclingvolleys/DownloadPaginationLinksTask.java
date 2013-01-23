package de.brvolleys.berlinrecyclingvolleys;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.util.SparseArray;

public class DownloadPaginationLinksTask extends AsyncTask<URL, Void, String[]> {
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
	public String[] doInBackground(URL... params) {
		while (running) {
			Document doc = null;
			String[] links = null;
			try {
				doc = Jsoup.connect(params[0].toString()).get();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (doc != null) {
				Elements pagination = doc.getElementsByClass("pagination");
				Elements children = pagination.get(0).getElementsByTag("a");
				SparseArray<String> tempLinks = new SparseArray<String>();
				tempLinks.append(0, params[0].toString());
				for (Element child : children) {
					if (!(child.hasClass("next") || child.hasClass("last"))) {
						int index = Integer.parseInt(child.text()) - 1;
						tempLinks.append(index, ArticleOverviewActivity.DOMAIN
								+ child.attr("href"));
					}
				}
				links = new String[tempLinks.size()];
				for (int i = 0; i < tempLinks.size(); i++) {
					links[i] = tempLinks.get(i);
				}
				return links;
			}
			return null;
		}
		return null;
	}

	@Override
	public void onPostExecute(String[] result) {
		mActivity.setPaginationLinks(result);
		if (mActivity.isAllowedToEnableButton()) {
			mActivity.enableButton();
		}
	}
}
