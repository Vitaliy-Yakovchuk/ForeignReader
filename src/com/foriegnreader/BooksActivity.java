package com.foriegnreader;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.foriegnreader.R;
import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;
import com.reader.common.ObjectsFactory;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class BooksActivity extends Activity {

	public static final String BOOK_FILE = "BOOKS";

	private static final int REQUEST_LOAD = 0;

	private ArrayAdapter<String> adapter;

	private ArrayList<String> files;

	public final static boolean TESTING_STORGE = false;

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

		final Button addFile = (Button) findViewById(R.id.addFileButton);

		final ListView books = (ListView) findViewById(R.id.bookListView);

		addFile.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				pickFile(null);
			}
		});

		books.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@SuppressWarnings("rawtypes")
			@Override
			public void onItemClick(AdapterView parentView, View childView,
					int position, long id) {
				openBook(files.get(position));
			}
		});

		SharedPreferences preferences = getSharedPreferences(BOOK_FILE, 0);
		String s = preferences.getString("lastFile", "");

		files = new ArrayList<String>();

		if (new File(s).exists()) {
			files.add(s);
		}

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, files);
		books.setAdapter(adapter);
	}

	protected void openBook(String fileName) {
		Intent intent = new Intent(getBaseContext(), ReaderActivity.class);
		intent.putExtra(ReaderActivity.FILE, fileName);

		startActivity(intent);
	}

	void pickFile(File aFile) {
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
			SharedPreferences settings = getSharedPreferences(BOOK_FILE, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("lastFile", filePath);
			editor.commit();
			adapter.add(filePath);
			adapter.notifyDataSetChanged();
		} else if (resultCode == Activity.RESULT_CANCELED) {
			Logger.getLogger(BooksActivity.class.getName()).log(Level.WARNING,
					"file not selected");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.books, menu);
		return true;
	}

}
