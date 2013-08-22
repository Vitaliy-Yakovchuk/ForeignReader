package com.foriegnreader.util;

import java.io.File;
import java.util.List;

import com.foriegnreader.TranslationHelper;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

import android.os.Environment;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

public class LongTranslationHelper {

	private FastTranslator fastTranslator;

	private Dictionary dict;

	private static boolean supported = true;

	public LongTranslationHelper() {
		fastTranslator = new FastTranslator();
	}

	public void getTranslation(String text, SpannableStringBuilder builder) {
		if (!supported)
			return;
		openIfNeed();
		if (!supported)
			return;

		text = TranslationHelper.normilize(text);

		String s = fastTranslator.getNotNornalizedMeaning(text);
		if (s != null && s.length() > 0) {
			builder.append(s);
			builder.append('\n');
		}
		add(dict.getIndexWord(text, POS.NOUN), "Noun\n", builder);
		add(dict.getIndexWord(text, POS.VERB), "Verb\n", builder);
		add(dict.getIndexWord(text, POS.ADJECTIVE), "Adjective\n", builder);
		add(dict.getIndexWord(text, POS.ADVERB), "Adverb\n", builder);
	}

	private void add(IIndexWord idxWord, String section,
			SpannableStringBuilder builder) {
		if (idxWord != null) {
			int s = builder.length();
			builder.append(section);
			builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), s,
					builder.length(), 0);
			builder.append('\n');
			List<IWordID> wordIDs = idxWord.getWordIDs();
			int number = 1;
			for (IWordID wordID : wordIDs) {
				s = builder.length();
				builder.append(number + ". ");

				IWord word = dict.getWord(wordID);
				List<IWord> words = word.getSynset().getWords();
				boolean first = true;
				if (words != null && words.size() > 0) {
					for (IWord word2 : words) {
						if (first)
							first = false;
						else
							builder.append("; ");
						builder.append(word2.getLemma());
					}
				}
				builder.append('\n');

				builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
						s, builder.length(), 0);
				builder.append(word.getSynset().getGloss());
				builder.append('\n');
				List<IWordID> related = word.getRelatedWords();
				if (related.size() > 0) {
					s = builder.length();
					builder.append("Related: ");

					first = true;

					for (IWordID r : related) {
						if (first)
							first = false;
						else
							builder.append("; ");
						builder.append(r.getLemma());
					}
				}
				number++;
			}
		}

	}

	public void openIfNeed() {
		if (dict == null) {
			File file = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES).getParentFile(),
					"Foreign Reader");
			file = new File(file, "WordNet");
			file = new File(file, "dict");
			if (!file.exists()) {
				supported = false;
				return;
			}
			try {
				dict = new Dictionary(file);
				dict.open();
			} catch (Exception exception) {
				supported = false;
				return;
			}
		}
	}

	public void close() {
		fastTranslator.close();
		if (dict != null) {
			dict.close();
			dict = null;
		}
	}

}
