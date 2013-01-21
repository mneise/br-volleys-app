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
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ArticleOverviewActivity extends Activity implements
		DownloadListener<List<ArticleOverviewEntry>> {
	public final static String EXTRA_LINK = "de.brvolleys.berlinrecyclingvolleys.LINK";
	public final static String EXTRA_ARTICLE_OVERVIEW_ID = "de.brvolleys.berlinrecyclingvolleys.ARTICLE_ID";
	public final static String DOMAIN = "http://www.br-volleys.de";
	private GifWebView mProgressView;
	private DownloadArticleTask<List<ArticleOverviewEntry>> mTask;
	private ArticleOverviewEntryDbAdapter mDbHelper;
	private List<ArticleOverviewEntry> mEntries = new ArrayList<ArticleOverviewEntry>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_overview);

		mDbHelper = new ArticleOverviewEntryDbAdapter(this);
		mDbHelper.open();

		if (isConnected()) {

			LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_article_overview_new);

			mProgressView = new GifWebView(this,
					"file:///android_asset/loading-spinner.html");
			mProgressView.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			layout.addView(mProgressView);

			// Start AsyncTask
			mTask = new DownloadArticleTask<List<ArticleOverviewEntry>>(this,
					new ArticleOverviewHtmlParser());
			URL url = null;
			try {
				url = new URL(
						"http://br-volleys.de/index.php/br-volleys-archiv/artikel/2012-13.html");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			mTask.execute(url);
		} else {
			List<ArticleOverviewEntry> entries = loadEntriesFromDb();
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

	public List<ArticleOverviewEntry> loadEntriesFromDb() {
		Cursor cursor = mDbHelper.getAllArticleOverviewEntries();

		List<ArticleOverviewEntry> entries = new ArrayList<ArticleOverviewEntry>();

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Integer id = cursor
					.getInt(cursor
							.getColumnIndexOrThrow(ArticleOverviewEntryDbAdapter.ROW_ID));
			String title = cursor
					.getString(cursor
							.getColumnIndexOrThrow(ArticleOverviewEntryDbAdapter.TITLE));
			String date = cursor.getString(cursor
					.getColumnIndexOrThrow(ArticleOverviewEntryDbAdapter.DATE));
			String link = cursor.getString(cursor
					.getColumnIndexOrThrow(ArticleOverviewEntryDbAdapter.LINK));
			entries.add(new ArticleOverviewEntry(id, title, link, DateConverter
					.getDate(date)));
			cursor.moveToNext();
		}
		return entries;
	}

	public List<ArticleOverviewEntry> getNewEntries(
			List<ArticleOverviewEntry> fetchedEntries,
			List<ArticleOverviewEntry> savedEntries) {
		List<ArticleOverviewEntry> newEntries = new ArrayList<ArticleOverviewEntry>();
		for (ArticleOverviewEntry entry : newEntries) {
			if (!savedEntries.contains(entry)) {
				entry.id = (int) mDbHelper.createArticleOverviewEntry(
						entry.title, DateConverter.getString(entry.date),
						entry.link);
				newEntries.add(entry);
			}
		}
		return newEntries;
	}

	public void displayNewEntries(List<ArticleOverviewEntry> entries) {
		LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_article_overview_new);

		if (entries.size() > 0) {
			// sort entries by date in descending order
			Collections.sort(entries);
			Collections.reverse(entries);

			for (ArticleOverviewEntry entry : entries) {
				displayEntry(entry, layout);
			}
		} else {
			layout.setVisibility(View.GONE);
		}
	}

	public void displayEntries(List<ArticleOverviewEntry> entries) {
		// sort entries by date in descending order
		Collections.sort(entries);
		Collections.reverse(entries);

		LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_article_overview_old);

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

	public List<ArticleOverviewEntry> removeNotFoundEntries(
			List<ArticleOverviewEntry> savedEntries,
			List<ArticleOverviewEntry> fetchedEntries) {
		List<ArticleOverviewEntry> updatedEntries = new ArrayList<ArticleOverviewEntry>();
		Collections.sort(fetchedEntries);
		Date lastDate = fetchedEntries.get(0).date;
		for (ArticleOverviewEntry entry : savedEntries) {
			if (!fetchedEntries.contains(entry) & entry.date.after(lastDate)) {
				mDbHelper.deleteArticleOverviewEntry(entry);
			} else {
				updatedEntries.add(entry);
			}
		}
		return updatedEntries;
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

	@Override
	public void onPreExecute() {
		mProgressView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onPostExecute(List<ArticleOverviewEntry> result) {
		List<ArticleOverviewEntry> savedEntries = loadEntriesFromDb();
		mProgressView.setVisibility(View.GONE);
		List<ArticleOverviewEntry> entries = getNewEntries(result, savedEntries);
		entries.addAll(removeNotFoundEntries(savedEntries, result));
		displayEntries(entries);
	}

	public Boolean cancelTask() {
		mTask.cancel(true);
		return mTask.isCancelled();
	}

	private Boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}
}
