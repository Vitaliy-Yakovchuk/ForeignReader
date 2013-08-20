package com.foriegnreader.pages;

public class Page {

	public Page(char[] text, int maxLineCount) {
		this.text = text;
		startLines = new int[maxLineCount];
		lengthLines = new int[maxLineCount];
		end = new boolean[maxLineCount];
	}

	public final char[] text;

	public final int[] startLines;

	public final int[] lengthLines;

	public final boolean[] end;

	public String getText() {
		int s = startLines[0];
		int l = 0;
		for (int i = 0; i < lengthLines.length; i++) {
			int j = startLines[i] + lengthLines[i] - s;
			if (l < j)
				l = j;
		}
		return new String(text, s, l);
	}

}
