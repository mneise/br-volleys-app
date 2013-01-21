package de.brvolleys.berlinrecyclingvolleys;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FullArticleActivity extends Activity implements
		DownloadListener<FullArticle> {
	private ImageView mImageView;
	private ProgressDialog mProgressDialog;
	DownloadArticleTask<FullArticle> task;
	private FullArticleDbAdapter mDbHelper;
	private FullArticle mArticle;
	private Integer mArticleOverviewId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_full_article);

		mDbHelper = new FullArticleDbAdapter(this);
		mDbHelper.open();

		// Get the link from the intent
		Intent intent = getIntent();
		String link = intent.getStringExtra(ArticleOverviewActivity.EXTRA_LINK);

		// Get article id
		mArticleOverviewId = intent.getIntExtra(
				ArticleOverviewActivity.EXTRA_ARTICLE_OVERVIEW_ID, -1);

		mArticle = mDbHelper
				.getFullArticleByArticleOverviewId(mArticleOverviewId);
		if (mArticle == null) {

			// Create ProgressDialog
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setMessage(getString(R.string.loading_article));

			// Start AsyncTask
			task = new DownloadArticleTask<FullArticle>(this,
					new FullArticleHtmlParser());
			URL url = null;
			try {
				url = new URL(link);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			task.execute(url);
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
	public void onPause() {
		super.onPause(); // Always call the superclass method first

		// if article is not in db, insert article
		if (mArticle.id == null && mArticleOverviewId != -1) {
			mArticle.id = (int) mDbHelper.createFullArticle(mArticle.title,
					mArticle.teaser, mArticle.imgsrc, mArticle.imgdescription,
					mArticle.text, mArticleOverviewId);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy(); // Always call the superclass method first
		mDbHelper.close();
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

			// Set image
			mImageView = new ImageView(this);
			DownloadImageTask task = new DownloadImageTask(this);
			task.execute(article.imgsrc);
			layout.addView(mImageView);

			// Set imagedescription
			TextView imgdescriptionView = new TextView(this);
			imgdescriptionView.setText(article.imgdescription);
			layout.addView(imgdescriptionView);

			// Set text
			TextView textView = new TextView(this);
			textView.setText(article.text);
			layout.addView(textView);
		}
	}

	public void displayImage(Bitmap image) {
		mImageView.setImageBitmap(image);
	}

	@Override
	public void onPreExecute() {
		mProgressDialog.show();
	}

	@Override
	public void onPostExecute(FullArticle result) {
		mProgressDialog.cancel();
		displayArticle(result);
	}

	public Boolean cancelTask() {
		task.cancel(true);
		return task.isCancelled();
	}
}
