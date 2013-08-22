package com.foriegnreader;

import java.util.List;

import com.reader.common.impl.SimpleTextParser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;

public class TranslationHelper {

	public static final String SEARCH_ACTION = "colordict.intent.action.SEARCH";
	public static final String EXTRA_QUERY = "EXTRA_QUERY";
	public static final String EXTRA_FULLSCREEN = "EXTRA_FULLSCREEN";
	public static final String EXTRA_HEIGHT = "EXTRA_HEIGHT";
	public static final String EXTRA_WIDTH = "EXTRA_WIDTH";
	public static final String EXTRA_GRAVITY = "EXTRA_GRAVITY";
	public static final String EXTRA_MARGIN_LEFT = "EXTRA_MARGIN_LEFT";
	public static final String EXTRA_MARGIN_TOP = "EXTRA_MARGIN_TOP";
	public static final String EXTRA_MARGIN_BOTTOM = "EXTRA_MARGIN_BOTTOM";
	public static final String EXTRA_MARGIN_RIGHT = "EXTRA_MARGIN_RIGHT";

	public static String normilize(String t) {
		final StringBuffer text = new StringBuffer();

		SimpleTextParser parser = new SimpleTextParser() {

			boolean first = true;

			@Override
			public void processWord(char[] textCharArray, int start, int len) {
				if (first)
					first = false;
				else
					text.append(' ');

				text.append(textCharArray, start, len);
			}
		};
		parser.parse(t.toCharArray());
		return text.toString();
	}

	public static boolean translate(Context context, TextOnScreen selectedText) {

		Intent intent = new Intent(SEARCH_ACTION);
		intent.putExtra(EXTRA_QUERY, normilize(selectedText.text));
		intent.putExtra(EXTRA_FULLSCREEN, false);

		if (isIntentAvailable(context, intent)) {

			WindowManager wm = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			int height = size.y;

			if (selectedText.y > height / 2) {
				intent.putExtra(EXTRA_HEIGHT,
						(int) (selectedText.y - (int) (height * 0.05)));
				intent.putExtra(EXTRA_MARGIN_TOP, (int) (height * 0.01));
				intent.putExtra(EXTRA_GRAVITY, Gravity.TOP);
			} else {
				intent.putExtra(EXTRA_GRAVITY, Gravity.BOTTOM);
				intent.putExtra(EXTRA_HEIGHT,
						(int) (height - selectedText.y - (int) (height * 0.05)));
				intent.putExtra(EXTRA_MARGIN_TOP,
						(int) (selectedText.y + (int) (height * 0.04)));
			}

			context.startActivity(intent);
			return true;
		}

		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle("Dictionary information");
		alertDialog
				.setMessage("To enable this feature please install ColorDict/BlueDict/GoldenDict/etc.");
		alertDialog.setIcon(R.drawable.icon);
		alertDialog.show();

		return false;
	}

	public static boolean isIntentAvailable(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
}