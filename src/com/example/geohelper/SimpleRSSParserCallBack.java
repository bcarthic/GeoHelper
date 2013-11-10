package com.example.geohelper;

import java.util.List;

public abstract class SimpleRSSParserCallBack {

	public abstract void onFeedParsed(List<RSSItem> items);

	public abstract void onError(Exception ex);
}