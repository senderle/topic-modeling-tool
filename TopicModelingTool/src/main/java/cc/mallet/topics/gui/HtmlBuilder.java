package cc.mallet.topics.gui;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import static cc.mallet.topics.gui.TopicModelingTool.TOPIC_WORDS;
import static cc.mallet.topics.gui.TopicModelingTool.DOCS_IN_TOPICS;
import static cc.mallet.topics.gui.TopicModelingTool.TOPICS_IN_DOCS;

import static cc.mallet.topics.gui.TopicModelingTool.MALLET_OUT;
import static cc.mallet.topics.gui.TopicModelingTool.CSV_OUT;
import static cc.mallet.topics.gui.TopicModelingTool.HTML_OUT;

public class HtmlBuilder {
    public static final String GUI_CSS = "malletgui.css";

    String CSV_DEL = ",";
    ArrayList<String> docNames = new ArrayList<String>();
    ArrayList<String> topics = new ArrayList<String>();
    int[][] Ntd;
    File input;


    public HtmlBuilder(int[][] value, File f, String csvDelim) {
        Ntd = value;
        input = f;
        CSV_DEL = csvDelim;
    }

    void createCss(File htmlDir, InputStream cssResource) throws IOException {

        OutputStream out = new FileOutputStream(new File(htmlDir, GUI_CSS));

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = cssResource.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        cssResource.close();
        out.close();
    }

    void writeHtmlHeader(BufferedWriter b, String title, String cssPath)
    throws IOException {
        String str = "<html><head><meta charset=\"utf-8\" /><head><link rel=\"stylesheet\" type=\"text/css\" href=\""+cssPath+"\" />";
        b.write(str);
        writeTitle(b, title);
        b.write("</head>");
    }


    void writeHtmlFooter(BufferedWriter b)
    throws IOException {
        String str = "</body></html>";
        b.write(str);
    }

    void writeReturnLink(BufferedWriter b)
    throws IOException {
        String str = "<br><br><br><br>"+makeUrl("../all_topics.html", "<b>[Index]</b>");
        b.write(str);
    }

    void writeTitle(BufferedWriter b, String title)
    throws IOException {
        String str = "<title>"+title+"</title>";
        b.write(str);
    }

    String makeUrl(String url, String text)
    {

        return(String.format("<a href=%s>%s</a>", url, text));
    }

    void writeFileExcerpt(File f, BufferedWriter out) throws IOException {
        String line;
        BufferedReader in = Files.newBufferedReader(f.toPath(), Charset.forName("UTF-8"));
        int PRINTCHARS = 500;
        int charsPrinted = 0;
        out.write("<textarea style=\"width: 50%; height: 150px;\">");
        while ((line = in.readLine()) != null){
            if(charsPrinted+line.length()>PRINTCHARS)
            {
                out.write(line.substring(0, PRINTCHARS - charsPrinted));
                break;
            }
            else{
                out.write(line+"\n");
                charsPrinted++;
            }
        }
        out.write("...</textarea>");
        in.close();
    }

    void writeLineExcerpt(String str, BufferedWriter out) throws IOException{

        int PRINTCHARS = 500;
        out.write("<textarea style=\"width: 50%; height: 150px;\">");
        out.write(str.substring(0, Math.min(str.length(), PRINTCHARS)));
        out.write("...</textarea>");
    }

    public void buildHtml1(File inputCsv, File outputDir)
    throws IOException {
        String FILE_NAME = "all_topics.html";
        BufferedWriter out = Files.newBufferedWriter(new File(outputDir, FILE_NAME).toPath(), Charset.forName("UTF-8"));
        BufferedReader in = Files.newBufferedReader(inputCsv.toPath(), Charset.forName("UTF-8"));
        writeHtmlHeader(out, "Topic Index", GUI_CSS);

        out.write("<body><h4>List of Topics </h4>");

        String line = "";
        String[] st = null;

        in.readLine();            //ignore header
        int n = 0;
        out.write("<table style=\" text-align: left;\" border=\"0\" cellpadding=\"2\" cellspacing=\"2\"><tbody>");
        while ((line = in.readLine()) != null)
        {
            st = line.split(CSV_DEL);
            out.write(String.format("<tr><td>%d. </td><td>%s</td></tr>", n, makeUrl("Topics/Topic"+st[0]+".html", st[1])));

            topics.add(st[1]);
            n++;
        }
        writeHtmlFooter(out);
        out.flush();

        out.close();
        in.close();
    }

