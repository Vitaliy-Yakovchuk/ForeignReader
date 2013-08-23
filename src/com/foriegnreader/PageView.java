package com.foriegnreader;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import com.foriegnreader.textimpl.TextWidthImpl;
import com.reader.common.AbstractTextProcessor;
import com.reader.common.ColorConstants;
import com.reader.common.ObjectsFactory;
import com.reader.common.TextSource;
import com.reader.common.TextWithProperties;
import com.reader.common.impl.SimpleTextParser;
import com.reader.common.pages.Page;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class PageView extends View {

	private TextPaint textPaint;
	private List<Word> words;
	private TextSource ts;

	final int blue = Color.parseColor("#CCCCFF");
	final int selected = Color.parseColor("#AAAAFF");
	final int red = Color.parseColor("#FF9999");
	final int yellow = Color.YELLOW;
	private int startSelection = -1;
	private int endSelection = -1;
	private boolean splitPages;

	private int gr0 = Color.parseColor("#0F0F0F");
	private int[] gr = new int[6];

	private Runnable loadPage;
	private int pageCount;
	private int currentPage;
	private TextWidthImpl textWidth;
	private int maxLineCount;
	private int lineHeight;
	private String title;

	public PageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		gr[0] = Color.parseColor("#A4A4A4");
		gr[1] = Color.parseColor("#AAAAAA");
		gr[2] = Color.parseColor("#BABABA");
		gr[3] = Color.parseColor("#CCCCCC");
		gr[4] = Color.parseColor("#DFDFDF");
		gr[5] = Color.parseColor("#F0F0F0");
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (words == null) {
			if (loadPage != null)
				loadPage.run();
			return;
		}

		if (splitPages) {
			int c = getWidth() / 2;
			textPaint.setColor(gr0);
			canvas.drawLine(c, 0, c, getHeight(), textPaint);
			for (int i = 0; i < 6; i++) {
				textPaint.setColor(gr[i]);
				canvas.drawLine(c + 1 + i, 0, c + 1 + i, getHeight(), textPaint);
				canvas.drawLine(c - 1 - i, 0, c - 1 - i, getHeight(), textPaint);
			}
		}

		for (int i = words.size() - 1; i >= 0; --i) {
			Word word = words.get(i);
			if (i >= startSelection && i <= endSelection) {
				textPaint.setColor(selected);
			} else
				textPaint.setColor(word.color);
			canvas.drawRect(word.rect, textPaint);
			textPaint.setColor(Color.BLACK);
			canvas.drawText(word.text, word.start, word.length2, word.x,
					word.y, textPaint);
		}

		if (splitPages) {
			int dy = (maxLineCount / 2 + 1) * lineHeight
					+ (int) (lineHeight * 0.3);
			String pc = (currentPage * 2 - 1) + "/" + (pageCount * 2);
			char[] text = pc.toCharArray();
			int w = textWidth.getWidth(text, 0, pc.length());
			textPaint.setColor(Color.GRAY);
			canvas.drawText(text, 0, pc.length(), getWidth() / 2 - w
					- (int) (getWidth() * 0.1), dy, textPaint);
			pc = (currentPage * 2) + "/" + (pageCount * 2);
			text = pc.toCharArray();
			w = textWidth.getWidth(text, 0, pc.length());
			textPaint.setColor(Color.GRAY);
			canvas.drawText(text, 0, pc.length(), (int) (getWidth() * 0.9) - w,
					dy, textPaint);
			canvas.drawText(title, (int) (getWidth() * 0.1), dy, textPaint);
		} else {
			int dy = (maxLineCount + 1) * lineHeight + (int) (lineHeight * 0.3);
			String pc = currentPage + "/" + pageCount;
			char[] text = pc.toCharArray();
			int w = textWidth.getWidth(text, 0, pc.length());
			textPaint.setColor(Color.GRAY);
			canvas.drawText(text, 0, pc.length(), (int) (getWidth() * 0.9) - w,
					dy, textPaint);
			canvas.drawText(title, (int) (getWidth() * 0.1), dy, textPaint);
		}
	}

	public void setText(final Page page, final TextWidthImpl textWidth,
			final int lineHeight, final int lineWidth,
			final boolean splitPages, int page2, int pageCount, String title) {
		this.title = title;
		this.textWidth = textWidth;
		this.currentPage = page2;
		this.pageCount = pageCount;
		this.splitPages = splitPages;
		this.maxLineCount = page.getMaxLineCount();
		this.lineHeight = lineHeight;
		clearSelection();
		this.textPaint = textWidth.getTextPaint();
		if (words == null)
			words = new ArrayList<PageView.Word>();
		else
			words.clear();
		final String text = page.getText();
		ts = ObjectsFactory.createSimpleSource(text);

		final int spaceWidth = textWidth.getWidth(new char[] { 't' }, 0, 1);

		AbstractTextProcessor abstractTextProcessor = new AbstractTextProcessor() {

			int num = 0;

			int line = 0;

			int cWidth;

			List<Word> lineWords = new ArrayList<PageView.Word>();

			@Override
			public void got(TextWithProperties textProperties) {
				while (text.charAt(num) != textProperties.getText().charAt(0))
					num++;

				if (page.lengthLines[line] == 0) {
					line++;
				}

				if (page.lengthLines[line] + page.startLines[line] < num
						+ page.startLines[0]) {
					boolean end = page.end[line];
					line++;
					fillLine(end);
				}

				Word word = new Word();
				word.start = num + page.startLines[0];

				int length = textProperties.getText().length();

				num += length;

				word.length1 = length;

				int z = num;

				while (z + 1 < text.length()
						&& !SimpleTextParser.isTextPart(text.charAt(z + 1))) {
					if (Character.isWhitespace(text.charAt(z)))
						break;
					z++;
					length++;
				}

				word.length2 = length;

				word.rect = new Rect();

				word.text = page.text;

				word.rect.right = textWidth.getWidth(word.text, word.start,
						word.length1);

				int st = word.start;
				boolean move = false;
				while (st > 0) {
					st--;
					if (Character.isSpaceChar(page.text[st])) {
						move = true;
						break;
					} else if (SimpleTextParser.isTextPart(page.text[st])) {
						break;
					}
				}
				st++;

				if (move) {
					int d = word.start - st;
					if (d > 0) {
						word.start = st;
						word.length2 += d;
						int a = textWidth.getWidth(page.text, st, d);
						word.rect.left = a;
						word.rect.right += a;
					}
				}

				word.rect.bottom = (int) (lineHeight * 0.2);
				word.rect.top = (int) (-lineHeight * 0.6);

				word.width2 = textWidth.getWidth(word.text, word.start,
						word.length2);

				cWidth += word.width2;
				lineWords.add(word);
				words.add(word);
				word.lcWord = textProperties.getText().toLowerCase(
						Locale.getDefault());

				word.color = toNativeColor(textProperties.getColor());
			}

			@Override
			public void end() {
				line++;
				fillLine(true);
			}

			private void fillLine(boolean end) {
				int dy = line * lineHeight + (int) (lineHeight * 0.3);

				boolean right = false;

				if (splitPages) {
					if (line > page.getMaxLineCount() / 2) {
						right = true;
						dy = (line - page.getMaxLineCount() / 2) * lineHeight
								+ (int) (lineHeight * 0.3);
					}
				}

				int sw;

				if (end)
					sw = spaceWidth;
				else {
					if (lineWords.size() > 1) {
						if (splitPages)
							sw = ((int) (lineWidth * 0.45 - cWidth))
									/ (lineWords.size() - 1);
						else
							sw = (int) (lineWidth * 0.96 - cWidth)
									/ (lineWords.size() - 1);
					} else
						sw = spaceWidth;
				}

				int dx = (int) (lineWidth * 0.02);

				if (right) {
					dx += lineWidth / 2;
				}

				for (Word word : lineWords) {
					word.x = dx;
					word.y = dy;
					word.rect.offset(dx, dy);

					dx += word.width2;
					dx += sw;

				}

				// hasParagraph = false;
				cWidth = 0;
				lineWords.clear();
			}

			@Override
			public void updated(TextWithProperties textProperties) {
				int i = 0;
				List<String> words2 = textProperties.getWords();
				while (i < words.size()) {
					if (words.get(i).lcWord.equals(words2.get(0))) {
						boolean found = true;
						int j = i + 1;
						int k = 1;
						while (j < words.size() && k < words2.size()) {
							if (!words.get(j).lcWord.equals(words2.get(k))) {
								found = false;
								break;
							}
							j++;
							k++;
						}
						if (found && k == words2.size()) {
							do {
								words.get(i).color = toNativeColor(textProperties
										.getColor());
								i++;
							} while (i < j);
							i++;
						}
					}
					i++;
				}

			}
		};

		ts.process(abstractTextProcessor);

	}

	public int toNativeColor(String c) {
		if (ColorConstants.WHITE.equals(c)) {
			return Color.WHITE;
		} else if (ColorConstants.BLUE.equals(c)) {
			return blue;
		} else if (ColorConstants.YELLOW.equals(c)) {
			return yellow;
		} else if (ColorConstants.RED.equals(c)) {
			return red;
		}
		return Color.WHITE;
	}

	public void markAsReaded() {
		HashSet<String> hs = new HashSet<String>();
		for (Word word : words)
			if (word.color == blue) {
				word.color = Color.WHITE;
				if (!hs.contains(word))
					hs.add(word.lcWord);
			}
		ts.markColor(hs.toArray(new String[hs.size()]), ColorConstants.WHITE);
	}

	private class Word {
		int start;

		int length1;

		int length2;

		int width2;

		char[] text;

		int x;

		int y;

		Rect rect;

		int color;

		String lcWord;
	}

	public void setColor(String text, String color) {
		ts.markColor(text, color);
	}

	public void clearSelection() {
		startSelection = -1;
		endSelection = -1;
	}

	public TextOnScreen select(int x1, int y1, int x2, int y2, int x, int y) {
		startSelection = -1;
		endSelection = -1;
		if(words==null)
			return null;
		for (int i = words.size() - 1; i >= 0; i--) {
			if (words.get(i).rect.contains(x1, y1))
				startSelection = i;
			if (words.get(i).rect.contains(x2, y2))
				endSelection = i;
		}

		if (startSelection < 0 || endSelection < 0)
			return null;

		if (endSelection < startSelection) {
			int t = endSelection;
			endSelection = startSelection;
			startSelection = t;
		}

		StringBuffer sb = null;

		for (int i = startSelection; i <= endSelection; i++) {
			Word word = words.get(i);
			if (sb == null)
				sb = new StringBuffer();
			else
				sb.append(' ');
			sb.append(new String(word.text, word.start, word.length2));
		}

		TextOnScreen onScreen = new TextOnScreen();

		onScreen.text = sb.toString();
		onScreen.x = x;
		onScreen.y = y;

		return onScreen;
	}

	public Runnable getLoadPage() {
		return loadPage;
	}

	public void setLoadPage(Runnable loadPage) {
		this.loadPage = loadPage;
	}

	public void setWords(List<Word> words) {
		this.words = words;
	}

}
