package com.foriegnreader.pages;

public class Page {

	public Page(char[] text, int maxLineCount) {
		this.text = text;
		startLines = new int[maxLineCount];
		lengthLines = new int[maxLineCount];
	}

	public final char[] text;

	public final int[] startLines;

	public final int[] lengthLines;

	public String getText() {
		int s = startLines[0];
		int l = 0;
		for (int i = 0; i < lengthLines.length; i++) {
			l += lengthLines[i];
		}
		return new String(text, s, l);
	}

}
