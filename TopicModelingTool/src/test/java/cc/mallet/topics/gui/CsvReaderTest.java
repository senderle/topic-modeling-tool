package cc.mallet.topics.gui;

import cc.mallet.topics.gui.util.CsvReader;
import java.io.File;
import junit.framework.TestCase;

public class CsvReaderTest extends TestCase {
    public void testCsvReader() throws Exception {
        String[] args = new String[1];
        args[0] = new File("src/test/resources/data/csvtest").getAbsolutePath();
        CsvReader.main(args);
    }
}
