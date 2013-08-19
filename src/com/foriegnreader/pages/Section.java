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
			Page page = new Page();
			pages.add(page);
			page.start = numChars;
			int lineCount = 0;
			while ((lineCount < maxLineCount) && (numChars < text.length())) {
				numChars = numChars
						+ paint.breakText(chr, numChars, ((numChars + 500 > text
								.length()) ? text.length()-numChars : 500),
								width, null);
				lineCount++;
			}
			page.length = numChars - page.start;
		} while (numChars < text.length());
	}

	public int getPageCount() {
		return pages.size();
	}

	public String getPage() {
		return text.substring(pages.get(currentPage).start,
				pages.get(currentPage).start + pages.get(currentPage).length);
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

}
