package com.foreignreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.foreignreader.util.FontManager;
import com.foreignreader.R;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;

public class ViewSettings extends Activity {

	public static final String VIEW_PREF = "VIEW_PREF";

	private List<String> fontPaths;

	private String selectedFontPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_settings);

		RadioGroup group = (RadioGroup) findViewById(R.id.fontChooserGroup);
		SharedPreferences preferences = getSharedPreferences(VIEW_PREF, 0);

		HashMap<String, String> fonts = FontManager.enumerateFonts();
		fontPaths = new ArrayList<String>();

		selectedFontPath = preferences.getString("font_family", "serif");

		int id=0;
		
		for (Entry<String, String> entry : fonts.entrySet()) {
			fontPaths.add(entry.getKey());
			RadioButton radioButton = new RadioButton(this);
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

		SeekBar sb = (SeekBar) findViewById(R.id.fontSizeBar);
		sb.setProgress(preferences.getInt("font_size", 30)-10);

		CheckBox splitPages = (CheckBox) findViewById(R.id.splitOnLandscapeCheckBox);
		splitPages.setChecked(preferences
				.getBoolean("split_on_landscape", true));
	}

	@Override
	protected void onStop() {
		super.onStop();

		SharedPreferences preferences = getSharedPreferences(VIEW_PREF, 0);
		SharedPreferences.Editor editor = preferences.edit();

		editor.putString("font_family", selectedFontPath);
		editor.putInt("font_size",
				((SeekBar) findViewById(R.id.fontSizeBar)).getProgress()+10);
		editor.putBoolean("split_on_landscape",
				((CheckBox) findViewById(R.id.splitOnLandscapeCheckBox))
						.isChecked());

		editor.commit();
	}
}
