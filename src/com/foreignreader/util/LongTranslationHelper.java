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
import java.util.List;

import android.os.Environment;

import com.reader.common.ObjectsFactory;
import com.reader.common.book.Section;
import com.reader.common.book.Sentence;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

public class LongTranslationHelper {

	private FastTranslator fastTranslator;

	private Dictionary dict;

	private static boolean supported = true;

	public LongTranslationHelper() {
		fastTranslator = new FastTranslator();
	}

	public void getTranslation(String text, Section builder) {
		text = TranslationHelper.normilize(text);
		openIfNeed();
		if (supported) {
			String s = fastTranslator.getNotNornalizedMeaning(text);
			if (s != null && s.length() > 0) {
				s.replace('\n', ' ');
				builder.getParagraphs().add(s);
				builder.setIgnoreParagraphCount(1);
			}
			add(dict.getIndexWord(text, POS.NOUN), "Noun\n", builder);
			add(dict.getIndexWord(text, POS.VERB), "Verb\n", builder);
			add(dict.getIndexWord(text, POS.ADJECTIVE), "Adjective\n", builder);
			add(dict.getIndexWord(text, POS.ADVERB), "Adverb\n", builder);
		}

		List<Sentence> sentences = ObjectsFactory.getDefaultDatabase()
				.getSentences(text);
		if (sentences != null) {
			for (Sentence sentence : sentences) {
				String text2 = sentence.text;
				while (text2.startsWith("\""))
					text2 = text2.substring(1);

				builder.getParagraphs().add(text2);
			}
		}
	}

	private void add(IIndexWord idxWord, String section, Section builder) {
		if (idxWord != null) {
			builder.getParagraphs().add(section);
			List<IWordID> wordIDs = idxWord.getWordIDs();
			int number = 1;
			for (IWordID wordID : wordIDs) {
				StringBuffer s = new StringBuffer();
				s.append(number + ". ");

				IWord word = dict.getWord(wordID);
				List<IWord> words = word.getSynset().getWords();
				boolean first = true;
				if (words != null && words.size() > 0) {
					for (IWord word2 : words) {
						if (first)
							first = false;
						else
							s.append("; ");
						s.append(word2.getLemma());
					}
				}
				builder.getParagraphs().add(s.toString());

				builder.getParagraphs().add(word.getSynset().getGloss());
				List<IWordID> related = word.getRelatedWords();
				boolean hasR = false;

				for (IWordID r : related)
					if (r.getLemma() != null) {
						hasR = true;
						break;
					}

				if (hasR) {
					s.setLength(0);
					s.append("Related: ");

					first = true;

					for (IWordID r : related)
						if (r.getLemma() != null) {
							if (first)
								first = false;
							else
								s.append("; ");
							s.append(r.getLemma());
						}
					builder.getParagraphs().add(s.toString());
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
