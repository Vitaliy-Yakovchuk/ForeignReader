package com.foriegnreader.pages;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Paint;

public class Section {

	private final String text;

	private List<Page> pages = new ArrayList<Page>();

	private int currentPage;

	public Section(String text) {
		this.text = text;
	}

	public void splitOnPages(Paint paint, int width, int maxLineCount) {
		pages.clear();
		char[] chr = text.toCharArray();
		int numChars = 0;
		do {
			Page page = new Page(chr, maxLineCount);
			pages.add(page);
			int lineCount = 0;
			while ((lineCount < maxLineCount) && (numChars < text.length())) {
				page.startLines[lineCount] = numChars;
				int count = paint.breakText(chr, numChars,
						((numChars + 500 > text.length()) ? text.length()
								- numChars : 500), width, null);
				numChars = numChars + count;
				page.lengthLines[lineCount] = count;
				lineCount++;
			}
		} while (numChars < text.length());
	}

	public int getPageCount() {
		return pages.size();
	}

	public Page getPage() {
		return pages.get(currentPage);
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

}
