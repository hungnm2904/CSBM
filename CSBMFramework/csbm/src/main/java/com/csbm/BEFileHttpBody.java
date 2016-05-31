/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

import com.csbm.http.BEHttpBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** package */ class BEFileHttpBody extends BEHttpBody {

  /* package */ final File file;

  public BEFileHttpBody(File file) {
    this(file, null);
  }

  public BEFileHttpBody(File file, String contentType) {
    super(contentType, file.length());
    this.file = file;
  }

  @Override
  public InputStream getContent() throws IOException {
    return new FileInputStream(file);
  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    if (out == null) {
      throw new IllegalArgumentException("Output stream can not be null");
    }

    final FileInputStream fileInput = new FileInputStream(file);
    try {
      BEIOUtils.copy(fileInput, out);
    } finally {
      BEIOUtils.closeQuietly(fileInput);
    }
  }
}
