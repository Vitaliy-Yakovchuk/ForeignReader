package com.foreignreader.words;

import java.util.Arrays;
import java.util.List;

import com.reader.common.ColorConstants;
import com.reader.common.ObjectsFactory;
import com.reader.common.Word;

public class WordsContent {

	private static Word[] items = null;

	public static void loadItems() {
		List<Word> words = ObjectsFactory.getDefaultDatabase().loadWords(
				ColorConstants.YELLOW);

		items = words.toArray(new Word[words.size()]);

		Arrays.sort(items);
	}

	public static Word[] getItems() {
		return items;
	}

	public static Word get(String word) {
		return ObjectsFactory.getDefaultDatabase().toWord(word);
	}

}
