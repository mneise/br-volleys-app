package de.brvolleys.berlinrecyclingvolleys;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class BRVolleysXmlParser {
	// We don't use namespaces
    private static final String ns = null;
    
    public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException{
    	try {
    		XmlPullParser parser = Xml.newPullParser();
    		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
    		parser.setInput(in, null);
    		parser.nextTag();
    		return readFeed(parser);
    	} finally {
    		in.close();
    	}
    }
    
    private List<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
    	List<Entry> entries = 	new ArrayList<Entry>();
    	
    	parser.require(XmlPullParser.START_TAG, ns, "rss");
		parser.nextTag();
    	parser.require(XmlPullParser.START_TAG, ns, "channel");    	
    	while (parser.next() != XmlPullParser.END_TAG){
    		if (parser.getEventType() != XmlPullParser.START_TAG){
    			continue;
    		}
    		String name = parser.getName();
    		// Starts by looking for the entry tag
    		if (name.equals("item")){
    			entries.add(readEntry(parser));
    		} else {
    			skip(parser);
    		}
    	}
    	return entries;
    }
    
    public static class Entry {
        public final String title;
        public final String link;
        public final String description;
        public final String imgsrc;

        private Entry(String title, String link, String description, String imgsrc) {
            this.title = title;
            this.link = link;
            this.description = description;
            this.imgsrc = imgsrc;
        }
    }
    
    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
    	parser.require(XmlPullParser.START_TAG, ns, "item");
    	String title = null;
    	String link = null;
    	String description = null;
    	String imgsrc = null;
    	while (parser.next() != XmlPullParser.END_TAG){
    		if (parser.getEventType() != XmlPullParser.START_TAG){
    			continue;
    		}
    		String name = parser.getName();
    		if (name.equals("title")){
    			title = readTitle(parser);
    		}
    		else if (name.equals("link")){
    			link = readLink(parser);
    		}
    		else if (name.equals("description")){
    			Map<String, String> map = readDescriptionAndImgsrc(parser);
    			description = map.get("description");
    			imgsrc = map.get("imgsrc");
    		}
    		else{
    			skip(parser);
    		}
    	}
    	return new Entry(title, link, description, imgsrc);
    }
    
    // Processes title tags in the feed.
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
    	parser.require(XmlPullParser.START_TAG, ns, "title");
    	String title = readText(parser);
    	parser.require(XmlPullParser.END_TAG, ns, "title");
    	return title;
    }
    
    // Processes link tags in the feed.
    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
    	String link = "";
    	parser.require(XmlPullParser.START_TAG, ns, "link");
    	link = readText(parser);
    	parser.require(XmlPullParser.END_TAG, ns, "link");
    	return link;
    }
    
    // Processes description tags in the feed
    private Map<String, String> readDescriptionAndImgsrc(XmlPullParser parser) throws IOException, XmlPullParserException {
    	parser.require(XmlPullParser.START_TAG, ns, "description");
    	String description = readText(parser);
    	Document doc =  Jsoup.parse(description);
    	Elements imgs = doc.getElementsByTag("img");
    	String imgsrc = "";
    	for (Element img : imgs) {
    		imgsrc = img.attr("src");
    	}
    	Map<String, String> map = new HashMap<String, String>();
    	description = doc.text();
    	map.put("description", description);
    	map.put("imgsrc", imgsrc);
    	parser.require(XmlPullParser.END_TAG, ns, "description");
    	return map;
    }
    
    // For the tag title, link and description, extracts their text value
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
    	String result = "";
    	if (parser.next() == XmlPullParser.TEXT) {
    		result = parser.getText();
    		parser.nextTag();
    	}
    	return result;
    }
    
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
    	if (parser.getEventType() != XmlPullParser.START_TAG){
    		throw new IllegalStateException();
    	}
    	int depth = 1;
    	while (depth != 0){
    		switch (parser.next()) {
    		case XmlPullParser.END_TAG:
    			depth--;
    			break;
    		case XmlPullParser.START_TAG:
    			depth++;
    			break;
    		}
    	}
    }
}
