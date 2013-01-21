package de.brvolleys.berlinrecyclingvolleys;


public interface DownloadListener<T> {
	
	public void onPreExecute();
	public void onPostExecute(T result);

}
