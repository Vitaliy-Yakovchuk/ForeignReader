package com.foreignreader;

import java.io.File;

import com.foreignreader.util.LongTranslationHelper;
import com.foreignreader.R;
import com.reader.common.ObjectsFactory;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

/**
 * An activity representing a list of Words. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link WordDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link WordListFragment} and the item details (if present) is a
 * {@link WordDetailFragment}.
 * <p>
 * This activity also implements the required {@link WordListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class WordListActivity extends FragmentActivity implements
		WordListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;

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

		
		setContentView(R.layout.activity_word_list);

		TextView stat = (TextView) findViewById(R.id.statisticTextView);

		if (BooksActivity.TESTING_STORGE)
			ObjectsFactory.storageFile = new File(getFilesDir(), "words.db");
		else {
			File file = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES).getParentFile(),
					"Foreign Reader");
			file.mkdirs();
			ObjectsFactory.storageFile = new File(file, "words.db");
		}

		int[] n = ObjectsFactory.getDefaultDatabase().getWordsCount();

		stat.setText("Known words: " + n[0] + "   Unknown words: " + n[1]);

		if (findViewById(R.id.word_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((WordListFragment) getSupportFragmentManager().findFragmentById(
					R.id.word_list)).setActivateOnItemClick(true);
		}

	}

	/**
	 * Callback method from {@link WordListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(WordDetailFragment.ARG_ITEM_ID, id);
			WordDetailFragment fragment = new WordDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.word_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, WordDetailActivity.class);
			detailIntent.putExtra(WordDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		new LongTranslationHelper().close();
	}
}
