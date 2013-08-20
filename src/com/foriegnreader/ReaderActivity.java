package com.foriegnreader;

import java.io.File;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.foriegnreader.R;
import com.foriegnreader.pages.Section;
import com.foriegnreader.textimpl.TextWidthImpl;
import com.reader.common.ColorConstants;
import com.reader.common.fb2.FictionBook;

public class ReaderActivity extends Activity implements OnGestureListener {

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

	private Button yellow;

	private Button blue;

	private Button white;

	private EditText selectedText;

	private GestureDetector gestureScanner;

	private TextWidthImpl textWidth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gestureScanner = new GestureDetector(this.getApplicationContext(), this);

		setContentView(R.layout.activity_reader);

		View findViewById = findViewById(R.id.fullscreen_content);
		contentView = (PageView) findViewById;

		prev = (Button) findViewById(R.id.prevPage);
		next = (Button) findViewById(R.id.nextPage);
		mark = (Button) findViewById(R.id.markAllAsKnown);
		yellow = (Button) findViewById(R.id.yellowButton);
		blue = (Button) findViewById(R.id.blueButton);
		white = (Button) findViewById(R.id.whiteButton);

		selectedText = (EditText) findViewById(R.id.selectedText);

		{
			yellow.setEnabled(false);
			white.setEnabled(false);
			blue.setEnabled(false);
		}

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

		yellow.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				markColor(ColorConstants.YELLOW);
			}
		});

		white.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				markColor(ColorConstants.WHITE);
			}
		});

		blue.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				markColor(ColorConstants.BLUE);
			}
		});

		selectedText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				updateTextMarkButtons(s.length() > 0);
			}
		});

		pageNumber = (TextView) findViewById(R.id.pageNumber);

		loadText();
	}

	protected void markColor(String color) {
		contentView.setColor(selectedText.getText().toString(), color);
		contentView.clearSelection();
		contentView.invalidate();
	}

	public void selectText(String text) {
		selectedText.setText(text);
		updateTextMarkButtons(text.length() > 0);
		contentView.invalidate();
	}

	private void updateTextMarkButtons(boolean contain) {
		if (!contain) {
			if (blue.isEnabled()) {
				blue.setEnabled(false);
				yellow.setEnabled(false);
				white.setEnabled(false);
			}
		} else {
			if (!blue.isEnabled()) {
				blue.setEnabled(true);
				yellow.setEnabled(true);
				white.setEnabled(true);
			}
		}
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
		contentView.setText(section.getPage(), (TextWidthImpl) textWidth,
				lineHeight, lineWidth);
		contentView.invalidate();
	}

	private void loadText() {
		String file = (String) getIntent().getExtras().get(FILE);
		try {
			FictionBook book = new FictionBook(new File(file));

			StringBuffer sb = new StringBuffer();

			for (String s : book.getSections()) {
				sb.append(s);
				sb.append(' ');
			}

			section = new Section(book.getSection(0));
		} catch (Exception e) {
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
		Typeface tf = Typeface.create("serif", Typeface.NORMAL);

		paint.setTypeface(tf);
		paint.setAntiAlias(true);

		lineHeight = dipToPixels(FONT_SIZE) + 3;
		int maxLineCount = screenHeight / lineHeight;

		lineWidth = screenWidth;

		textWidth = new TextWidthImpl(paint);

		section.splitOnPages(textWidth, screenWidth, maxLineCount);

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

	private int toContentViewX(float x) {
		int[] location = new int[2];
		contentView.getLocationOnScreen(location);
		return Math.round(x - location[0]);
	}

	private int toContentViewY(float y) {
		int[] location = new int[2];
		contentView.getLocationOnScreen(location);
		return Math.round(y - location[1]);
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		return gestureScanner.onTouchEvent(me);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// selectedText.setText("-" + "FLING" + "-");
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// selectedText.setText("-" + "LONG PRESS" + "-");
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		String text = contentView.select(toContentViewX(e1.getX()),
				toContentViewY(e1.getY()), toContentViewX(e2.getX()),
				toContentViewY(e2.getY()));
		if (text != null)
			selectText(text);
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		String text = contentView.select(toContentViewX(e.getX()),
				toContentViewY(e.getY()), toContentViewX(e.getX()),
				toContentViewY(e.getY()));
		if (text != null)
			selectText(text);
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		String text = contentView.select(toContentViewX(e.getX()),
				toContentViewY(e.getY()), toContentViewX(e.getX()),
				toContentViewY(e.getY()));
		if (text != null)
			selectText(text);
		return true;
	}

}
