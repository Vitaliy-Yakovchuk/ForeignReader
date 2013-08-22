package com.foriegnreader;

import java.io.File;

import java.util.Date;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.foriegnreader.cache.SectionCacheHelper;
import com.foriegnreader.textimpl.TextWidthImpl;
import com.foriegnreader.util.FastTranslator;
import com.foriegnreader.util.SystemUiHider;
import com.reader.common.BookMetadata;
import com.reader.common.ColorConstants;
import com.reader.common.ObjectsFactory;
import com.reader.common.book.Book;
import com.reader.common.book.BookLoader;
import com.reader.common.book.SectionMetadata;
import com.reader.common.impl.SectionImpl;
import com.reader.common.pages.Section;

public class ReaderActivity extends Activity {

	public static final int FONT_SIZE = 32;

	public static final String FILE = "FileName";

	public static final String CURRENT_SECTION = "CURRENT_SECTION";

	private Section section;

	private PageView contentView;

	private boolean prev;

	private boolean next;

	private TextPaint paint;

	private int lineHeight;

	private int lineWidth;

	private Button mark;

	private Button yellow;

	private Button blue;

	private Button white;

	private GestureDetector gestureScanner;

	private TextWidthImpl textWidth;

	public static boolean splitPages = true;

	protected static int currentSection;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = 0;// SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	private Book book;

	private int sectionCount;

	private TextOnScreen selectedText;

	private Button sendButton;

	private Button translateButton;

	private boolean translationEnable = true;

	private BookMetadata bookMetadata;

	private SectionCacheHelper sectionCacheHelper;

	private boolean landscape;

	private FastTranslator fastTranslator;

	private TextView fastTranslation;

	private long downTime = -1;

	private float downY;

	private float downX;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

		fastTranslator = new FastTranslator();

		splitPages = landscape;

		sectionCacheHelper = new SectionCacheHelper(this);

		getActionBar().hide();

		gestureScanner = new GestureDetector(this.getApplicationContext(),
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (e.getX() < contentView.getWidth() / 3) {
							if (prev)
								prev();
						} else if (e.getX() > contentView.getWidth() / 3 * 2) {
							if (next)
								next();
						} else {
							if (mSystemUiHider.isVisible()) {
								hideControls();
							} else {
								showControls();
							}
						}
						return true;
					}

