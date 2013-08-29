package com.foreignreader;

import java.io.File;
import java.util.Date;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.foreignreader.cache.SectionCacheHelper;
import com.foreignreader.textimpl.TextWidthImpl;
import com.foreignreader.util.FastTranslator;
import com.foreignreader.util.SystemUiHider;
import com.foreignreader.util.TranslationHelper;
import com.reader.common.BookMetadata;
import com.reader.common.ObjectsFactory;
import com.reader.common.book.Book;
import com.reader.common.book.BookLoader;
import com.reader.common.book.SectionMetadata;
import com.reader.common.impl.SectionImpl;
import com.reader.common.impl.SimpleTextWithSymbolsParser;
import com.reader.common.pages.Section;

public class ReaderActivity extends Activity {

	private String fontName = "serif";

	public int fontSize = 30;

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

	private Button markWord;

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

	private TextView pageNumber;

	private SeekBar seekPageBar;

	private long downTime = -1;

	private float downY;

	private float downX;

	private int background;

	private int foreground;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (BooksActivity.TESTING_STORGE)
			ObjectsFactory.storageFile = new File(getFilesDir(), "words.db");
		else {
			File file = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES).getParentFile(),
					"Foreign Reader");
			file.mkdirs();
			ObjectsFactory.storageFile = new File(file, "words.db");
		}

		landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

		fastTranslator = new FastTranslator();

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		splitPages = landscape
				&& preferences.getBoolean("split_on_landscape", true);

		fontSize = preferences.getInt("font_size", 30);

		fontName = preferences.getString("font_family", "serif");

		background = preferences.getInt("reader_bk_color", Color.WHITE);

		foreground = preferences.getInt("reader_fk_color", Color.BLACK);

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

		contentView.setBackgroundColor(background);

		contentView.setForegroundColor(foreground);

		final View controlsView = findViewById(R.id.fullscreen_controls);

		mark = (Button) findViewById(R.id.markAllAsKnown);
		markWord = (Button) findViewById(R.id.markWordButton);
		fastTranslation = (TextView) findViewById(R.id.translationText);
		fastTranslation.setBackgroundColor(Color.WHITE);
		seekPageBar = (SeekBar) findViewById(R.id.seekPageBar);

		seekPageBar
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					private int i;

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						section.setCurrentPage(i);
						loadPage(i + 1);
						updateNextPrevEnable();
						hideControls();
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						i = progress;
						if (splitPages)
							progress *= 2;
						pageNumber.setText(Integer.toString(progress + 1));
					}
				});

		pageNumber = (TextView) findViewById(R.id.showPageText);
		((Button) findViewById(R.id.selectPageButton))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						int currentPage = section.getCurrentPage();
						seekPageBar.setProgress(currentPage);
						seekPageBar.setMax(section.getPageCount() - 1);

						if (splitPages)
							currentPage *= 2;

						pageNumber.setText(Integer.toString(currentPage + 1));
						pageNumber.setVisibility(View.VISIBLE);
						seekPageBar.setVisibility(View.VISIBLE);
					}
				});

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
			markWord.setEnabled(false);
		}

		mark.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				markAllAsReaded();
			}
		});

		markWord.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				markWord();
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

	protected void markWord() {
		contentView.markWord(selectedText.text);
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
			if (markWord.isEnabled()) {
				markWord.setEnabled(false);
				sendButton.setEnabled(false);
				if (translationEnable)
					translateButton.setEnabled(false);
			}
		} else {
			if (!markWord.isEnabled()) {
				markWord.setEnabled(true);
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
		next = true;
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

	private void updateNextPrevEnable() {
		int p = section.getCurrentPage();
		if (p + 1 == section.getPageCount()
				&& currentSection + 1 == sectionCount)
			next = false;
		else
			next = true;
		if (p == 0 && currentSection == 0)
			prev = false;
		else
			prev = true;
	}

	private void loadPage(int page) {
		String chapter = section.getSection().getTitle();
		if (chapter == null)
			chapter = "";
		else {
			final StringBuilder b = new StringBuilder();

			SimpleTextWithSymbolsParser p = new SimpleTextWithSymbolsParser() {

				int l;

				@Override
				public void processWord(char[] txt, int start, int len) {
					l += len;
					if (l > 35)
						return;

					b.append(txt, start, len);
					b.append(' ');
				}
			};

			p.parse(chapter.toCharArray());

			chapter = b.toString().trim();
		}
		contentView.setText(section.getPage(), (TextWidthImpl) textWidth,
				lineHeight, lineWidth, splitPages, page,
				section.getPageCount(), chapter);
		contentView.invalidate();
		updateTextMarkButtons(false);
		if (mSystemUiHider.isVisible()) {
			hideControls();
		}
		updateNextPrevEnable();
	}

	private void hideControls() {
		mSystemUiHider.hide();
		if (pageNumber.getVisibility() == View.VISIBLE) {
			pageNumber.setVisibility(View.INVISIBLE);
			seekPageBar.setVisibility(View.INVISIBLE);
		}
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
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			splitPages = preferences.getBoolean("split_on_landscape", true);
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
				landscape, splitPages, fontName, fontSize,
				contentView.getWidth(), contentView.getHeight());
		if (section == null) {
			loadNativeSection();
			sectionCacheHelper.setToCache(bookMetadata, currentSection,
					landscape, splitPages, fontName, fontSize,
					(SectionImpl) section, contentView.getWidth(),
					contentView.getHeight());
		} else {
			int screenWidth = contentView.getWidth();

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

		paint.setTextSize(fontSize);
		Typeface tf;
		if ("serif".equals(fontName))
			tf = Typeface.create(fontName, Typeface.NORMAL);
		else
			tf = Typeface.createFromFile(fontName);

		paint.setTypeface(tf);
		paint.setAntiAlias(true);

		lineHeight = dipToPixels(fontSize) + 3;

		lineWidth = screenWidth;

		textWidth = new TextWidthImpl(paint);

		screenHeight -= fastTranslation.getHeight() + (int) (lineHeight * 0.3);

		int maxLineCount = screenHeight / lineHeight;

		float l = (screenHeight - (lineHeight * 0.1f)) - lineHeight
				* maxLineCount;
		lineHeight += l / maxLineCount;

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
