package com.foreignreader;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import android.app.Activity;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		TextView tv = (TextView) findViewById(R.id.aboutText);
		String text = getResources().getString(R.string.about_text);
		tv.setText(Html.fromHtml(text));
	}

}
