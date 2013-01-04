package de.brvolleys.berlinrecyclingvolleys;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.brvolleys.berlinrecyclingvolleys.BRVolleysHtmlParser.ArticleOverviewEntry;

//import de.brvolleys.berlinrecyclingvolleys.BRVolleysXmlParser.Entry;

public class MainActivity extends Activity {
	public final static String EXTRA_LINK = "de.brvolleys.berlinrecyclingvolleys.LINK";
	public final static String DOMAIN = "http://www.br-volleys.de";
	ProgressDialog progressdialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		progressdialog = new ProgressDialog(this);
		progressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressdialog.setMessage(getString(R.string.loading_articles));
		LoadArticleOverviewTask task = new LoadArticleOverviewTask();
		task.setActivityContext(this);
		task.execute("http://br-volleys.de/index.php/br-volleys-archiv/artikel/2012-13.html");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private class LoadArticleOverviewTask extends
			AsyncTask<String, Void, List<ArticleOverviewEntry>> {
		Context context = null;

		protected void setActivityContext(Context context) {
			this.context = context;
		}

		@Override
		protected List doInBackground(String... urls) {
			List entries = null;
			entries = BRVolleysHtmlParser.parseArticleOverview(urls[0]);

			return entries;
		}

		@Override
		protected void onPreExecute() {
			progressdialog.show();
		}

		@Override
		protected void onPostExecute(List<ArticleOverviewEntry> entries) {
			progressdialog.cancel();
			LinearLayout layout = (LinearLayout) findViewById(R.id.linearlayout);

			if (entries == null || entries.isEmpty()) {
				TextView textView = new TextView(this.context);
				textView.setTextSize(20);
				textView.setText(getString(R.string.no_articles));
				layout.addView(textView);
			} else {

				for (ArticleOverviewEntry entry : entries) {

					LinearLayout entrylayout = new LinearLayout(this.context);
					entrylayout.setOrientation(1);
					entrylayout.setClickable(true);
					NewsOnClickListener listener = new NewsOnClickListener(
							entry.link, this.context);
					entrylayout.setOnClickListener(listener);

					TextView date = new TextView(this.context);
					date.setTextSize(20);
					date.setText(entry.date);
					entrylayout.addView(date);

					TextView title = new TextView(this.context);
					title.setTextSize(20);
					title.setText(entry.title);
					entrylayout.addView(title);

					layout.addView(entrylayout);

				}
			}
		}
	}

	public class NewsOnClickListener implements OnClickListener {

		String link = null;
		Context context = null;

		public NewsOnClickListener(String link, Context context) {
			this.link = link;
			this.context = context;
		}

		public void onClick(View v) {
			Intent intent = new Intent(this.context,
					DisplayFullArticleActivity.class);
			intent.putExtra(EXTRA_LINK, link);
			startActivity(intent);
		}
	}
}
