package com.foreignreader.pref;

import com.foreignreader.R;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

public class FontSizePreference extends DialogPreference {

	private static final int DEFAULT_VALUE = 30;

	private int sSize = 30;

	private SeekBar bar;

	public FontSizePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.dialog_font_preference);

		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);

		setDialogIcon(null);
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		bar = (SeekBar) view.findViewById(R.id.font_size_bar);
		bar.setProgress(sSize - 10);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		if (restorePersistedValue) {
			// Restore existing state
			sSize = this.getPersistedInt(DEFAULT_VALUE);
		} else {
			// Set default state from the XML attribute
			sSize = (Integer) defaultValue;
			persistInt(sSize);
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// When the user selects "OK", persist the new value
		if (positiveResult) {
			sSize = bar.getProgress() + 10;
			persistInt(sSize);
		}
	}

}
