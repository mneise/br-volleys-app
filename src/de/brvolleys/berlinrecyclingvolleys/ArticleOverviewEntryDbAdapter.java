package de.brvolleys.berlinrecyclingvolleys;

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

	/**
	 * Open the database. If it cannot be opened, try to create a new instance
	 * of the database. If it cannot be created, throw an exception to signal
	 * the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public ArticleOverviewEntryDbAdapter open() throws SQLException {
		this.mDbHelper = new DatabaseHelper(this.mCtx);
		this.mDb = this.mDbHelper.getWritableDatabase();
		return this;
	}

	/**
	 * close return type: void
	 */
	public void close() {
		this.mDbHelper.close();
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
	public long createArticleOverviewEntry(String title, String date,
			String link) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(TITLE, title);
		initialValues.put(DATE, date);
		initialValues.put(LINK, link);
		return this.mDb.insert(TABLE_NAME, null, initialValues);
	}

	/**
	 * Return a Cursor over the list of all article overview entries in the
	 * database
	 * 
	 * @return Cursor over all article overview entries
	 */
	public Cursor getAllArticleOverviewEntries() {

		return this.mDb.query(TABLE_NAME, new String[] { ROW_ID, TITLE, DATE,
				LINK }, null, null, null, null, null);
	}

	/**
	 * Delete the entry
	 * 
	 * @param entry
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteArticleOverviewEntry(ArticleOverviewEntry entry) {

		this.mDb.delete(FullArticleDbAdapter.TABLE_NAME,
				FullArticleDbAdapter.ROW_ID + "=" + entry.id, null);
		return this.mDb.delete(TABLE_NAME, ROW_ID + "=" + entry.id, null) > 0; //$NON-NLS-1$
	}
}
