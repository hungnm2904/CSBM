/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

/**
 * REST network command for creating & uploading {@link BEFile}s.
 */


import com.csbm.http.BEHttpBody;
import com.csbm.http.BEHttpRequest;

import java.io.File;

/** package */ class BERESTFileCommand extends BERESTCommand {

  public static class Builder extends Init<Builder> {

    private byte[] data = null;
    private String contentType = null;
    private File file;

    public Builder() {
      // We only ever use BERESTFileCommand for file uploads, so default to POST.
      method(BEHttpRequest.Method.POST);
    }

    public Builder fileName(String fileName) {
      return httpPath(String.format("files/%s", fileName));
    }

    public Builder data(byte[] data) {
      this.data = data;
      return this;
    }

    public Builder contentType(String contentType) {
      this.contentType = contentType;
      return this;
    }

    public Builder file(File file) {
      this.file = file;
      return this;
    }

    @Override
    /* package */ Builder self() {
      return this;
    }

    public BERESTFileCommand build() {
      return new BERESTFileCommand(this);
    }
  }

  private final byte[] data;
  private final String contentType;
  private final File file;

  public BERESTFileCommand(Builder builder) {
    super(builder);
    if (builder.file != null && builder.data != null) {
      throw new IllegalArgumentException("File and data can not be set at the same time");
    }
    this.data = builder.data;
    this.contentType = builder.contentType;
    this.file = builder.file;
  }

  @Override
  protected BEHttpBody newBody(final ProgressCallback progressCallback) {
    // TODO(mengyan): Delete BEByteArrayHttpBody when we change input byte array to staged file
    // in BEFileController
    if (progressCallback == null) {
      return data != null ?
          new BEByteArrayHttpBody(data, contentType) : new BEFileHttpBody(file, contentType);
    }
    return data != null ?
        new BECountingByteArrayHttpBody(data, contentType, progressCallback) :
        new BECountingFileHttpBody(file, contentType, progressCallback);
  }
}
