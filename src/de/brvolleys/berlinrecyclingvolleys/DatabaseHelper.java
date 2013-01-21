package de.brvolleys.berlinrecyclingvolleys;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	// If you change the database schema, you must increment the database
	// version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "BRVolleys.db";

	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";

	private static final String SQL_CREATE_ARTICLE_OVERVIEW_ENTRIES = "CREATE TABLE "
			+ ArticleOverviewEntryDbAdapter.TABLE_NAME
			+ " ("
			+ ArticleOverviewEntryDbAdapter.ROW_ID
			+ " INTEGER PRIMARY KEY,"
			+ ArticleOverviewEntryDbAdapter.TITLE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ArticleOverviewEntryDbAdapter.DATE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ArticleOverviewEntryDbAdapter.LINK
			+ TEXT_TYPE
			+ COMMA_SEP
			+ "UNIQUE("
			+ ArticleOverviewEntryDbAdapter.TITLE
			+ COMMA_SEP
			+ ArticleOverviewEntryDbAdapter.DATE
			+ COMMA_SEP
			+ ArticleOverviewEntryDbAdapter.LINK + "))";

	private static final String SQL_DELETE_ARTICLE_OVERVIEW_ENTRIES = "DROP TABLE IF EXISTS "
			+ ArticleOverviewEntryDbAdapter.TABLE_NAME;

	private static final String SQL_CREATE_FULL_ARTICLES = "CREATE TABLE "
			+ FullArticleDbAdapter.TABLE_NAME + " ("
			+ FullArticleDbAdapter.ROW_ID + " INTEGER PRIMARY KEY,"
			+ FullArticleDbAdapter.TITLE + TEXT_TYPE + COMMA_SEP
			+ FullArticleDbAdapter.TEASER + TEXT_TYPE + COMMA_SEP
			+ FullArticleDbAdapter.IMGSRC + TEXT_TYPE + COMMA_SEP
			+ FullArticleDbAdapter.IMG_DESCRIPTION + TEXT_TYPE + COMMA_SEP
			+ FullArticleDbAdapter.TEXT + TEXT_TYPE + COMMA_SEP
			+ FullArticleDbAdapter.ARTICLE_OVERVIEW_ID + INTEGER_TYPE
			+ COMMA_SEP + "FOREIGN KEY ("
			+ FullArticleDbAdapter.ARTICLE_OVERVIEW_ID + ") REFERENCES "
			+ FullArticleDbAdapter.TABLE_NAME + " ("
			+ FullArticleDbAdapter.ROW_ID + "))";

	private static final String SQL_DELETE_FULL_ARTICLES = "DROP TABLE IF EXISTS "
			+ FullArticleDbAdapter.TABLE_NAME;

	DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ARTICLE_OVERVIEW_ENTRIES);
		db.execSQL(SQL_CREATE_FULL_ARTICLES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DatabaseHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL(SQL_DELETE_ARTICLE_OVERVIEW_ENTRIES);
		db.execSQL(SQL_DELETE_FULL_ARTICLES);
		onCreate(db);
	}
}
