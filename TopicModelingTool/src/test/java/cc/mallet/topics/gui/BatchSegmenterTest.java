package cc.mallet.topics.gui;

import cc.mallet.topics.gui.util.BatchSegmenter;
import java.io.File;
import junit.framework.TestCase;

public class BatchSegmenterTest extends TestCase {
    public void testBatchSegmenter() throws Exception {
        String[] args = new String[2];
        args[0] = new File("src/test/resources/data/").getAbsolutePath();
        args[1] = new File("src/test/resources/data/batchtest").getAbsolutePath();
        BatchSegmenter.main(args);
    }
}
