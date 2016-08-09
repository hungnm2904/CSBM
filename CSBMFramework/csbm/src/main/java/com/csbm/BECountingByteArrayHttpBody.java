package com.csbm;

import java.io.IOException;
import java.io.OutputStream;

import static java.lang.Math.min;

/** package */ class BECountingByteArrayHttpBody extends BEByteArrayHttpBody {
  private static final int DEFAULT_CHUNK_SIZE = 4096;
  private final ProgressCallback progressCallback;

  public BECountingByteArrayHttpBody(byte[] content, String contentType,
                                     final ProgressCallback progressCallback) {
    super(content, contentType);
    this.progressCallback = progressCallback;
  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    if (out == null) {
      throw new IllegalArgumentException("Output stream may not be null");
    }

    int position = 0;
    int totalLength = content.length;
    while (position < totalLength) {
      int length = min(totalLength - position, DEFAULT_CHUNK_SIZE);

      out.write(content, position, length);
      out.flush();

      if (progressCallback != null) {
        position += length;

        int progress = 100 * position / totalLength;
        progressCallback.done(progress);
      }
    }
  }
}
