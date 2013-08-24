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