    public void buildHtml2(File inputCsv, File outputDir)
    throws IOException {
        BufferedReader in = Files.newBufferedReader(
                inputCsv.toPath(), 
                Charset.forName("UTF-8"));
        in.readLine();            //ignore header

        String line = "";
        String[] row = null;

        while ((line = in.readLine()) != null)
        {
            row = line.split(CSV_DEL);
            // Values always have to be arranged pairwise -> 
            //      if mod 2 not 0 this indicates problems
            if (row.length % 2 != 0) {
                System.out.println("Error on line ");
                System.out.println(line);
            }

            String FILE_NAME = "Doc" + row[0] + ".html";
            BufferedWriter out = Files.newBufferedWriter(
                    new File(outputDir, FILE_NAME).toPath(), 
                    Charset.forName("UTF-8"));
            writeHtmlHeader(out, FILE_NAME, "../" + GUI_CSS);

            docNames.add(row[1]);

            out.write("<table style=\" text-align: left;\" border=\"0\" cellpadding=\"2\" cellspacing=\"2\"><tbody>");
            String tdocname = row[1];
           
            // Create file URI for display:
            URI furi = null;
            try {
                furi = new URI(row[1]);
            } catch (URISyntaxException exc) {
                throw new RuntimeException(exc);
            }

            File df = new File(furi);
            out.write("<body><h4><u>DOC</u> :" + df.getName() + "</h4><br>");
            writeFileExcerpt(df, out);
            
            out.write("<br><br>Top topics in this doc (% words in doc assigned to this topic) <br>");
            for (int i = 2; i < row.length - 1; i = i + 2) {
                // Record the topic percent for the given document:
                out.write(String.format("<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>(%.0f%%)</td><td>", 
                            new Float(row[i + 1]) * 100));
                // Build a link to the topic page:
                out.write(makeUrl("../Topics/Topic" + row[i] + ".html", 
                            topics.get(Integer.parseInt(row[i]))));
                out.write(" ...</td></tr>");
            }
            out.write("</tbody></table>");
            writeReturnLink(out);
            writeHtmlFooter(out);
            out.flush();
            out.close();
        }

        in.close();
    }


    public void buildHtml3(File inputCsv, File outputDir)
    throws IOException {
        BufferedReader in = Files.newBufferedReader(inputCsv.toPath(), Charset.forName("UTF-8"));
        in.readLine();            //ignore header

        String line = "";
        String[] st = null;
        String prevId = "-1";
        BufferedWriter out = null;

        while ((line = in.readLine()) != null) {
            st = line.split(CSV_DEL);

            if (!st[0].equals(prevId)) {    
                if (!prevId.equals("-1")) {    
                    out.write("</tbody></table>");
                    writeReturnLink(out);
                    writeHtmlFooter(out);
                    out.flush();
                    out.close();
                }
            
                String FILE_NAME = "Topic"+st[0]+".html";
                out = Files.newBufferedWriter(new File(outputDir, FILE_NAME).toPath(), Charset.forName("UTF-8"));
                writeHtmlHeader(out, FILE_NAME, "../" + GUI_CSS);

                out.write("<body><h4><u>TOPIC</u> : "+topics.get(Integer.parseInt(st[0]))+" ...</h4>");
                prevId = st[0];
                out.write("<br>top-ranked docs in this topic (#words in doc assigned to this topic)<br>");
                out.write("<table style=\" text-align: left;\" border=\"0\" cellpadding=\"2\" cellspacing=\"2\"><tbody>");
            }

            String tdocname = new File(st[st.length-1]).getName();
            if(tdocname.equals("null-source")){
                tdocname = "doc "+st[st.length-2];
            }

            String doc_name = makeUrl("../Docs/Doc"+st[st.length-2]+".html ", tdocname);
            try {
                out.write(
                        String.format(
                            "<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>%d.</td><td>%s</td><td>%s</td></tr>", 
                            Integer.parseInt(st[1]) + 1, 
                            "(" + Ntd[Integer.parseInt(st[0])][Integer.parseInt(st[st.length-2])] + ")", 
                            doc_name
                        )
                );
            } catch (Exception e){}
            //String temStr = String.format("        %d. %6s    ", "("+Ntd[Integer.parseInt(st[0])][Integer.parseInt(st[st.length-2])]+")");
            //out.write(temStr.replace(" ", "&nbsp;"));
            //out.write(makeUrl("../Docs/Doc"+st[st.length-2]+".html ", new File(st[st.length-1]).getName()));
            //out.write(" <br>");

        }
        out.write("</tbody></table>");
        writeReturnLink(out);
        writeHtmlFooter(out);
        out.flush();
        out.close();
        in.close();
    }

    public void createHtmlFiles(File outputDir)
    throws IOException {
        File htmlDir = new File(outputDir, HTML_OUT);
        htmlDir.mkdir();                            //FIXME case when folder already exists
        createCss(htmlDir, TopicModelingTool.class.getResourceAsStream("/css/" + GUI_CSS));

        File topicsHtmlDir = new File(htmlDir, "Topics");
        topicsHtmlDir.mkdir();
        File docsHtmlDir = new File(htmlDir, "Docs");
        docsHtmlDir.mkdir();
        File csvDir = new File(outputDir, CSV_OUT);
        buildHtml1(new File(csvDir, TOPIC_WORDS), htmlDir);
        buildHtml2(new File(csvDir, TOPICS_IN_DOCS), docsHtmlDir);
        buildHtml3(new File(csvDir, DOCS_IN_TOPICS), topicsHtmlDir);

    }



}
