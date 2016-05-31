package com.csbm;

import com.csbm.http.BEHttpBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/** package */ class BEByteArrayHttpBody extends BEHttpBody {
  /* package */ final byte[] content;
  /* package */ final InputStream contentInputStream;

  public BEByteArrayHttpBody(String content, String contentType)
      throws UnsupportedEncodingException {
    this(content.getBytes("UTF-8"), contentType);
  }

  public BEByteArrayHttpBody(byte[] content, String contentType) {
    super(contentType, content.length);
    this.content = content;
    this.contentInputStream = new ByteArrayInputStream(content);
  }

  @Override
  public InputStream getContent() {
    return contentInputStream;
  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    if (out == null) {
      throw new IllegalArgumentException("Output stream may not be null");
    }

    out.write(content);
  }
}
