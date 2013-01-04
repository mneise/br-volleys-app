package de.brvolleys.berlinrecyclingvolleys;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.brvolleys.berlinrecyclingvolleys.BRVolleysHtmlParser.FullArticle;

public class DisplayFullArticleActivity extends Activity {
	ProgressDialog progressdialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_full_article);

		// Get the link from the intent
		Intent intent = getIntent();
		String link = intent.getStringExtra(MainActivity.EXTRA_LINK);

		// Create ProgressDialog
		progressdialog = new ProgressDialog(this);
		progressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressdialog.setMessage(getString(R.string.loading_article));

		// Start AsyncTask
		LoadArticleTask task = new LoadArticleTask();
		task.setActivityContext(this);
		task.execute(link);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_display_full_article, menu);
		return true;
	}

	private class LoadArticleTask extends AsyncTask<String, Void, FullArticle> {
		Context context = null;

		protected void setActivityContext(Context context) {
			this.context = context;
		}

		@Override
		protected FullArticle doInBackground(String... urls) {
			return BRVolleysHtmlParser.parseFullArticle(urls[0]);
		}

		@Override
		protected void onPreExecute() {
			progressdialog.show();
		}

		@Override
		protected void onPostExecute(FullArticle article) {
			progressdialog.cancel();

			// Create the view
			LinearLayout layout = (LinearLayout) findViewById(R.id.linearlayou_fullarticle);

			if (article == null) {
				TextView textView = new TextView(this.context);
				textView.setTextSize(20);
				textView.setText(getString(R.string.no_article));
				layout.addView(textView);
			} else {

				TextView titleView = new TextView(this.context);
				titleView.setTextSize(20);
				titleView.setText(article.title);
				layout.addView(titleView);

				TextView teaserView = new TextView(this.context);
				teaserView.setTypeface(null, Typeface.BOLD);
				teaserView.setText(article.teaser);
				layout.addView(teaserView);

				try {
					ImageView imgView = new ImageView(this.context);
					URL url = new URL(article.imgsrc);
					Bitmap bmp = BitmapFactory.decodeStream(url
							.openConnection().getInputStream());
					imgView.setImageBitmap(bmp);
					layout.addView(imgView);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				TextView imgdescriptionView = new TextView(this.context);
				imgdescriptionView.setText(article.imgdescription);
				layout.addView(imgdescriptionView);

				TextView textView = new TextView(this.context);
				StringBuilder sb = new StringBuilder();
				for (String paragraph : article.text) {
					sb.append(paragraph);
					sb.append("\n\n ");
				}
				textView.setText(sb.toString());
				layout.addView(textView);
			}
		}
	}
}
