package de.brvolleys.berlinrecyclingvolleys;

public class FullArticle {
	public Integer id = null;
	public String title = "";
	public String teaser = "";
	public String imgsrc = "";
	public String imgdescription = "";
	public String text = "";
	public Integer articleOverviewId = -1;

	public FullArticle(String title, String teaser, String imgsrc,
			String imgdescription, String text) {
		this.title = title;
		this.teaser = teaser;
		this.imgsrc = imgsrc;
		this.imgdescription = imgdescription;
		this.text = text;
	}

	public FullArticle(Integer id, String title, String teaser, String imgsrc,
			String imgdescription, String text) {
		this.id = id;
		this.title = title;
		this.teaser = teaser;
		this.imgsrc = imgsrc;
		this.imgdescription = imgdescription;
		this.text = text;
	}

	public FullArticle(Integer id, String title, String teaser, String imgsrc,
			String imgdescription, String text, Integer articleOverviewId) {
		this.id = id;
		this.title = title;
		this.teaser = teaser;
		this.imgsrc = imgsrc;
		this.imgdescription = imgdescription;
		this.text = text;
		this.articleOverviewId = articleOverviewId;
	}

}
