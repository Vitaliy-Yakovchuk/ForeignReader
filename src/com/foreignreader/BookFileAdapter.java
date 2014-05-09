/*******************************************************************************
 * Copyright 2013 Vitaliy Yakovchuk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.foreignreader;

import java.util.List;

import com.reader.common.BookMetadata;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BookFileAdapter extends ArrayAdapter<BookMetadata> {

	private List<BookMetadata> values;
	private Context context;

	public BookFileAdapter(Context context, int resource,
			List<BookMetadata> values) {
		super(context, resource, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.book_item, parent, false);
		TextView textView = (TextView) rowView
				.findViewById(R.id.bookFileTextView);
		textView.setText(values.get(position).toString());
		rowView.findViewById(R.id.removeBookButton)
				.setTag(values.get(position));
		rowView.findViewById(R.id.openBookButton).setTag(values.get(position));
		rowView.findViewById(R.id.scanBookButton).setTag(values.get(position));
		return rowView;
	}
}
