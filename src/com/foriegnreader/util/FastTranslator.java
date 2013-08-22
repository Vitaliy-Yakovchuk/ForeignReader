package com.foriegnreader.util;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ConcurrentNavigableMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import com.foriegnreader.TranslationHelper;

import android.os.Environment;

public class FastTranslator {

	private static DB db;

	private static ConcurrentNavigableMap<String, String> meanings;

	public FastTranslator() {
	}

	public String getMeaning(String word) {
		openIfNeed();
		String t = meanings.get(TranslationHelper.normilize(word
				.toLowerCase(Locale.getDefault())));
		if (t == null)
			return null;
		return TranslationHelper.normilize(t);
	}

	public void openIfNeed() {
		if (db == null) {
			File file = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES).getParentFile(),
					"Foreign Reader");
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
