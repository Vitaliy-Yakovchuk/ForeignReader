package com.reader.common.book;

import java.util.List;

public interface Book {

	public List<String> getSections() throws Exception;

	public Section getSection(int i) throws Exception;

}
