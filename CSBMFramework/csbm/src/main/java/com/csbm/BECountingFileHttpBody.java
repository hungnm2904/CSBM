package com.csbm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/** package */ class BECountingFileHttpBody extends BEFileHttpBody {

  private static final int DEFAULT_CHUNK_SIZE = 4096;
  private static final int EOF = -1;

  private final ProgressCallback progressCallback;

  public BECountingFileHttpBody(File file, ProgressCallback progressCallback) {
    this(file, null, progressCallback);
  }

  public BECountingFileHttpBody(
          File file, String contentType, ProgressCallback progressCallback) {
    super(file, contentType);
    this.progressCallback = progressCallback;
  }

  @Override
  public void writeTo(OutputStream output) throws IOException {
    if (output == null) {
      throw new IllegalArgumentException("Output stream may not be null");
    }

    final FileInputStream fileInput = new FileInputStream(file);;
    try {
      byte[] buffer = new byte[DEFAULT_CHUNK_SIZE];
      int n;
      long totalLength = file.length();
      long position = 0;
      while (EOF != (n = fileInput.read(buffer))) {
        output.write(buffer, 0, n);
        position += n;

        if (progressCallback != null) {
          int progress = (int) (100 * position / totalLength);
          progressCallback.done(progress);
        }
      }
    } finally {
      BEIOUtils.closeQuietly(fileInput);
    }
  }
}
