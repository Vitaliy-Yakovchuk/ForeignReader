/*******************************************************************************
 * Copyright 2013 Vitaliy Yakovchuk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.foreignreader.util;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ConcurrentNavigableMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import com.reader.common.ObjectsFactory;
import com.reader.common.Word;

import android.os.Environment;

public class FastTranslator {

	private static DB db;

	private static boolean supported = true;

	private static ConcurrentNavigableMap<String, String> meanings;

	public FastTranslator() {
	}

	public String getMeaning(String word) {
		word = TranslationHelper
				.normilize(word.toLowerCase(Locale.getDefault()));
		String res = getMeaningA(word);
		Word word2 = ObjectsFactory.getDefaultDatabase().getWordInfo(word);
		String nw = word;
		if (word2 != null)
			nw = word2.getText() + " (" + word2.getInSentenceCount() + ")";
		if (res == null)
			return nw;
		return res.replaceAll(word, nw);
	}

	public String getMeaningA(String word) {
		if (!supported)
			return null;
		openIfNeed();
		if (!supported)
			return null;
		String t = meanings.get(word);
		if (t == null)
			return null;
		return TranslationHelper.normilizeA(t);
	}

	public String getNotNornalizedMeaning(String word) {
		word = TranslationHelper
				.normilize(word.toLowerCase(Locale.getDefault()));
		String res = getNotNornalizedMeaningA(word);
		Word word2 = ObjectsFactory.getDefaultDatabase().getWordInfo(word);
		String nw = word;
		if (word2 != null)
			nw = word2.getText() + " (" + word2.getInSentenceCount() + ")";
		if (res == null)
			return nw;
		return res.replaceAll(word, nw);
	}

	public String getNotNornalizedMeaningA(String word) {
		if (!supported)
			return null;
		openIfNeed();
		if (!supported)
			return null;
		return meanings.get(word);
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
