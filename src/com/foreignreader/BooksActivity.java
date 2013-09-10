package com.foreignreader;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.foreignreader.R;
import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;
import com.reader.common.BookMetadata;
import com.reader.common.BooksDatabase;
import com.reader.common.Database;
import com.reader.common.ObjectsFactory;
import com.reader.common.book.Book;
import com.reader.common.book.BookLoader;
import com.reader.common.book.Sentence;
import com.reader.common.book.SentenceParserCallback;

public class BooksActivity extends Activity {

	private static final int REQUEST_LOAD = 0;

	private static final int REQUEST_OPEN_WORDS = 1;

	private static final int REQUEST_OPEN_SETTING = 2;

	private BookFileAdapter adapter;

	private List<BookMetadata> bookFiles;

	public final static boolean TESTING_STORGE = false;// false;

	private BooksDatabase booksDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_books);

		if (TESTING_STORGE)
			ObjectsFactory.storageFile = new File(getFilesDir(), "words.db");
		else {
			File file = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES).getParentFile(),
					"Foreign Reader");
			file.mkdirs();
			ObjectsFactory.storageFile = new File(file, "words.db");
		}

		final ListView books = (ListView) findViewById(R.id.bookListView);

		books.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@SuppressWarnings("rawtypes")
			@Override
			public void onItemClick(AdapterView parentView, View childView,
					int position, long id) {
				loadBook(bookFiles.get(position));
			}
		});

		booksDatabase = ObjectsFactory.getDefaultBooksDatabase();

		bookFiles = booksDatabase.getBooks();

		adapter = new BookFileAdapter(this, R.layout.book_item, bookFiles);
		books.setAdapter(adapter);
	}

	public void removeBook(View view) {
		BookMetadata bm = (BookMetadata) view.getTag();
		booksDatabase.removeBook(bm);
		adapter.remove(bm);
		adapter.notifyDataSetChanged();
	}

	public void openBook(View view) {
		loadBook((BookMetadata) view.getTag());
	}

	public void scanBook(View view) {
		final BookMetadata bm = (BookMetadata) view.getTag();
		Book book;
		try {
			book = BookLoader.loadBook(new File(bm.getFileName()));
			book.scanForSentences(new SentenceParserCallback() {

				Database database = ObjectsFactory.getDefaultDatabase();

				@Override
				public boolean found(Sentence sentence) {
					database.addSentence(sentence.text, bm.getName(),
							sentence.section, -1);
					return false;
				}
			});
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(e.getLocalizedMessage());
			builder.create().show();
		}

	}

	protected void openSettingActivity() {
		Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
		startActivityForResult(intent, REQUEST_OPEN_SETTING);
	}

	protected void openWordsActivity() {
		Intent intent = new Intent(getBaseContext(), WordListActivity.class);
		startActivityForResult(intent, REQUEST_OPEN_WORDS);

	}

	protected void loadBook(BookMetadata filebook) {
		Intent intent = new Intent(getBaseContext(), ReaderActivity.class);
		intent.putExtra(ReaderActivity.FILE, filebook.getFileName());

		startActivity(intent);
	}

	private void pickFile(File aFile) {
		Intent intent = new Intent(getBaseContext(), FileDialog.class);
		intent.putExtra(FileDialog.START_PATH, Environment
				.getExternalStorageDirectory().getAbsolutePath());

		// can user select directories or not
		intent.putExtra(FileDialog.CAN_SELECT_DIR, false);

		intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);

		// alternatively you can set file filter
		// intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "png" });

		startActivityForResult(intent, REQUEST_LOAD);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {

			if (requestCode == REQUEST_LOAD) {
				System.out.println("Loading...");
			}

			String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
			BookMetadata bookMetadata = createMetadata(filePath);

			adapter.insert(bookMetadata, 0);
			adapter.notifyDataSetChanged();
		} else if (resultCode == Activity.RESULT_CANCELED) {
			Logger.getLogger(BooksActivity.class.getName()).log(Level.WARNING,
					"file not selected");
		}
	}

	private BookMetadata createMetadata(String path) {
		File file = new File(path);

		BookMetadata bookMetadata = new BookMetadata();
		bookMetadata.setFileName(file.getAbsolutePath());
		// bookMetadata.setFontSize(ReaderActivity.FONT_SIZE);
		bookMetadata.setLastOpen(new Date());
		bookMetadata.setName(file.getName());

		booksDatabase.setBook(bookMetadata);

		return bookMetadata;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.books, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_file:
			pickFile(null);
			return true;
		case R.id.action_settings:
			openSettingActivity();
			return true;
		case R.id.known_words:
			openWordsActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
