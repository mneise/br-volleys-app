package de.brvolleys.berlinrecyclingvolleys;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ArticleOverviewActivity extends Activity implements
		DownloadListener<List<ArticleOverviewEntry>> {
	public final static String EXTRA_LINK = "de.brvolleys.berlinrecyclingvolleys.LINK";
	public final static String EXTRA_ARTICLE_OVERVIEW_ID = "de.brvolleys.berlinrecyclingvolleys.ARTICLE_ID";
	public final static String DOMAIN = "http://www.br-volleys.de";
	private GifWebView mProgressView;
	private ArticleOverviewEntryDbAdapter mDbHelper;
	private List<ArticleOverviewEntry> mEntries = new ArrayList<ArticleOverviewEntry>();
	private SparseArray<String> mPaginationLinks = new SparseArray<String>();
	private Integer mPageKey = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_overview);

		mDbHelper = new ArticleOverviewEntryDbAdapter(this);
		mDbHelper.open();

		if (isConnected()) {

			// Start AsyncTask to fetch entries
			URL url = null;
			try {
				url = new URL(
						"http://br-volleys.de/index.php/br-volleys-archiv/artikel/2012-13.html");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			loadEntriesFromUrl(url);

			// Start AsyncTask to fetch pagination links
			DownloadPaginationLinksTask downloadPaginationLinksTask = new DownloadPaginationLinksTask(
					this);
			downloadPaginationLinksTask.execute(url);

		} else {
			List<ArticleOverviewEntry> entries = mDbHelper.getAllEntries();
			mEntries.addAll(entries);
			displayEntries(entries);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy(); // Always call the superclass method first
		mDbHelper.close();
	}

	public void loadEntriesFromUrl(URL url) {

		LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_loading_articles);
		mProgressView = new GifWebView(this,
				"file:///android_asset/loading-spinner.html");
		mProgressView.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		layout.addView(mProgressView);

		DownloadArticleTask<List<ArticleOverviewEntry>> downloadArticleTask = new DownloadArticleTask<List<ArticleOverviewEntry>>(
				this, new ArticleOverviewHtmlParser());
		downloadArticleTask.execute(url);
	}

	public void loadMoreEntries() {
		String link = this.mPaginationLinks.get(++mPageKey);
		URL url = null;
		try {
			url = new URL(link);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		loadEntriesFromUrl(url);
	}

	public List<ArticleOverviewEntry> saveNewEntries(
			List<ArticleOverviewEntry> fetchedEntries,
			List<ArticleOverviewEntry> savedEntries) {
		List<ArticleOverviewEntry> newEntries = new ArrayList<ArticleOverviewEntry>();
		for (ArticleOverviewEntry entry : fetchedEntries) {
			if (!savedEntries.contains(entry)) {
				entry.id = (int) mDbHelper.createArticleOverviewEntry(
						entry.title, DateConverter.getString(entry.date),
						entry.link);
				newEntries.add(entry);
			}
		}
		return newEntries;
	}

	public void enableLoadMoreEntriesButton() {
		Button button = (Button) findViewById(R.id.button_load_more_entries);
		button.setVisibility(View.VISIBLE);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				loadMoreEntries();
			}
		});
	}

	public void disableLoadMoreEntriesButton() {
		Button button = (Button) findViewById(R.id.button_load_more_entries);
		button.setVisibility(View.GONE);
	}

	public void displayEntries(List<ArticleOverviewEntry> entries) {
		// sort entries by date in descending order
		Collections.sort(entries);
		Collections.reverse(entries);

		LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_article_overview);

		for (ArticleOverviewEntry entry : entries) {
			displayEntry(entry, layout);
		}
	}

	public void displayEntry(ArticleOverviewEntry entry, LinearLayout layout) {
		// One LinearLayout per article
		LinearLayout entrylayout = new LinearLayout(this);
		entrylayout.setOrientation(1);
		entrylayout.setClickable(true);

		// Register OnClickListener and set Link and Context
		ArticleOverviewEntryOnClickListener listener = new ArticleOverviewEntryOnClickListener(
				entry.link, entry.id, this);
		entrylayout.setOnClickListener(listener);

		// Set date
		TextView date = new TextView(this);
		date.setTextSize(20);
		date.setText(DateConverter.getString(entry.date));
		entrylayout.addView(date);

		// Set title
		TextView title = new TextView(this);
		title.setTextSize(20);
		title.setText(entry.title);
		entrylayout.addView(title);

		layout.addView(entrylayout);
	}

	public List<ArticleOverviewEntry> removeOutdatedEntries(
			List<ArticleOverviewEntry> savedEntries,
			List<ArticleOverviewEntry> fetchedEntries) {
		List<ArticleOverviewEntry> updatedEntries = new ArrayList<ArticleOverviewEntry>();
		Collections.sort(fetchedEntries);
		Date oldestDate = fetchedEntries.get(0).date;
		for (ArticleOverviewEntry entry : savedEntries) {
			if (!fetchedEntries.contains(entry) & entry.date.after(oldestDate)) {
				mDbHelper.deleteArticleOverviewEntry(entry);
			} else {
				updatedEntries.add(entry);
			}
		}
		return updatedEntries;
	}

	@Override
	public void onPreExecute() {
		this.disableLoadMoreEntriesButton();
		mProgressView.setVisibility(View.VISIBLE);
		final ScrollView scrollview = (ScrollView) this
				.findViewById(R.id.scrollview);
		scrollview.post(new Runnable() {
			@Override
			public void run() {
				Boolean worked = scrollview.fullScroll(View.FOCUS_DOWN);
				System.out.print(worked);
			}
		});
	}

	@Override
	public void onPostExecute(List<ArticleOverviewEntry> result) {
		Collections.sort(result);
		Date dateAfter = result.get(0).date;
		Date dateBefore = result.get(result.size() - 1).date;

		List<ArticleOverviewEntry> savedEntries = mDbHelper
				.getAllEntriesBetweenTwoDates(dateAfter, dateBefore);
		List<ArticleOverviewEntry> entries = saveNewEntries(result,
				savedEntries);
		entries.addAll(removeOutdatedEntries(savedEntries, result));
		mProgressView.setVisibility(View.GONE);
		displayEntries(entries);
		if (mPageKey < mPaginationLinks.size()) {
			this.enableLoadMoreEntriesButton();
		}
	}

	public void onPostExecute(SparseArray<String> links) {
		this.mPaginationLinks = links;
	}

	public Boolean cancelTask(AsyncTask task) {
		task.cancel(true);
		return task.isCancelled();
	}

	private Boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public class ArticleOverviewEntryOnClickListener implements OnClickListener {

		String link = null;
		Integer articleId = null;
		Context context = null;

		public ArticleOverviewEntryOnClickListener(String link,
				Integer articleId, Context context) {
			this.link = link;
			this.context = context;
			this.articleId = articleId;
		}

		public void onClick(View v) {
			Intent intent = new Intent(this.context, FullArticleActivity.class);
			intent.putExtra(EXTRA_LINK, link);
			intent.putExtra(EXTRA_ARTICLE_OVERVIEW_ID, articleId);
			startActivity(intent);
		}
	}
}
