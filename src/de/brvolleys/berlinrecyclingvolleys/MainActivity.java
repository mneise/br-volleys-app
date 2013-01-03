package de.brvolleys.berlinrecyclingvolleys;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.brvolleys.berlinrecyclingvolleys.BRVolleysNewsParser.Entry;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		DownloadXmlTask task = new DownloadXmlTask();
		task.setActivityContext(this);
		task.execute("http://www.berlin-recycling-volleys.de/index.php/br-volleys-archiv/artikel/2012-13.feed?type=rss");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private class DownloadXmlTask extends AsyncTask<String, Void, List<Entry>> {
		Context context = null;

		protected void setActivityContext(Context context) {
			this.context = context;
		}

		@Override
		protected List<Entry> doInBackground(String... urls) {
			List<Entry> entries = null;
			try {
				entries = loadXmlFromNetwork(urls[0]);
			} catch (IOException e) {
				System.err.println("Caught IOException: " + e.getMessage());
			} catch (XmlPullParserException e) {
				System.err.println("XmlPullParserException: " + e.getMessage());
			}

			return entries;
		}

		@Override
		protected void onPostExecute(List<Entry> entries) {
			setContentView(R.layout.activity_main);
			// Displays the HTML string in the UI

			LinearLayout layout = (LinearLayout) findViewById(R.id.linearlayout);

			if (entries == null) {
				TextView textView = new TextView(this.context);
				textView.setTextSize(20);
				textView.setText("Es konnten keine Nachrichten gefunden werden.");
				layout.addView(textView);
			} else {

				for (Entry entry : entries) {

					TextView title = new TextView(this.context);
					title.setTextSize(20);
					title.setText(entry.title);
					layout.addView(title);

					if (entry.imgsrc != null) {
						ImageView img = new ImageView(this.context);
						try {
							URL url = new URL(entry.imgsrc);
							Bitmap bmp = BitmapFactory.decodeStream(url
									.openConnection().getInputStream());
							img.setImageBitmap(bmp);
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						layout.addView(img);
					}

					TextView description = new TextView(this.context);
					description.setText(entry.description);
					layout.addView(description);

					TextView link = new TextView(this.context);
					link.setText(Html.fromHtml("<a href='" + entry.link
							+ "'>Kompletter Artikel</a>"));
					link.setMovementMethod(LinkMovementMethod.getInstance());
					layout.addView(link);

				}
			}
		}
	}

	// Uploads XML, parses it, and combines it with
	// HTML markup. Returns HTML string.
	private List<Entry> loadXmlFromNetwork(String urlString)
			throws XmlPullParserException, IOException {
		InputStream stream = null;
		// Instantiate the parser
		BRVolleysNewsParser newsparser = new BRVolleysNewsParser();
		List<Entry> entries = null;
		Calendar rightNow = Calendar.getInstance();
		DateFormat formatter = new SimpleDateFormat("MMM dd h:mmaa");

		StringBuilder htmlString = new StringBuilder();
		htmlString.append("<h3>News</h3>");
		htmlString.append("<em>Geladen am: "
				+ formatter.format(rightNow.getTime()) + "</em>");

		try {
			stream = downloadUrl(urlString);
			entries = newsparser.parse(stream);
			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (stream != null) {
				stream.close();
			}
		}

		// BRVolleysNewsParser returns a List (called "entries") of Entry
		// objects.
		// Each Entry object represents a single post in the XML feed.
		// This section processes the entries list to combine each entry with
		// HTML markup.
		// Each entry is displayed in the UI as a link that optionally includes
		// a text summary.
		// for (Entry entry : entries) {
		// htmlString.append("<p><a href='");
		// htmlString.append(entry.link);
		// htmlString.append("'>" + entry.title + "</a></p>");
		// htmlString.append(entry.description);
		// }
		// String result = htmlString.toString();
		// return result;
		return entries;
	}

	// Given a string representation of a URL, sets up a connection and gets
	// an input stream.
	private InputStream downloadUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		// Starts the query
		conn.connect();
		InputStream stream = conn.getInputStream();
		return stream;
	}
}
