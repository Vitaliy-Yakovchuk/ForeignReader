package com.foreignreader.cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;

import com.reader.common.BookMetadata;
import com.reader.common.impl.SectionImpl;
import com.reader.mapdb.SectionKey;

public class SectionCacheHelper {

	private List<Data> cache = new ArrayList<Data>();

	private static final int MAX_CACHE_SIZE = 4;

	public SectionCacheHelper(Activity activity) {

	}

	public SectionImpl getFromCache(BookMetadata metadata, int section,
			boolean landscape, boolean splited, String fontName, int fontSize,
			int width, int height) {
		SectionKey key = new SectionKey(metadata.getFileName(), section,
				landscape, splited, fontName, fontSize, width, height);
		for (Data data : cache) {
			if (data.key.equals(key))
				return data.section;
		}
		return null;
	}

	public void setToCache(BookMetadata metadata, int section,
			boolean landscape, boolean splited, String fontName, int fontSize,
			SectionImpl s, int width, int height) {
		SectionKey key = new SectionKey(metadata.getFileName(), section,
				landscape, splited, fontName, fontSize, width, height);
		if (cache.size() == MAX_CACHE_SIZE) {
			int r = 0;
			Date min = cache.get(0).date;
			for (int i = 1; i < cache.size(); i++) {
				if (min.after(cache.get(i).date)) {
					r = i;
					min = cache.get(i).date;
				}
			}
			cache.remove(r);
		}
		cache.add(new Data(key, s));
	}

	public void close() {

	}

	private class Data {
		public Data(SectionKey key2, SectionImpl s) {
			this.key = key2;
			this.section = s;
			this.date = new Date();
		}

		SectionKey key;

		SectionImpl section;

		Date date;
	}

}
