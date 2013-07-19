package cc.mallet.topics.gui;

import java.io.File;

import junit.framework.TestCase;

public class FilePathFormatTest extends TestCase {

	public void testFilePathAsURI() throws Exception {
		File f = File.createTempFile("uri test with blanks in%20path%20", ".txt");
		File f2 = new File(f.toURI());
		assertEquals(true, f2.exists());
		// passing URI as String results in not detecting the file as existing
		File f3 = new File(f.toURI().toString());
		assertEquals(false, f3.exists());
		f.deleteOnExit();
	}

}
