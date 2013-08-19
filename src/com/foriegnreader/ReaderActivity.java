package com.foriegnreader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.foriegnreader.R;
import com.foriegnreader.pages.Section;

public class ReaderActivity extends Activity {

	private static final int FONT_SIZE = 26;

	public static final String FILE = "FileName";

	private Section section;

	private PageView contentView;

	private Button prev;

	private Button next;

	private TextView pageNumber;

	private TextPaint paint;

	private int lineHeight;

	private int lineWidth;

	private Button mark;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_reader);

		View findViewById = findViewById(R.id.fullscreen_content);
		contentView = (PageView) findViewById;

		prev = (Button) findViewById(R.id.prevPage);
		next = (Button) findViewById(R.id.nextPage);
		mark = (Button) findViewById(R.id.markAllAsKnown);

		findViewById(R.id.loadButton).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						loadFile();
					}
				});

		prev.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				prev();
			}
		});

		next.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				next();
			}
		});

		mark.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				markAllAsReaded();
			}
		});

		pageNumber = (TextView) findViewById(R.id.pageNumber);

		loadText();
	}

	protected void markAllAsReaded() {
		contentView.markAsReaded();
		contentView.invalidate();
	}

	protected void prev() {
		int p = section.getCurrentPage() - 1;
		section.setCurrentPage(p);
		loadPage();
		if (p == 0)
			prev.setEnabled(false);
		if (p + 2 == section.getPageCount())
			next.setEnabled(true);

		pageNumber.setText("Page " + (p + 1) + " of " + section.getPageCount());
	}

	protected void next() {
		int p = section.getCurrentPage() + 1;
		section.setCurrentPage(p);
		loadPage();
		if (p > 0)
			prev.setEnabled(true);
		if (p + 1 == section.getPageCount())
			next.setEnabled(false);

		pageNumber.setText("Page " + (p + 1) + " of " + section.getPageCount());
	}

	private void loadPage() {
		contentView.setText(section.getPage(), paint, lineHeight, lineWidth);
		contentView.invalidate();
	}

	private void loadText() {
		String file = (String) getIntent().getExtras().get(FILE);
		try {
			StringBuffer sb = new StringBuffer();
			InputStreamReader reader = new InputStreamReader(
					new FileInputStream(file), "UTF-8");

			char[] buff = new char[1024 * 5];

			int r;
			while ((r = reader.read(buff, 0, 1024 * 5)) > 0) {
				sb.append(buff, 0, r);
			}

			reader.close();

			section = new Section(sb.toString());

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadFile() {
		int screenWidth = contentView.getWidth();
		int screenHeight = contentView.getHeight();

		// DisplayMetrics dm = new DisplayMetrics();
		// getWindowManager().getDefaultDisplay().getMetrics(dm);
		// int screenWidth = dm.widthPixels;
		// int screenHeight = dm.heightPixels;

		paint = new TextPaint();

		paint.setTextSize(FONT_SIZE);

		lineHeight = dipToPixels(FONT_SIZE);
		int maxLineCount = screenHeight / lineHeight;

		lineWidth = screenWidth;

		section.splitOnPages(paint, screenWidth, maxLineCount);

		next.setEnabled(section.getPageCount() > 1);
		pageNumber.setText("Page 1 of " + section.getPageCount());

		if (section.getPageCount() > 0)
			loadPage();
	}

	private int dipToPixels(int dipValue) {
		Resources r = getResources();
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dipValue, r.getDisplayMetrics());
		return px;
	}

}
