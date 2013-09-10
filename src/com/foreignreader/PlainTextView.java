package com.foreignreader;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.foreignreader.textimpl.TextWidthImpl;
import com.foreignreader.util.FastTranslator;
import com.foreignreader.util.TranslationHelper;
import com.reader.common.ObjectsFactory;
import com.reader.common.Word;
import com.reader.common.book.Section;
import com.reader.common.impl.SectionImpl;

public class PlainTextView extends RelativeLayout {

	private Section section;

	private PageView contentView;

	private TextPaint paint;

	private int fontSize;

	private String fontName;

	private int lineHeight;

	private int lineWidth;

	private TextWidthImpl textWidth;

	private View rootView;

	private GestureDetector gestureScanner;

	private TextOnScreen selectedText;

	private FastTranslator fastTranslator = new FastTranslator();

	private TextView fastTranslation;

	private Button sendButton;

	private Button markWord;

	private Button translateButton;

	private float downX;

	private float downY;

	public PlainTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PlainTextView(Context context) {
		super(context);
		init(context);
	}

	private void init(final Context context) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (inflater != null)
			rootView = inflater.inflate(R.layout.text_view, this);

		contentView = (PageView) rootView.findViewById(R.id.pageView);
		contentView.setPaintBottom(false);
		contentView.setLoadPage(new Runnable() {

			@Override
			public void run() {
				loadSection();
			}
		});

		contentView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				return PlainTextView.this.onTextTouchEvent(arg1);
			}
		});

		markWord = (Button) findViewById(R.id.markWordButton);
		markWord.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				markWord();
			}
		});

		sendButton = (Button) rootView.findViewById(R.id.sendButton);
		sendButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				sendText();
			}
		});

		fastTranslation = (TextView) findViewById(R.id.translationText);
		fastTranslation.setBackgroundColor(Color.WHITE);
		fastTranslation.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Word word = ObjectsFactory.getDefaultDatabase().getWordInfo(
						TranslationHelper
						.normilize(selectedText.text).toLowerCase(Locale.getDefault()));
				if (word != null)
					openWordInfo(word);
			}
		});


		translateButton = (Button) rootView.findViewById(R.id.translateButton);
		translateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				TextOnScreen tos = new TextOnScreen();
				tos.text = selectedText.text;
				tos.x = selectedText.x;
				int[] location = new int[2];
				contentView.getLocationOnScreen(location);
				tos.y = Math.round(selectedText.y + location[1]);
				if (!TranslationHelper.translate(context, tos)) {
					translateButton.setEnabled(false);
				}
			}
		});

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);

		fontSize = preferences.getInt("font_size", 30);

		fontName = preferences.getString("font_family", "serif");

		gestureScanner = new GestureDetector(context,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public void onShowPress(MotionEvent e) {
						TextOnScreen text = contentView.select(
								toContentViewX(e.getX()),
								toContentViewY(e.getY()),
								toContentViewX(e.getX()),
								toContentViewY(e.getY()), (int) e.getX(),
								(int) e.getY());
						if (text != null)
							selectText(text);
					}

					@Override
					public boolean onSingleTapUp(MotionEvent e) {
						TextOnScreen text = contentView.select(
								toContentViewX(e.getX()),
								toContentViewY(e.getY()),
								toContentViewX(e.getX()),
								toContentViewY(e.getY()), (int) e.getX(),
								(int) e.getY());
						if (text != null)
							selectText(text);
						return true;
					}
				});

	}

	protected void openWordInfo(Word word) {
		Intent intent = new Intent(getContext().getApplicationContext(),
				WordDetailActivity.class);
		intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(WordDetailFragment.ARG_ITEM_ID, word.getText());
		getContext().getApplicationContext().startActivity(intent);
	}

	protected void markWord() {
		contentView.markWord(selectedText.text);
		contentView.clearSelection();
		contentView.invalidate();
	}

	protected void sendText() {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT,
				TranslationHelper.normilize(selectedText.text));
		sendIntent.setType("text/plain");
		getContext().startActivity(
				Intent.createChooser(sendIntent,
						getResources().getText(R.string.send_to)));
	}

	public void selectText(TextOnScreen text) {
		selectedText = text;
		updateTextMarkButtons(text.text.length() > 0);
		contentView.invalidate();
		String normilize = TranslationHelper.normilize(text.text);
		boolean clean = true;
		if (normilize.length() > 0) {
			String m = fastTranslator.getMeaning(normilize);
			if (m != null) {
				clean = false;
				SpannableString spannableString = new SpannableString(m);
				spannableString.setSpan(new BackgroundColorSpan(Color.WHITE),
						0, m.length(), 0);
				fastTranslation.setText(m);
			}
		}
		if (clean)
			fastTranslation.setText("");
	}

	private void updateTextMarkButtons(boolean contain) {
		if (!contain) {
			if (markWord.isEnabled()) {
				markWord.setEnabled(false);
				sendButton.setEnabled(false);
				translateButton.setEnabled(false);
			}
		} else {
			if (!markWord.isEnabled()) {
				markWord.setEnabled(true);
				sendButton.setEnabled(true);
				translateButton.setEnabled(true);
			}
		}

	}

	private int toContentViewX(float x) {
		int[] location = new int[2];
		// this.getLocationOnScreen(location);
		return Math.round(x - location[0]);
	}

	private int toContentViewY(float y) {
		int[] location = new int[2];
		// this.getLocationOnScreen(location);
		return Math.round(y - location[1]);
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		loadSection();
	}

	protected void loadSection() {
		SectionImpl sectionImpl = new SectionImpl(section);

		paint = new TextPaint();

		paint.setTextSize(fontSize);
		Typeface tf;
		if ("serif".equals(fontName))
			tf = Typeface.create(fontName, Typeface.NORMAL);
		else
			tf = Typeface.createFromFile(fontName);

		paint.setTypeface(tf);
		paint.setAntiAlias(true);

		lineHeight = dipToPixels(fontSize) + 3;

		lineWidth = contentView.getWidth();

		textWidth = new TextWidthImpl(paint);

		sectionImpl.splitOnPages(textWidth, lineWidth, Integer.MAX_VALUE);

		if (sectionImpl.getPage() != null) {
			contentView.setText(sectionImpl.getPage(), textWidth, lineHeight,
					lineWidth, false, 0, 0, "");
			contentView.getLayoutParams().height = lineHeight
					* sectionImpl.getPage().getMaxLine()
					+ fastTranslation.getHeight() + lineHeight;
			rootView.requestLayout();
		}
	}

	public boolean onTextTouchEvent(MotionEvent me) {
		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			downX = me.getX();
			downY = me.getY();
		} else if (me.getAction() == MotionEvent.ACTION_MOVE) {
			TextOnScreen text = contentView
					.select(toContentViewX(downX), toContentViewY(downY),
							toContentViewX(me.getX()),
							toContentViewY(me.getY()), (int) me.getX(),
							(int) me.getY());
			if (text != null) {
				selectText(text);
				return true;
			}
		}
		return gestureScanner.onTouchEvent(me);
	}

	private int dipToPixels(int dipValue) {
		Resources r = getResources();
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dipValue, r.getDisplayMetrics());
		return px;
	}

	public void setText(Section section) {
		this.section = section;
		post(new Runnable() {

			@Override
			public void run() {
				if (contentView != null)
					loadSection();
			}
		});
	}
}
