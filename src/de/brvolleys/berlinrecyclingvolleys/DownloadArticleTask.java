package de.brvolleys.berlinrecyclingvolleys;

import java.net.URL;

import android.os.AsyncTask;

public class DownloadArticleTask<T> extends AsyncTask<URL, Void, T> {
	private volatile boolean running = true;
	
	private DownloadListener<T> mListener;
	private HtmlParser<T> mParser;

	public DownloadArticleTask(DownloadListener<T> listener, HtmlParser<T> parser) {
		this.mListener = listener;
		this.mParser = parser;
	}

	@Override
	public void onPreExecute() {
		mListener.onPreExecute();
	}
	
	@Override
    protected void onCancelled() {
        running = false;
    }

	@Override
	public T doInBackground(URL... params) {
		while (running) {
			return (T) mParser.parse(params[0]);
		}
		return null;
	}

	@Override
	public void onPostExecute(T result) {
		mListener.onPostExecute(result);
	}
}
