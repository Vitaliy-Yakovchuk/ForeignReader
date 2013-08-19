package com.foriegnreader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.example.foriegnreader.R;
import com.foriegnreader.pages.Section;
import com.reader.common.AbstractTextProcessor;
import com.reader.common.ColorConstants;
import com.reader.common.ObjectsFactory;
import com.reader.common.TextSource;
import com.reader.common.TextWithProperties;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ReaderActivity extends Activity {

	public static final String FILE = "FileName";

	private Section section;

	private TextView contentView;

	private Button prev;

	private Button next;

	private TextView pageNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_reader);

		contentView = (TextView) findViewById(R.id.fullscreen_content);

		prev = (Button) findViewById(R.id.prevPage);
		next = (Button) findViewById(R.id.nextPage);

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

		pageNumber = (TextView) findViewById(R.id.pageNumber);

		loadText();
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
		final String page = section.getPage();
		final SpannableString text = new SpannableString(page);

		TextSource ts = ObjectsFactory.createSimpleSource(page);

		ts.process(new AbstractTextProcessor() {

			int num = 0;

			@Override
			public void got(TextWithProperties textProperties) {
				while (page.charAt(num) != textProperties.getText().charAt(0))
					num++;
				int length = textProperties.getText().length();
				/*if (ColorConstants.YELLOW.equals(textProperties.getColor()))
					text.setSpan(new BackgroundColorSpan(Color.YELLOW), num,
							num + length, 0);
				else if (ColorConstants.BLUE.equals(textProperties.getColor()))
					text.setSpan(new BackgroundColorSpan(Color.BLUE), num, num
							+ length, 0);*/
				num += length;
			}
		});

		contentView.setText(text);
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
		// int screenWidth = contentView.getWidth();
		// int screenHeight = contentView.getHeight();

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;

		Paint paint = new Paint();
		paint.setTextSize(contentView.getTextSize());

		int maxLineCount = screenHeight / contentView.getLineHeight();
		contentView.setLines(maxLineCount);

		section.splitOnPages(paint, screenWidth, maxLineCount);

		next.setEnabled(section.getPageCount() > 1);
		pageNumber.setText("Page 1 of " + section.getPageCount());

		if (section.getPageCount() > 0)
			loadPage();
	}

}
