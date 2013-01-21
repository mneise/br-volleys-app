package de.brvolleys.berlinrecyclingvolleys;

import java.net.URL;

public interface HtmlParser<T> {
	
	public T parse(URL url);

}
