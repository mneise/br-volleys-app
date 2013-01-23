package de.brvolleys.berlinrecyclingvolleys;

import java.io.IOException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	private FullArticleActivity mActivity;
	private volatile boolean running = true;

	public DownloadImageTask(FullArticleActivity activity) {
		mActivity = activity;
	}

	@Override
	protected void onCancelled() {
		running = false;
	}

	public Bitmap doInBackground(String... imgsrc) {
		while (running) {
			Bitmap bitmap = null;
			try {
				URL url = new URL(imgsrc[0]);
				bitmap = BitmapFactory.decodeStream(url.openConnection()
						.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bitmap;
		}
		return null;
	}

	public void onPostExecute(Bitmap result) {
		mActivity.displayImage(result);
	}
}