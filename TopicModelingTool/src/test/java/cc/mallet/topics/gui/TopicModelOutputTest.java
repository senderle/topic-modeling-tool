package cc.mallet.topics.gui;

import cc.mallet.topics.gui.TopicModelingTool;
import java.io.File;
import junit.framework.TestCase;

public class TopicModelOutputTest extends TestCase {
    public void testTopicModelOutput() throws Exception {
        String[] args = new String[3];
        args[0] = new File("src/test/resources/data/subset").getAbsolutePath();
        args[1] = new File("target/test-outputs").getAbsolutePath();
        args[2] = new File(
                "src/test/resources/data/dos-bulletin-1953-1954-metadata.csv"
                ).getAbsolutePath();
        TopicModelingTool.main(args, true); 
    }
}
