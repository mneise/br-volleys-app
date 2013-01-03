package de.brvolleys.berlinrecyclingvolleys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;

public class DisplayFullArticleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_full_article);

		// Get the link from the intent
		Intent intent = getIntent();
		String link = intent.getStringExtra(MainActivity.EXTRA_LINK);

		// Create the view
		WebView view = new WebView(this);
		view.loadUrl(link);
		setContentView(view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_display_full_article, menu);
		return true;
	}

}
