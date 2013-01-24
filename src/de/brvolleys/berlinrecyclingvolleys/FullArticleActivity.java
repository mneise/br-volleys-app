package de.brvolleys.berlinrecyclingvolleys;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FullArticleActivity extends Activity implements
		DownloadListener<FullArticle> {
	private ImageView mImageView = null;
	private WebView mProgressView = null;
	private DownloadArticleTask<FullArticle> mDownlaodArticleTask = null;
	private FullArticleDbAdapter mDbHelper = null;
	private FullArticle mArticle = null;
	private AlertDialog mAlertDialog = null;
	private String mLink = null;
	private DownloadImageTask mDownloadImageTask = null;
	private int mArticleOverviewId = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_full_article);

		mDbHelper = new FullArticleDbAdapter(this);

		// Get link from intent
		Intent intent = getIntent();
		mLink = intent.getStringExtra(ArticleOverviewActivity.EXTRA_LINK);

		// Get article id
		mArticleOverviewId = intent.getIntExtra(
				ArticleOverviewActivity.EXTRA_ARTICLE_OVERVIEW_ID, -1);

		mArticle = mDbHelper
				.getFullArticleByArticleOverviewId(mArticleOverviewId);
		if (mArticle == null) {

			if (isConnected()) {
				initializeProgressView();
				downloadArticle();
			} else {
				showDialog();
			}
		} else {
			displayArticle(mArticle);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_display_full_article, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume(); // Always call the superclass method first
		if (mArticle == null && isConnected()) {
			initializeProgressView();
			downloadArticle();
		}
	}

	@Override
	public void onPause() {
		super.onPause(); // Always call the superclass method first

		if (mArticle == null) {
			return;
		}

		// if article is not in db, insert article
		if (mArticle.id == null && mArticle.articleOverviewId != -1) {
			mArticle.id = (int) mDbHelper.createFullArticle(mArticle);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy(); // Always call the superclass method first
		cancelTask(mDownlaodArticleTask);
		cancelTask(mDownloadImageTask);
	}

	@Override
	public void onPreExecute() {
		enableProgressView();
	}

	@Override
	public void onPostExecute(FullArticle result) {
		disableProgressView();
		if (result != null) {
			result.articleOverviewId = mArticleOverviewId;
			displayArticle(result);
		}
	}

	public void initializeProgressView() {
		// Initialize mProgressView
		mProgressView = (WebView) findViewById(R.id.webview_loading_spinner_article);
		mProgressView
				.loadUrl("file:///android_asset/loading-spinner-article.html");
	}

	public void downloadArticle() {
		// Start AsyncTask
		mDownlaodArticleTask = new DownloadArticleTask<FullArticle>(this,
				new FullArticleHtmlParser());
		URL url = null;
		try {
			url = new URL(mLink);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		mDownlaodArticleTask.execute(url);
	}

	public void displayArticle(FullArticle article) {
		mArticle = article;

		// Create the view
		LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_full_article);
		layout.removeAllViews();

		if (article == null) {
			TextView textView = new TextView(this);
			textView.setTextSize(20);
			textView.setText(getString(R.string.no_article));
			layout.addView(textView);
		} else {

			// Set title
			TextView titleView = new TextView(this);
			titleView.setTextSize(20);
			titleView.setText(article.title);
			layout.addView(titleView);

			// Set teaser
			TextView teaserView = new TextView(this);
			teaserView.setTypeface(null, Typeface.BOLD);
			teaserView.setText(article.teaser);
			layout.addView(teaserView);

			if (isConnected() && article.imgsrc.length() > 0) {
				// Set image
				mImageView = new ImageView(this);
				mDownloadImageTask = new DownloadImageTask(this);
				mDownloadImageTask.execute(article.imgsrc);
				layout.addView(mImageView);

				// Set imagedescription
				TextView imgdescriptionView = new TextView(this);
				imgdescriptionView.setText(article.imgdescription);
				layout.addView(imgdescriptionView);
			}

			// Set text
			TextView textView = new TextView(this);
			textView.setText(article.text);
			layout.addView(textView);
		}
	}

	public void showDialog() {
		if (mAlertDialog != null && mAlertDialog.isShowing())
			return;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_internet_connection_title);
		builder.setMessage(R.string.dialog_internet_connection_message);
		builder.setPositiveButton(R.string.dialog_internet_connection_enable,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						startActivity(new Intent(
								Settings.ACTION_WIRELESS_SETTINGS));
					}
				}).setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						finish();
					}
				});
		builder.setCancelable(false);
		mAlertDialog = builder.create();
		mAlertDialog.show();
	}

	public void displayImage(Bitmap image) {
		if (image != null) {
			mImageView.setImageBitmap(image);
		}
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
		return cancelTask(mDownlaodArticleTask)
				&& cancelTask(mDownloadImageTask);
	}

	public Boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}
}
