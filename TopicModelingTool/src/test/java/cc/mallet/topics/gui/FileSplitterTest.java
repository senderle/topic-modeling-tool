package cc.mallet.topics.gui;

import cc.mallet.topics.gui.util.FileSplitter;
import java.io.File;
import junit.framework.TestCase;

public class FileSplitterTest extends TestCase {
    public void testFileSplitter() throws Exception {
        String[] args = new String[2];
        args[0] = "5";
        args[1] = new File("src/test/resources/data/words").getAbsolutePath();
        FileSplitter.main(args);
    }
}
