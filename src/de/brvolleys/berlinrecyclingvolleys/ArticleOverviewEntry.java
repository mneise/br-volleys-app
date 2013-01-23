package de.brvolleys.berlinrecyclingvolleys;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class ArticleOverviewEntry implements Comparable<ArticleOverviewEntry>,
		Parcelable {
	public Integer id = null;
	public String title;
	public String link;
	public Date date;

	/**
	 * @param title
	 * @param link
	 * @param date
	 */
	public ArticleOverviewEntry(String title, String link, Date date) {
		this.title = title;
		this.link = link;
		this.date = date;
	}

	/**
	 * @param id
	 * @param title
	 * @param link
	 * @param date
	 */
	public ArticleOverviewEntry(Integer id, String title, String link, Date date) {
		this.id = id;
		this.title = title;
		this.link = link;
		this.date = date;
	}

	private ArticleOverviewEntry(Parcel in) {
		this.id = in.readInt();
		this.title = in.readString();
		this.link = in.readString();
		this.date = DateConverter.getDate(in.readString());
	}

	@Override
	public int compareTo(ArticleOverviewEntry another) {
		return this.date.compareTo(another.date);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArticleOverviewEntry other = (ArticleOverviewEntry) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	public static final Parcelable.Creator<ArticleOverviewEntry> CREATOR = new Parcelable.Creator<ArticleOverviewEntry>() {
		public ArticleOverviewEntry createFromParcel(Parcel in) {
			return new ArticleOverviewEntry(in);
		}

		public ArticleOverviewEntry[] newArray(int size) {
			return new ArticleOverviewEntry[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(title);
		dest.writeString(link);
		dest.writeString(DateConverter.getString(date));
	}
}
