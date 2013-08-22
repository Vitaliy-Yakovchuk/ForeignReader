package com.foriegnreader.cache;

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import android.app.Activity;

import com.foriegnreader.BooksActivity;
import com.reader.common.BookMetadata;
import com.reader.common.impl.SectionImpl;
import com.reader.common.pages.Page;
import com.reader.mapdb.SectionKey;

public class SectionCacheHelper {

	private DB db;

	private ConcurrentNavigableMap<SectionKey, SectionImpl> data;

	private Activity activity;

	public SectionCacheHelper(Activity activity) {
		this.activity = activity;
		open();
	}

	private void open() {
		File file = new File(activity.getCacheDir(), "books-data.db");
		db = DBMaker.newFileDB(file).make();
		data = db.getTreeMap("cache1");
	}

	public SectionImpl getFromCache(BookMetadata metadata, int section,
			boolean landscape, boolean splited, String fontName, int fontSize,
			int width, int height) {
		if (db.isClosed())
			open();
		SectionKey key = new SectionKey(metadata.getFileName(), section,
				landscape, splited, fontName, fontSize, width, height);
		if (BooksActivity.TESTING_STORGE)
			return null;
		try {
			SectionImpl sData = data.get(key);
			if (sData == null)
				return null;
			for (Page page : sData.getPages())
				page.text = sData.getT();
			return sData;
		} catch (Exception e) {
			return null;
		}
	}

	public void setToCache(BookMetadata metadata, int section,
			boolean landscape, boolean splited, String fontName, int fontSize,
			SectionImpl s, int width, int height) {
		if (BooksActivity.TESTING_STORGE)
			return;
		if (db.isClosed())
			open();
		SectionKey key = new SectionKey(metadata.getFileName(), section,
				landscape, splited, fontName, fontSize, width, height);
		data.put(key, s);
	}

	public void removeBook(BookMetadata bookMetadata) {
		Iterator<SectionKey> i = data.keySet().iterator();
		List<SectionKey> l = new ArrayList<SectionKey>();
		while (i.hasNext()) {
			SectionKey key = i.next();
			if (key.getFileName().equals(bookMetadata.getFileName()))
				l.add(key);
		}
		for (SectionKey key : l)
			data.remove(key);
	}

	public void close() {
		if (db.isClosed())
			return;
		db.commit();
		db.close();
	}

}
