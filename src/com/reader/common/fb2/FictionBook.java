package com.reader.common.fb2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.reader.common.book.AbstractBook;
import com.reader.common.book.Section;

public class FictionBook extends AbstractBook {

	private File file;

	private XmlPullParser parser;

	private static final String ns = null;

	public FictionBook(File file) {
		this.file = file;
	}

	@Override
	public List<String> getSections() throws Exception {
		FileInputStream in = new FileInputStream(file);
		List<String> res = new ArrayList<String>();
		try {
			open(in);
			readSections(res);
			parser = null;
		} catch (Exception exception) {
			in.close();
			throw exception;
		}
		return res;
	}

	private void readSections(List<String> res) throws Exception {
		parser.require(XmlPullParser.START_TAG, ns, "FictionBook");
		String name = "";
		boolean inSection = false;
		boolean inBody = false;
		while (true) {
			int next = parser.next();
			if (next == XmlPullParser.END_TAG) {
				name = parser.getName();
				if ("FictionBook".equals(parser.getName()))
					return;
				if ("section".equals(parser.getName()))
					inSection = false;
				if ("body".equals(parser.getName()))
					inBody = false;
				continue;
			} else if (next == XmlPullParser.START_TAG) {
				name = parser.getName();
				if ("body".equals(name))
					inBody = true;
				else if (inBody && "section".equals(name))
					inSection = true;
				else if (inSection && "title".equals(name)) {
					String title = readTitle(parser);
					if (title != null && title.length() > 0)
						res.add(title);
				}

			}
		}
	}

	private String readTitle(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String title = "";
		while (true) {
			int next = parser.next();
			if (next == XmlPullParser.END_TAG) {
				String name = parser.getName();
				if ("title".equals(name))
					return title;
			} else if (next == XmlPullParser.TEXT) {
				if (title.length() > 0)
					title += " " + parser.getText();
				else
					title += parser.getText();
			}
		}
	}

	@Override
	public Section getSection(int i) throws Exception {
		FileInputStream in = new FileInputStream(file);
		Section section = new Section();
		try {
			open(in);
			readSection(section, i);
			parser = null;
		} catch (Exception exception) {
			in.close();
			throw exception;
		}
		return section;
	}

	private void readSection(Section section, int i) throws Exception {
		parser.require(XmlPullParser.START_TAG, ns, "FictionBook");
		String name = "";
		boolean inSection = false;
		boolean inBody = false;
		int sectionIndex = 0;
		while (true) {
			int next = parser.next();
			if (next == XmlPullParser.END_TAG) {
				name = parser.getName();
				if ("FictionBook".equals(parser.getName()))
					return;
				if ("section".equals(parser.getName()))
					if (inSection)
						return;
				if ("body".equals(parser.getName()))
					inBody = false;
				continue;
			} else if (next == XmlPullParser.START_TAG) {
				name = parser.getName();
				if ("body".equals(name))
					inBody = true;
				else if (inBody && "section".equals(name)) {
					if (i == sectionIndex)
						inSection = true;
					sectionIndex++;
				} else if (inSection && "title".equals(name)) {
					String title = readTitle(parser);
					if (title != null)
						section.getParagraphs().add(title);
				} else if (inSection && "p".equals(name)) {
					String p = readP(parser);
					if (p != null)
						section.getParagraphs().add(p);
				}

			}
		}
	}

	private String readP(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		StringBuilder p = new StringBuilder();
		int deep = 1;
		while (true) {
			int next = parser.next();
			if (next == XmlPullParser.END_DOCUMENT) {
				return null;
			}
			if (next == XmlPullParser.END_TAG) {
				String name = parser.getName();
				if ("p".equals(name)) {
					deep--;
					if (deep == 0)
						return p.toString();
				}
			} else if (next == XmlPullParser.START_TAG) {
				String name = parser.getName();
				if ("p".equals(name))
					deep++;
			} else if (next == XmlPullParser.TEXT) {
				p.append(parser.getText());
			}
		}
	}

	private void open(InputStream in) throws Exception {
		parser = Xml.newPullParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(in, null);
		parser.nextTag();
	}

}
