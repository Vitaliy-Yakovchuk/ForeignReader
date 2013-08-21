package com.foriegnreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.foriegnreader.words.WordsContent;
import com.reader.common.Word;

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
			final TextView textView = (TextView) rootView
					.findViewById(R.id.word_detail);
			textView.setText(mItem.getText());

			((Button) rootView.findViewById(R.id.wordSendButton))
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent sendIntent = new Intent();
							sendIntent.setAction(Intent.ACTION_SEND);
							sendIntent.putExtra(Intent.EXTRA_TEXT,
									mItem.getText());
							sendIntent.setType("text/plain");
							startActivity(Intent.createChooser(sendIntent,
									getResources().getText(R.string.send_to)));
						}
					});

			((Button) rootView.findViewById(R.id.wordTranslateButton))
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							TextOnScreen textOnScreen = new TextOnScreen();
							textOnScreen.text = mItem.getText();
							int location[] = new int[2];
							textView.getLocationOnScreen(location);

							textOnScreen.x = location[0];
							textOnScreen.y = location[1]+textView.getHeight();

							TranslationHelper.translate(rootView.getContext(),
									textOnScreen);
						}
					});
		}

		return rootView;
	}
}
