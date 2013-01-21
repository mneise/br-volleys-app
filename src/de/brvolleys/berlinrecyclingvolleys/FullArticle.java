package de.brvolleys.berlinrecyclingvolleys;


public class FullArticle {
	public Integer id = null;
	public final String title;
	public final String teaser;
	public final String imgsrc;
	public final String imgdescription;
	public final String text;

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
}
