package de.brvolleys.berlinrecyclingvolleys;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ArticleOverviewActivity extends Activity {
	public final static String EXTRA_LINK = "de.brvolleys.berlinrecyclingvolleys.LINK";
	public final static String EXTRA_ARTICLE_OVERVIEW_ID = "de.brvolleys.berlinrecyclingvolleys.ARTICLE_ID";
	public final static String DOMAIN = "http://www.br-volleys.de";
	public final static String START_URL = "http://br-volleys.de/index.php/br-volleys-archiv/artikel/2012-13.html";
	private List<ArticleOverviewEntry> mEntries = new ArrayList<ArticleOverviewEntry>();
	private SparseArray<String> mPaginationLinks = new SparseArray<String>();
	private Integer mPageKey = 1;
	private ArticleOverviewEntryDbAdapter mDbHelper = null;
	private WebView mProgressView = null;
	private Button mButton = null;
	private Boolean wasConnected = false;
	private DownloadPaginationLinksTask mDownloadPaginationLinksTask = null;
	private DownloadArticleTask<List<ArticleOverviewEntry>> mDownloadArticleTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_overview);

		// Initialize mDbHelper
		mDbHelper = new ArticleOverviewEntryDbAdapter(this);
		mDbHelper.open();

		// Initialize mProgressView
		mProgressView = (WebView) findViewById(R.id.webview_loading_spinner_entries);
		mProgressView
				.loadUrl("file:///android_asset/loading-spinner-overview.html");

		// Initialize mButton
		mButton = (Button) findViewById(R.id.button_load_more_entries);
		mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				loadMoreEntries();
			}
		});

		if (isConnected()) {

			wasConnected = true;

			// Start AsyncTask to fetch entries
			URL url = null;
			try {
				url = new URL(START_URL);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			loadEntriesFromUrl(url);

			// Start AsyncTask to fetch pagination links
			mDownloadPaginationLinksTask = new DownloadPaginationLinksTask(this);
			mDownloadPaginationLinksTask.execute(url);

		} else {
			// Load entries from db
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
	public void onResume() {
		super.onResume();

		if (!isConnected() && wasConnected) {
			wasConnected = false;

			// Remove all previous entries and disable button
			LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_article_overview);
			layout.removeAllViews();
			disableButton();

			// Load entries from db
			List<ArticleOverviewEntry> entries = mDbHelper.getAllEntries();
			mEntries.addAll(entries);
			displayEntries(entries);
		}

		if (isConnected() && !wasConnected) {
			wasConnected = true;

			// Remove all previous entries and disable button
			LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_article_overview);
			layout.removeAllViews();

			// Start AsyncTask to fetch entries
			URL url = null;
			try {
				url = new URL(START_URL);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			loadEntriesFromUrl(url);

			// Start AsyncTask to fetch pagination links
			DownloadPaginationLinksTask downloadPaginationLinksTask = new DownloadPaginationLinksTask(
					this);
			downloadPaginationLinksTask.execute(url);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy(); // Always call the superclass method first
		cancelTask(mDownloadArticleTask);
		cancelTask(this.mDownloadPaginationLinksTask);
		mDbHelper.close();
	}

	public void loadEntriesFromUrl(URL url) {

		ArticleOverviewDownloadListener downloadListener = new ArticleOverviewDownloadListener(
				mDbHelper, this);
		mDownloadArticleTask = new DownloadArticleTask<List<ArticleOverviewEntry>>(
				downloadListener, new ArticleOverviewHtmlParser());
		mDownloadArticleTask.execute(url);
	}

	public void loadMoreEntries() {
		String link = mPaginationLinks.get(++mPageKey);
		URL url = null;
		try {
			url = new URL(link);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		loadEntriesFromUrl(url);
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

	public void scrollDown() {
		final ScrollView scrollview = (ScrollView) findViewById(R.id.scrollview);
		scrollview.post(new Runnable() {
			@Override
			public void run() {
				scrollview.fullScroll(View.FOCUS_DOWN);
			}
		});
	}

	public Boolean isAllowedToEnableButton() {
		if (mPageKey < mPaginationLinks.size()
				&& mProgressView.getVisibility() != View.VISIBLE) {
			return true;
		}
		return false;
	}

	public void enableButton() {
		mButton.setVisibility(View.VISIBLE);
	}

	public void disableButton() {
		mButton.setVisibility(View.GONE);
	}

	public void enableProgressView() {
		mProgressView.setVisibility(View.VISIBLE);
	}

	public void disableProgressView() {
		mProgressView.setVisibility(View.GONE);
	}

	public Boolean cancelTask(AsyncTask task) {
		if (task != null) {
			task.cancel(true);
			return task.isCancelled();
		}
		return true;
	}

	public Boolean cancelTasks() {
		return cancelTask(mDownloadArticleTask)
				&& cancelTask(this.mDownloadPaginationLinksTask);
	}

	public void setPaginationLinks(SparseArray<String> paginationLinks) {
		mPaginationLinks = paginationLinks;
	}

	public Boolean isConnected() {
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
			Intent intent = new Intent(context, FullArticleActivity.class);
			intent.putExtra(EXTRA_LINK, link);
			intent.putExtra(EXTRA_ARTICLE_OVERVIEW_ID, articleId);
			startActivity(intent);
		}
	}
}
