package cc.mallet.topics.gui;

import java.io.*;
import java.util.zip.*;

public class GunZipper {
  private InputStream in;

  public GunZipper(File f) throws IOException {
    this.in = new FileInputStream(f);
  }  
  public void unzip(File fileTo) throws IOException {
    OutputStream out = new FileOutputStream(fileTo);
    try {
      in = new GZIPInputStream(in);
      byte[] buffer = new byte[65536];
      int noRead;
      while ((noRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, noRead);
      }
    } finally {
      try { out.close(); } catch (Exception e) {}
    }
    close();
  }
  public void close() {
    try { in.close(); } catch (Exception e) {}
  }
}

