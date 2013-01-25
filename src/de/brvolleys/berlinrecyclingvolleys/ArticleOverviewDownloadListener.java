package de.brvolleys.berlinrecyclingvolleys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.brvolleys.berlinrecyclingvolleys.ArticleOverviewActivity.State;

public class ArticleOverviewDownloadListener implements
		DownloadListener<List<ArticleOverviewEntry>> {
	private ArticleOverviewEntryDbAdapter mDbHelper = null;
	private ArticleOverviewActivity mActivity = null;

	public ArticleOverviewDownloadListener(
			ArticleOverviewEntryDbAdapter dbHelper,
			ArticleOverviewActivity activity) {
		mDbHelper = dbHelper;
		mActivity = activity;
	}

	@Override
	public void onPreExecute() {
		mActivity.state = State.LOADING;
		mActivity.disableButton();
		mActivity.enableProgressView();
	}

	@Override
	public void onPostExecute(List<ArticleOverviewEntry> result) {
		Collections.sort(result);
		Date dateAfter = result.get(0).date;
		Date dateBefore = result.get(result.size() - 1).date;

		List<ArticleOverviewEntry> savedEntries = mDbHelper
				.getAllEntriesBetweenTwoDates(dateAfter, dateBefore);
		List<ArticleOverviewEntry> entries = saveNewEntries(result,
				savedEntries);
		entries.addAll(removeOutdatedEntries(savedEntries, result));
		mActivity.addNewEntries(entries);
		mActivity.disableProgressView();
		mActivity.displayEntries(entries);
		if (mActivity.isAllowedToEnableButton()) {
			mActivity.enableButton();
		}
		mActivity.state = State.WAITING;
	}

	public List<ArticleOverviewEntry> removeOutdatedEntries(
			List<ArticleOverviewEntry> savedEntries,
			List<ArticleOverviewEntry> fetchedEntries) {
		List<ArticleOverviewEntry> updatedEntries = new ArrayList<ArticleOverviewEntry>();
		Collections.sort(fetchedEntries);
		Date oldestDate = fetchedEntries.get(0).date;
		for (ArticleOverviewEntry entry : savedEntries) {
			if (!fetchedEntries.contains(entry) & entry.date.after(oldestDate)) {
				mDbHelper.deleteArticleOverviewEntry(entry);
			} else {
				updatedEntries.add(entry);
			}
		}
		return updatedEntries;
	}

	public List<ArticleOverviewEntry> saveNewEntries(
			List<ArticleOverviewEntry> fetchedEntries,
			List<ArticleOverviewEntry> savedEntries) {
		List<ArticleOverviewEntry> newEntries = new ArrayList<ArticleOverviewEntry>();
		for (ArticleOverviewEntry entry : fetchedEntries) {
			if (!savedEntries.contains(entry)) {
				entry.id = (int) mDbHelper.createArticleOverviewEntry(entry);
				newEntries.add(entry);
			}
		}
		return newEntries;
	}
}
