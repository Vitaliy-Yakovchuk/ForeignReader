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
