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
package com.foreignreader.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import android.os.AsyncTask;

/**
 * 
 * 
 * 
 * @author Vitaliy Yakovchuk
 * 
 */

public class WordNetExtractor extends AsyncTask<InputStream, Void, Void> {

	@Override
	protected Void doInBackground(InputStream... streams) {
		assert streams.length == 1;

		try {

			BufferedInputStream in = new BufferedInputStream(streams[0]);
			GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);

			TarArchiveInputStream tar = new TarArchiveInputStream(gzIn);

			TarArchiveEntry tarEntry;

			File dest = LongTranslationHelper.getWordNetDict().getParentFile();

			while ((tarEntry = tar.getNextTarEntry()) != null) {
				File destPath = new File(dest, tarEntry.getName());
				if (tarEntry.isDirectory()) {
					destPath.mkdirs();
				} else {
					destPath.createNewFile();
					byte[] btoRead = new byte[1024 * 10];

					BufferedOutputStream bout = new BufferedOutputStream(
							new FileOutputStream(destPath));
					int len = 0;

					while ((len = tar.read(btoRead)) != -1) {
						bout.write(btoRead, 0, len);
					}

					bout.close();

				}
			}

			tar.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
