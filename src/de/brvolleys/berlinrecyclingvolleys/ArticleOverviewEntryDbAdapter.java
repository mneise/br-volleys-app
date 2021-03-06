package de.brvolleys.berlinrecyclingvolleys;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ArticleOverviewEntryDbAdapter {
	public static final String TABLE_NAME = "article_overview_entry";
	public static final String ROW_ID = "_id";
	public static final String TITLE = "title";
	public static final String DATE = "date";
	public static final String LINK = "link";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private final Context mCtx;

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public ArticleOverviewEntryDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public void open() throws SQLException {
		this.mDbHelper = DatabaseHelper.getInstance(mCtx);
		this.mDb = this.mDbHelper.getWritableDatabase();
	}

	/**
	 * close return type: void
	 */
	public void close() {
		mDbHelper.close();
	}

	/**
	 * Create a new article overview entry. If the entry is successfully created
	 * return the new rowId for that entry, otherwise return a -1 to indicate
	 * failure.
	 * 
	 * @param title
	 * @param date
	 * @param link
	 * @return rowId or -1 if failed
	 */
	public long createArticleOverviewEntry(ArticleOverviewEntry entry) {
		open();
		ContentValues initialValues = new ContentValues();
		initialValues.put(TITLE, entry.title);
		initialValues.put(DATE, DateConverter.getString(entry.date));
		initialValues.put(LINK, entry.link);
		long id = mDb.insert(TABLE_NAME, null, initialValues);
		close();
		return id;
	}

	/**
	 * Return a Cursor over the list of all article overview entries in the
	 * database
	 * 
	 * @return Cursor over all article overview entries
	 */
	public List<ArticleOverviewEntry> getAllEntries() {

		open();
		Cursor cursor = mDb.query(TABLE_NAME, new String[] { ROW_ID, TITLE,
				DATE, LINK }, null, null, null, null, null);

		List<ArticleOverviewEntry> entries = new ArrayList<ArticleOverviewEntry>();

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Integer id = cursor
					.getInt(cursor
							.getColumnIndexOrThrow(ArticleOverviewEntryDbAdapter.ROW_ID));
			String title = cursor
					.getString(cursor
							.getColumnIndexOrThrow(ArticleOverviewEntryDbAdapter.TITLE));
			String date = cursor.getString(cursor
					.getColumnIndexOrThrow(ArticleOverviewEntryDbAdapter.DATE));
			String link = cursor.getString(cursor
					.getColumnIndexOrThrow(ArticleOverviewEntryDbAdapter.LINK));
			entries.add(new ArticleOverviewEntry(id, title, link, DateConverter
					.getDate(date)));
			cursor.moveToNext();
		}
		close();
		return entries;
	}

	public List<ArticleOverviewEntry> getAllEntriesAfter(Date date) {
		List<ArticleOverviewEntry> allEntries = getAllEntries();
		List<ArticleOverviewEntry> entries = new ArrayList<ArticleOverviewEntry>();
		for (ArticleOverviewEntry entry : allEntries) {
			if (entry.date.after(date) || entry.date.equals(date)) {
				entries.add(entry);
			}
		}
		return entries;
	}

	public List<ArticleOverviewEntry> getAllEntriesBetweenTwoDates(
			Date dateAfter, Date dateBefore) {
		List<ArticleOverviewEntry> allEntries = getAllEntries();
		List<ArticleOverviewEntry> entries = new ArrayList<ArticleOverviewEntry>();
		for (ArticleOverviewEntry entry : allEntries) {
			if ((entry.date.after(dateAfter) || entry.date.equals(dateAfter))
					&& (entry.date.before(dateBefore) || entry.date
							.equals(dateBefore))) {
				entries.add(entry);
			}
		}
		return entries;
	}

	/**
	 * Delete the entry
	 * 
	 * @param entry
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteArticleOverviewEntry(ArticleOverviewEntry entry) {

		open();
		mDb.delete(FullArticleDbAdapter.TABLE_NAME,
				FullArticleDbAdapter.ARTICLE_OVERVIEW_ID + "=" + entry.id, null);
		boolean success = mDb.delete(TABLE_NAME, ROW_ID + "=" + entry.id, null) > 0;
		close();
		return success;
	}
}
