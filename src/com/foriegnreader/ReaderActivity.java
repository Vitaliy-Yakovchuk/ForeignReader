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
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.foriegnreader.cache.SectionCacheHelper;
import com.foriegnreader.textimpl.TextWidthImpl;
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

	private TextOnScreen selectedText;

	private Button sendButton;

	private Button translateButton;

	private boolean translationEnable = true;

	private BookMetadata bookMetadata;

	private SectionCacheHelper sectionCacheHelper;

	private boolean landscape;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

		splitPages = landscape;

		sectionCacheHelper = new SectionCacheHelper(this);

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
					public boolean onScroll(MotionEvent e1, MotionEvent e2,
							float distanceX, float distanceY) {
						TextOnScreen text = contentView.select(
								toContentViewX(e1.getX()),
								toContentViewY(e1.getY()),
								toContentViewX(e2.getX()),
								toContentViewY(e2.getY()), (int) e1.getX(),
								(int) e1.getY());
						if (text != null)
							selectText(text);
						return true;
					}

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
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
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
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

					}
				});

	}

	protected void sendText() {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, selectedText.text);
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
		contentView.invalidate();
	}

	public void selectText(TextOnScreen text) {
		selectedText = text;
		updateTextMarkButtons(text.text.length() > 0);
		contentView.invalidate();
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
		section.setCurrentPage(p);
		loadPage(p + 1);
		if (p == 0)
			prev = false;
		if (p + 2 == section.getPageCount())
			next = true;

	}

	protected void next() {
		int p = section.getCurrentPage() + 1;
		section.setCurrentPage(p);
		loadPage(p + 1);
		if (p > 0)
			prev = true;
		if (p + 1 == section.getPageCount())
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
		getActionBar().hide();
	}

	private void loadText() {
		String file = (String) getIntent().getExtras().get(FILE);
		bookMetadata = ObjectsFactory.getDefaultBooksDatabase().getBook(file);
		currentSection = bookMetadata.getLastSection();
		bookMetadata.setLastOpen(new Date());
		ObjectsFactory.getDefaultBooksDatabase().setBook(bookMetadata);
		try {
			book = BookLoader.loadBook(new File(file));
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

		next = section.getPageCount() > 1;
		if (section.getPageCount() > 0) {
			if (currentSection == bookMetadata.getLastSection()) {
				section.setCurrentPageByCharacteNumber(bookMetadata
						.getLastPosition());
				int page = section.getCurrentPage();
				next = section.getPageCount() > page + 1;
				prev = page > 0;

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
		return gestureScanner.onTouchEvent(me);
	}

	private void showControls() {
		mSystemUiHider.show();
		getActionBar().show();
	}

	@Override
	protected void onStop() {
		super.onStop();
		bookMetadata.setLastSection(currentSection);
		bookMetadata.setLastPosition(section.getCurrentCharacter());
		sectionCacheHelper.close();

		ObjectsFactory.getDefaultBooksDatabase().setBook(bookMetadata);
	}

}
