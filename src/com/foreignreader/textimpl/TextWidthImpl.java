package com.foreignreader.textimpl;

import java.util.HashMap;

import com.reader.common.TextWidth;

import android.graphics.Rect;
import android.text.TextPaint;

public class TextWidthImpl implements TextWidth {

	private final TextPaint textPaint;

	private HashMap<Key, Integer> buffer = new HashMap<Key, Integer>();

	private Rect rect = new Rect();

	public TextWidthImpl(TextPaint textPaint) {
		this.textPaint = textPaint;
	}

	@Override
	public int getWidth(char[] text, int start, int length) {
		Key key = new Key(text, start, length);
		Integer i = buffer.get(key);
		if (i != null)
			return i;
		textPaint.getTextBounds(text, start, length, rect);
		i = rect.right;
		buffer.put(key, i);
		return i;
	}

	public TextPaint getTextPaint() {
		return textPaint;
	}
	
	private class Key {
		final char[] txt;

		final int start;

		final int length;

		final int hash;

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			Key other = (Key) obj;
			if (length != other.length)
				return false;
			if (start != other.start)
				return false;
			for (int i = 0; i < length; i++)
				if (txt[i + start] != other.txt[i + start])
					return false;
			return true;
		}

		public Key(char[] txt, int start, int length) {
			this.txt = txt;
			this.start = start;
			this.length = length;

			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + length;
			result = prime * result + start;
			for (int i = 0; i < length; i++)
				result = 31 * result + txt[start + i];
			this.hash = result;
		}

		private TextWidthImpl getOuterType() {
			return TextWidthImpl.this;
		}
	}

}
