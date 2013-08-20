package com.foriegnreader.pages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.foriegnreader.textprocessor.TextWidth;
import com.reader.common.impl.SimpleTextWithSymbolsParser;

public class Section {

	private final char[] t;

	private List<Page> pages = new ArrayList<Page>();

	private int currentPage;

	private com.reader.common.book.Section section;

	public Section(com.reader.common.book.Section section) {
		this.section = section;
		StringBuilder builder = new StringBuilder();
		for (String s : section.getParagraphs()) {
			builder.append(s);
			builder.append(' ');
		}
		t = new char[builder.length()];
		builder.getChars(0, t.length, t, 0);
	}

	public void splitOnPages(final TextWidth textWidth, final int width,
			final int maxLineCount) {
		pages.clear();

		final int spaceWidth = textWidth.getWidth(new char[] { 't' }, 0, 1);

		pages.add(new Page(t, maxLineCount));

		final Iterator<String> paragraphs = section.getParagraphs().iterator();

		class SimpleTextWithSymbolsParserA extends SimpleTextWithSymbolsParser {
			private int line;

			private int lineWidth;

			private Page page;

			{
				page = pages.get(0);
			}

			private int st;

			private int len;

			private String p;

			private int pIndex;

			@Override
			public void processWord(char[] txt, int start, int length) {
				int w = textWidth.getWidth(txt, start, length);
				if (lineWidth > 0)
					lineWidth += spaceWidth;

				lineWidth += w;
				if (paragraphChanged(txt, start, length)) {
					add(true);
					lineWidth = w;
					st = start;
					len = 0;
					line++;
					if (line + 1 < maxLineCount)
						line++;
				} else if (lineWidth > width) {
					add(false);
					lineWidth = w;
					st = start;
					len = 0;
					line++;
				}
				len = start - st + length;
			}

			private boolean paragraphChanged(char[] txt, int start, int length) {
				if (p == null)
					p = paragraphs.next();

				boolean res = false;

				while (true) {
					if (pIndex >= p.length()) {
						res = true;
						p = paragraphs.next();
						pIndex = 0;
						while (p.charAt(pIndex) != txt[start]) {
							pIndex++;
						}
						break;
					}
					if (p.charAt(pIndex) == txt[start])
						break;
					pIndex++;
				}
				pIndex += length;
				return res;
			}

			private void add(boolean end) {
				if (line >= maxLineCount) {
					page = new Page(t, maxLineCount);
					pages.add(page);
					line = 0;
				}

				page.startLines[line] = st;
				page.lengthLines[line] = len;
				page.end[line] = end;

			}
		}

		SimpleTextWithSymbolsParserA parser = new SimpleTextWithSymbolsParserA();

		parser.parse(t);

		parser.add(true);
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

	public void setSection(com.reader.common.book.Section section) {
		this.section = section;
	}
}