					@Override
					public void onShowPress(MotionEvent e) {
						if (downTime == -1)
							return;
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
						if (downTime == -1)
							return true;
						TextOnScreen text = contentView.select(
								toContentViewX(e.getX()),
								toContentViewY(e.getY()),
								toContentViewX(e.getX()),
								toContentViewY(e.getY()), (int) e.getX(),
								(int) e.getY());
						if (text != null)
							selectText(text);
						else {
							if (e.getX() < contentView.getWidth() / 20) {
								if (prev)
									prev();
							} else if (e.getX() > contentView.getWidth()
									- contentView.getWidth() / 20) {
								if (next)
									next();
							} else if (splitPages
									&& e.getX() > contentView.getWidth() / 2
											- contentView.getWidth() / 20
									&& e.getX() < contentView.getWidth() / 2
											+ contentView.getWidth() / 20) {
								if (mSystemUiHider.isVisible()) {
									hideControls();
								} else {
									showControls();
								}
							}
						}
						return true;
					}
				});

		setContentView(R.layout.activity_reader);

		contentView = (PageView) findViewById(R.id.fullscreen_content);
		final View controlsView = findViewById(R.id.fullscreen_controls);

		mark = (Button) findViewById(R.id.markAllAsKnown);
		yellow = (Button) findViewById(R.id.yellowButton);
		blue = (Button) findViewById(R.id.blueButton);
		white = (Button) findViewById(R.id.whiteButton);
		fastTranslation = (TextView) findViewById(R.id.translationText);
		fastTranslation.setBackgroundColor(Color.WHITE);

		((Button) findViewById(R.id.selectChapterButton))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							selectChapter();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

		sendButton = (Button) findViewById(R.id.sendButton);
		sendButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				sendText();
			}
		});

		translateButton = (Button) findViewById(R.id.translateButton);

		translateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!TranslationHelper.translate(ReaderActivity.this,
						selectedText)) {
					translationEnable = false;
					translateButton.setEnabled(false);
				}
			}
		});

		contentView.setLoadPage(new Runnable() {

			@Override
			public void run() {
				loadSection();
			}
		});

		{
			yellow.setEnabled(false);
			white.setEnabled(false);
			blue.setEnabled(false);
		}

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

		loadText();

		// ---FULL SCREEN

		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

					}
				});

	}

	protected void sendText() {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT,
				TranslationHelper.normilize(selectedText.text));
		sendIntent.setType("text/plain");
		startActivity(Intent.createChooser(sendIntent,
				getResources().getText(R.string.send_to)));
	}

	protected void selectChapter() throws Exception {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Chapter");
		List<SectionMetadata> l = book.getSections();
		CharSequence[] charSequences = new CharSequence[l.size()];
		for (int i = l.size() - 1; i >= 0; i--)
			charSequences[i] = l.get(i).getTitle();
		builder.setItems(charSequences, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				try {
					ReaderActivity.currentSection = which;
					loadSection();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Dialog d = builder.create();
		d.show();

	}

	protected void markColor(String color) {
		contentView.setColor(selectedText.text, color);
		contentView.clearSelection();
		fastTranslation.setText("");
		contentView.invalidate();
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
		if (!mSystemUiHider.isVisible())
			showControls();
	}

	private void updateTextMarkButtons(boolean contain) {
		if (!contain) {
			if (blue.isEnabled()) {
				blue.setEnabled(false);
				yellow.setEnabled(false);
				white.setEnabled(false);
				sendButton.setEnabled(false);
				if (translationEnable)
					translateButton.setEnabled(false);
			}
		} else {
			if (!blue.isEnabled()) {
				blue.setEnabled(true);
				yellow.setEnabled(true);
				white.setEnabled(true);
				sendButton.setEnabled(true);
				if (translationEnable)
					translateButton.setEnabled(true);
			}
		}
	}

	protected void markAllAsReaded() {
		contentView.markAsReaded();
		contentView.invalidate();
	}

	protected void prev() {
		int p = section.getCurrentPage() - 1;
		if (p < 0) {
			currentSection--;
			loadSection();
			p = section.getPageCount() - 1;
			if (p >= 0) {
				section.setCurrentPage(p);
				loadPage(p + 1);
			}
			if (p == 0 && currentSection == 0)
				prev = false;
			return;
		}
		section.setCurrentPage(p);
		loadPage(p + 1);
		if (p == 0 && currentSection == 0)
			prev = false;
	}

	protected void next() {
		int p = section.getCurrentPage() + 1;
		if (p == section.getPageCount()) {
			currentSection++;
			loadSection();
			return;
		}
		section.setCurrentPage(p);
		loadPage(p + 1);
		prev = true;
		if (p + 1 == section.getPageCount()
				&& currentSection + 1 == sectionCount)
			next = false;
	}

	private void loadPage(int page) {
		contentView
				.setText(section.getPage(), (TextWidthImpl) textWidth,
						lineHeight, lineWidth, splitPages, page,
						section.getPageCount());
		contentView.invalidate();
		updateTextMarkButtons(false);
		if (mSystemUiHider.isVisible()) {
			hideControls();
		}
	}

	private void hideControls() {
		mSystemUiHider.hide();
	}

	private void loadText() {
		String file = (String) getIntent().getExtras().get(FILE);
		bookMetadata = ObjectsFactory.getDefaultBooksDatabase().getBook(file);
		currentSection = bookMetadata.getLastSection();
		bookMetadata.setLastOpen(new Date());
		ObjectsFactory.getDefaultBooksDatabase().setBook(bookMetadata);
		try {
			book = BookLoader.loadBook(new File(file));
			sectionCount = book.getSections().size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			landscape = true;
			splitPages = true;
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			landscape = false;
			splitPages = false;
		}
		bookMetadata.setLastSection(currentSection);
		if (section != null)
			bookMetadata.setLastPosition(section.getCurrentCharacter());
		contentView.setWords(null);

	}

	private void loadSection() {

		section = sectionCacheHelper.getFromCache(bookMetadata, currentSection,
				landscape, splitPages, FONT_SIZE, contentView.getWidth(),
				contentView.getHeight());
		if (section == null) {
			loadNativeSection();
			sectionCacheHelper.setToCache(bookMetadata, currentSection,
					landscape, splitPages, FONT_SIZE, (SectionImpl) section,
					contentView.getWidth(), contentView.getHeight());
		} else {
			int screenWidth = contentView.getWidth();

			paint = new TextPaint();

			paint.setTextSize(FONT_SIZE);
			Typeface tf = Typeface.create("serif", Typeface.NORMAL);

			paint.setTypeface(tf);
			paint.setAntiAlias(true);

			lineHeight = dipToPixels(FONT_SIZE) + 3;

			lineWidth = screenWidth;

			textWidth = new TextWidthImpl(paint);
		}

		next = section.getPageCount() > 1 || currentSection + 1 < sectionCount;
		prev = currentSection > 0;
		if (section.getPageCount() > 0) {
			if (currentSection == bookMetadata.getLastSection()) {
				section.setCurrentPageByCharacteNumber(bookMetadata
						.getLastPosition());
				int page = section.getCurrentPage();
				next = section.getPageCount() > page + 1
						|| currentSection + 1 < sectionCount;
				prev = page > 0 || currentSection + 1 < sectionCount;

			}
			loadPage(section.getCurrentPage() + 1);
			bookMetadata.setLastPosition(section.getCurrentPage());
			bookMetadata.setLastSection(currentSection);
			ObjectsFactory.getDefaultBooksDatabase().setBook(bookMetadata);
		}
	}

	private void loadNativeSection() {
		try {
			section = new SectionImpl(book.getSection(currentSection));
		} catch (Exception e) {
			e.printStackTrace();
		}
		int screenWidth = contentView.getWidth();
		int screenHeight = contentView.getHeight();

		paint = new TextPaint();

		paint.setTextSize(FONT_SIZE);
		Typeface tf = Typeface.create("serif", Typeface.NORMAL);

		paint.setTypeface(tf);
		paint.setAntiAlias(true);

		lineHeight = dipToPixels(FONT_SIZE) + 3;

		lineWidth = screenWidth;

		textWidth = new TextWidthImpl(paint);

		int maxLineCount = screenHeight / lineHeight - 1;// last row is system
		// bar
		if (splitPages) {
			maxLineCount *= 2;
			screenWidth = (int) (screenWidth * 0.46);
		} else
			screenWidth = (int) (screenWidth * 0.96);

		section.splitOnPages(textWidth, screenWidth, maxLineCount);
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
		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			downTime = System.currentTimeMillis();
			downX = me.getX();
			downY = me.getY();
		} else if (me.getAction() == MotionEvent.ACTION_MOVE) {
			if (downTime == -1)
				return false;
			if (System.currentTimeMillis() - downTime < 210) {
				return tryChangePage(me);
			}
			if (downTime == -1)
				return true;
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

	private boolean tryChangePage(MotionEvent e2) {
		float distanceX;
		float distanceY;
		distanceX = e2.getX() - downX;
		distanceY = e2.getY() - downY;
		if (Math.abs(distanceX) > Math.abs(distanceY)
				&& contentView.getWidth() * 0.05 < Math.abs(distanceX)) {
			if (distanceX > 0) {
				if (prev) {
					prev();
					downTime = -1;
					return true;
				}
			} else {
				if (next) {
					next();
					downTime = -1;
					return true;
				}
			}
		}
		return true;
	}

	private void showControls() {
		mSystemUiHider.show();
	}

	@Override
	protected void onStop() {
		super.onStop();
		bookMetadata.setLastSection(currentSection);
		bookMetadata.setLastPosition(section.getCurrentCharacter());
		sectionCacheHelper.close();

		ObjectsFactory.getDefaultBooksDatabase().setBook(bookMetadata);
		fastTranslator.close();
	}

}
