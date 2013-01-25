package de.brvolleys.berlinrecyclingvolleys;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ArticleOverviewActivity extends Activity {
	private static final String TAG = "ArticleOverviewActivity";
	public final static String EXTRA_LINK = "de.brvolleys.berlinrecyclingvolleys.LINK";
	public final static String EXTRA_ARTICLE_OVERVIEW_ID = "de.brvolleys.berlinrecyclingvolleys.ARTICLE_ID";
	public final static String DOMAIN = "http://www.br-volleys.de";
	public final static String START_URL = "http://br-volleys.de/index.php/br-volleys-archiv/artikel/2012-13.html";

	private ScrollView mScrollView = null;
	private List<ArticleOverviewEntry> mEntries = null;
	private String[] mPaginationLinks = null;
	private Integer mPageKey = 0;
	private ArticleOverviewEntryDbAdapter mDbHelper = null;
	private WebView mProgressView = null;
	private Button mButton = null;
	private DownloadPaginationLinksTask mDownloadPaginationLinksTask = null;
	private DownloadArticleTask<List<ArticleOverviewEntry>> mDownloadArticleTask = null;

	public enum State {
		LOADING, WAITING
	}

	public State state = State.LOADING;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_overview);

		// get scrollView
		mScrollView = (ScrollView) findViewById(R.id.scrollview);

		// Initialize mDbHelper
		mDbHelper = new ArticleOverviewEntryDbAdapter(this);

		// Initialize mProgressView
		mProgressView = (WebView) findViewById(R.id.webview_loading_spinner_article_overview);
		mProgressView
				.loadUrl("file:///android_asset/loading-spinner-overview.html");

		// Initialize mButton
		mButton = (Button) findViewById(R.id.button_load_more_entries);
		mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				URL url = getUrl(mPaginationLinks[++mPageKey]);
				loadEntriesFromUrl(url);
			}
		});

		float[] scrollPosition = null;

		if (savedInstanceState != null) {
			mEntries = savedInstanceState.getParcelableArrayList("mEntries");
			mPaginationLinks = savedInstanceState
					.getStringArray("mPaginationLinks");
			mPageKey = savedInstanceState.getInt("mPageKey");
			state = Enum.valueOf(State.class,
					savedInstanceState.getString("state"));
			scrollPosition = savedInstanceState
					.getFloatArray("mScrollPosition");
		}

		if (isConnected()) {

			URL startUrl = getUrl(START_URL);
			if (mPaginationLinks == null) {
				// Start AsyncTask to fetch pagination links
				mDownloadPaginationLinksTask = new DownloadPaginationLinksTask(
						this);
				mDownloadPaginationLinksTask.execute(startUrl);
			}

			switch (state) {
			case LOADING:
				if (mEntries == null) {
					// Start AsyncTask to fetch entries
					loadEntriesFromUrl(startUrl);
				} else {
					displayEntries(mEntries);
					URL url = getUrl(mPaginationLinks[mPageKey]);
					loadEntriesFromUrl(url);
				}
				break;
			case WAITING:
				displayEntries(mEntries);
				if (isAllowedToEnableButton()) {
					enableButton();
				}
				break;
			}

		} else {
			// Load entries from db
			List<ArticleOverviewEntry> entries = mDbHelper.getAllEntries();
			addNewEntries(entries);
			displayEntries(entries);
		}

		if (scrollPosition != null) {
			final float[] scrollPositionTmp = scrollPosition;
			final RelativeLayout layout = (RelativeLayout) findViewById(R.id.relative_layout_article_overview);
			ViewTreeObserver vto = layout.getViewTreeObserver();
			vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

				@SuppressLint("NewApi")
				@Override
				public void onGlobalLayout() {
					scroll(scrollPositionTmp, layout.getHeight());
					ViewTreeObserver obs = layout.getViewTreeObserver();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						obs.removeOnGlobalLayoutListener(this);
						Log.v(TAG, "Device is using API Level 16 or higher");
					} else {
						obs.removeGlobalOnLayoutListener(this);
						Log.v(TAG, "Device is using API Level 15 or lower");
					}
				}

			});

		}
	}

	public void scroll(final float[] scrollPosition, int height) {
		final int x = (int) scrollPosition[0];
		final int y = (int) (scrollPosition[1] * height);
		Runnable scroll = new Runnable() {
			public void run() {
				mScrollView.scrollTo(x, y);
			}
		};
		mScrollView.post(scroll);
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
	}

	@Override
	public void onDestroy() {
		super.onDestroy(); // Always call the superclass method first
		cancelTask(mDownloadArticleTask);
		cancelTask(this.mDownloadPaginationLinksTask);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save UI state changes to the savedInstanceState.
		savedInstanceState.putParcelableArrayList("mEntries",
				(ArrayList<ArticleOverviewEntry>) mEntries);
		savedInstanceState.putStringArray("mPaginationLinks", mPaginationLinks);
		savedInstanceState.putInt("mPageKey", mPageKey);
		savedInstanceState.putString("state", state.name());
		float height = mScrollView.getChildAt(0).getHeight();
		float yPosition = mScrollView.getScrollY();
		float percentage = yPosition / height;
		savedInstanceState.putFloatArray("mScrollPosition", new float[] {
				mScrollView.getScrollX(), percentage });

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	public void loadEntriesFromUrl(URL url) {
		Log.v(TAG, "PageKey: " + mPageKey);

		ArticleOverviewDownloadListener downloadListener = new ArticleOverviewDownloadListener(
				mDbHelper, this);
		mDownloadArticleTask = new DownloadArticleTask<List<ArticleOverviewEntry>>(
				downloadListener, new ArticleOverviewHtmlParser());
		mDownloadArticleTask.execute(url);
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
		if (mPaginationLinks != null) {
			if (mPageKey < mPaginationLinks.length - 1
					&& mProgressView.getVisibility() != View.VISIBLE) {
				return true;
			}
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

	public void setPaginationLinks(String[] paginationLinks) {
		mPaginationLinks = paginationLinks;
	}

	public void addNewEntries(List<ArticleOverviewEntry> entries) {
		if (mEntries == null) {
			mEntries = new ArrayList<ArticleOverviewEntry>();
		}
		mEntries.addAll(entries);
	}

	public Boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public URL getUrl(String src) {
		URL url = null;
		try {
			url = new URL(src);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url;
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
