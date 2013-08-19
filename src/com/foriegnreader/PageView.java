package com.foriegnreader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import com.foriegnreader.pages.Page;
import com.reader.common.AbstractTextProcessor;
import com.reader.common.ColorConstants;
import com.reader.common.ObjectsFactory;
import com.reader.common.TextSource;
import com.reader.common.TextWithProperties;
import com.reader.common.impl.SimpleTextParser;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class PageView extends View {

	private TextPaint textPaint;
	private List<Word> words = new ArrayList<PageView.Word>();
	private TextSource ts;

	final int blue = Color.parseColor("#AAAAFF");
	final int red = Color.parseColor("#FF9999");

	public PageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		for (Word word : words) {
			textPaint.setColor(word.color);
			canvas.drawRect(word.rect, textPaint);
			textPaint.setColor(Color.BLACK);
			canvas.drawText(word.text, word.start, word.length2, word.x,
					word.y, textPaint);
		}
	}

	public void setText(final Page page, final TextPaint textPaint,
			final int lineHeight, final int lineWidth) {
		this.textPaint = textPaint;
		words.clear();
		final String text = page.getText();
		ts = ObjectsFactory.createSimpleSource(text);

		Rect bounds = new Rect();

		textPaint.getTextBounds(" ", 0, 1, bounds);

		// final int spaceWidth = bounds.right;

		AbstractTextProcessor abstractTextProcessor = new AbstractTextProcessor() {

			int num = 0;

			int line = 0;

			int cWidth;

			List<Word> lineWords = new ArrayList<PageView.Word>();

			@Override
			public void got(TextWithProperties textProperties) {
				while (text.charAt(num) != textProperties.getText().charAt(0))
					num++;

				if (page.lengthLines[line] + page.startLines[line] < num
						+ page.startLines[0]) {
					line++;
					fillLine();
				}

				Word word = new Word();
				word.start = num + page.startLines[0];

				int length = textProperties.getText().length();

				num += length;

				word.length1 = length;

				int z = num;

				while (z + 1 < text.length()
						&& !SimpleTextParser.isTextPart(text.charAt(z + 1))) {
					z++;
					length++;
					// if (text.charAt(z + 1) == '\n')
					// hasParagraph = true;
				}

				word.length2 = length;

				word.rect = new Rect();

				word.text = page.text;

				textPaint.getTextBounds(word.text, word.start, word.length1,
						word.rect);
				textPaint.getTextBounds(word.text, word.start, word.length2,
						rect);

				word.width2 = rect.right;

				cWidth += rect.right;
				lineWords.add(word);
				words.add(word);
				word.lcWord = textProperties.getText().toLowerCase(
						Locale.getDefault());

				if (ColorConstants.WHITE.equals(textProperties.getColor())) {
					word.color = Color.WHITE;
				} else if (ColorConstants.BLUE
						.equals(textProperties.getColor())) {
					word.color = blue;
				} else if (ColorConstants.YELLOW.equals(textProperties
						.getColor())) {
					word.color = Color.YELLOW;
				} else if (ColorConstants.RED.equals(textProperties.getColor())) {
					word.color = red;
				}
			}

			@Override
			public void end() {
				line++;
				fillLine();
			}

			Rect rect = new Rect();

			private void fillLine() {
				int dy = line * lineHeight;
				// if (cWidth * 2 < lineWidth)
				// hasParagraph = true;

				int sw; // = spaceWidth;

				// if (!hasParagraph) {
				sw = (lineWidth - cWidth) / (lineWords.size() - 1);
				// }

				int dx = 0;

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
		};

		ts.process(abstractTextProcessor);

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

}
