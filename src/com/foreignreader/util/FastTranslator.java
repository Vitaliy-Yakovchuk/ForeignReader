package com.foreignreader.util;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ConcurrentNavigableMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;


import android.os.Environment;

public class FastTranslator {

	private static DB db;

	private static boolean supported = true;

	private static ConcurrentNavigableMap<String, String> meanings;

	public FastTranslator() {
	}

	public String getMeaning(String word) {
		if (!supported)
			return null;
		openIfNeed();
		if (!supported)
			return null;
		String t = meanings.get(TranslationHelper.normilize(word
				.toLowerCase(Locale.getDefault())));
		if (t == null)
			return null;
		return TranslationHelper.normilizeA(t);
	}
	
	public String getNotNornalizedMeaning(String word) {
		if (!supported)
			return null;
		openIfNeed();
		if (!supported)
			return null;
		return meanings.get(TranslationHelper.normilize(word
				.toLowerCase(Locale.getDefault())));		
	}

	public void openIfNeed() {
		if (db == null) {
			File file = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES).getParentFile(),
					"Foreign Reader");
			if (!file.exists()) {
				supported = false;
				return;
			}
			file.mkdirs();
			db = DBMaker.newFileDB(new File(file, "dictionary.db")).make();
			meanings = db.getTreeMap("word_meanings");
		}
	}

	public void close() {
		if (db != null) {
			db.close();
			db = null;
		}
	}

}
