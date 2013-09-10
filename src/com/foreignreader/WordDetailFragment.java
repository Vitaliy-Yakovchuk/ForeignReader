package com.foreignreader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.foreignreader.util.LongTranslationHelper;
import com.foreignreader.words.WordsContent;
import com.reader.common.Word;
import com.reader.common.book.Section;

/**
 * A fragment representing a single Word detail screen. This fragment is either
 * contained in a {@link WordListActivity} in two-pane mode (on tablets) or a
 * {@link WordDetailActivity} on handsets.
 */
public class WordDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private Word mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public WordDetailFragment() {
	}

	private LongTranslationHelper longTranslationHelper = new LongTranslationHelper();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		longTranslationHelper = new LongTranslationHelper();

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = WordsContent.get(getArguments().getString(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_word_detail,
				container, false);

		// Show the dummy content as text in a TextView.
		if (mItem != null) {

			final PlainTextView longTranslation = (PlainTextView) rootView
					.findViewById(R.id.longTranslationText);

			Section builder = new Section();

			longTranslationHelper.getTranslation(mItem.getText(), builder);

			longTranslation.setText(builder);
		}

		return rootView;
	}

	@Override
	public void onStop() {
		super.onStop();
	}
}
