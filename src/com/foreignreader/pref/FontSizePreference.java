package com.foreignreader.pref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.foreignreader.R;
import com.foreignreader.util.FontManager;

public class FontSizePreference extends Preference implements
		Preference.OnPreferenceClickListener {

	private static final int DEFAULT_VALUE = 30;

	private List<String> fontPaths;

	private String selectedFontPath;

	private int fontSize;

	public FontSizePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		View view = inflater.inflate(R.layout.dialog_font_preference, null);
		builder.setView(view)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								SharedPreferences.Editor editor = getSharedPreferences()
										.edit();

								editor.putString("font_family",
										selectedFontPath);
								editor.putInt("font_size", fontSize);

								editor.commit();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// LoginDialogFragment.this.getDialog().cancel();
							}
						});
		AlertDialog dialog = builder.create();

		SeekBar bar = (SeekBar) view.findViewById(R.id.font_size_bar);

		bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				fontSize = seekBar.getProgress() + 10;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}
		});

		RadioGroup group = (RadioGroup) view
				.findViewById(R.id.font_chooser_group);

		bar.setProgress(getSharedPreferences().getInt("font_size",
				DEFAULT_VALUE) - 10);

		HashMap<String, String> fonts = FontManager.enumerateFonts();
		fontPaths = new ArrayList<String>();

		selectedFontPath = getSharedPreferences().getString("font_family",
				"serif");

		int id = 0;

		for (Entry<String, String> entry : fonts.entrySet()) {
			fontPaths.add(entry.getKey());
			RadioButton radioButton = new RadioButton(view.getContext());
			group.addView(radioButton);
			radioButton.setText(entry.getValue());

			Typeface tface = Typeface.createFromFile(entry.getKey());
			radioButton.setTypeface(tface);
			radioButton.setId(id);
			if (entry.getKey().equals(selectedFontPath)) {
				radioButton.setChecked(true);
			}
			id++;
		}

		group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				selectedFontPath = fontPaths.get(checkedId);
			}
		});

		dialog.show();
		return true;
	}
}
