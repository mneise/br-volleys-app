package de.brvolleys.berlinrecyclingvolleys;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class FullArticleDbAdapter {
	public static final String TABLE_NAME = "full_article";
	public static final String ROW_ID = "_id";
	public static final String TITLE = "title";
	public static final String TEASER = "teaser";
	public static final String IMGSRC = "imgsrc";
	public static final String IMG_DESCRIPTION = "img_description";
	public static final String TEXT = "text";
	public static final String ARTICLE_OVERVIEW_ID = "article_overview_id";

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
	public FullArticleDbAdapter(Context ctx) {
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
	public FullArticleDbAdapter open() throws SQLException {
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
	 * Create a new article. If the article is successfully created return the
	 * new rowId for that article, otherwise return a -1 to indicate failure.
	 * 
	 * @param title
	 * @param teaser
	 * @param img
	 * @param img
	 *            description
	 * @param text
	 * @param overview
	 *            article id
	 * @return rowId or -1 if failed
	 */
	public long createFullArticle(String title, String teaser, String imgsrc,
			String imgDescription, String text, Integer articleOverviewId) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(TITLE, title);
		initialValues.put(TEASER, teaser);
		initialValues.put(IMGSRC, imgsrc);
		initialValues.put(IMG_DESCRIPTION, imgDescription);
		initialValues.put(TEXT, text);
		initialValues.put(ARTICLE_OVERVIEW_ID, articleOverviewId);
		return this.mDb.insert(TABLE_NAME, null, initialValues);
	}

	/**
	 * Return a Cursor positioned at the car that matches the given rowId
	 * 
	 * @param rowId
	 * @return Cursor positioned to matching article, if found
	 * @throws SQLException
	 *             if article could not be found/retrieved
	 */
	public Cursor getFullArticle(long rowId) throws SQLException {

		Cursor mCursor =

		this.mDb.query(true, TABLE_NAME, new String[] { ROW_ID, TITLE, TEASER,
				IMGSRC, IMG_DESCRIPTION, TEXT }, ROW_ID + "=" + rowId, null,
				null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public FullArticle getFullArticleByArticleOverviewId(
			Integer articleOverviewId) {
		Cursor mCursor =

		this.mDb.query(true, TABLE_NAME, new String[] { ROW_ID, TITLE, TEASER,
				IMGSRC, IMG_DESCRIPTION, TEXT }, ARTICLE_OVERVIEW_ID + "="
				+ articleOverviewId, null, null, null, null, null);
		if (mCursor != null & mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			String title = mCursor.getString(mCursor
					.getColumnIndexOrThrow(TITLE));
			String teaser = mCursor.getString(mCursor
					.getColumnIndexOrThrow(TEASER));
			String img = mCursor.getString(mCursor
					.getColumnIndexOrThrow(IMGSRC));
			String imgdescription = mCursor.getString(mCursor
					.getColumnIndexOrThrow(IMG_DESCRIPTION));
			String text = mCursor
					.getString(mCursor.getColumnIndexOrThrow(TEXT));
			return new FullArticle(title, teaser, img, imgdescription, text);
		}
		return null;
	}
}
