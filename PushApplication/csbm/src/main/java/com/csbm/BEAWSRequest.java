package com.csbm;

import com.csbm.http.BEHttpRequest;
import com.csbm.http.BEHttpResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;

import bolts.Task;

/**
 * Request returns a byte array of the response and provides a callback the progress of the data
 * read from the network.
 */

/** package */ class BEAWSRequest extends BERequest<Void> {

  // The temp file is used to save the BEFile content when we fetch it from server
  private final File tempFile;

  public BEAWSRequest(BEHttpRequest.Method method, String url, File tempFile) {
    super(method, url);
    this.tempFile = tempFile;
  }

  @Override
  protected Task<Void> onResponseAsync(final BEHttpResponse response,
                                       final ProgressCallback downloadProgressCallback) {
    int statusCode = response.getStatusCode();
    if (statusCode >= 200 && statusCode < 300 || statusCode == 304) {
      // OK
    } else {
      String action = method == BEHttpRequest.Method.GET ? "Download from" : "Upload to";
      return Task.forError(new BEException(BEException.CONNECTION_FAILED, String.format(
        "%s S3 failed. %s", action, response.getReasonPhrase())));
    }

    if (method != BEHttpRequest.Method.GET) {
      return null;
    }

    return Task.call(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        long totalSize = response.getTotalSize();
        long downloadedSize = 0;
        InputStream responseStream = null;
        try {
          responseStream = response.getContent();
          FileOutputStream tempFileStream = BEFileUtils.openOutputStream(tempFile);

          int nRead;
          byte[] data = new byte[32 << 10]; // 32KB

          while ((nRead = responseStream.read(data, 0, data.length)) != -1) {
            tempFileStream.write(data, 0, nRead);
            downloadedSize += nRead;
            if (downloadProgressCallback != null && totalSize != -1) {
              int progressToReport =
                  Math.round((float) downloadedSize / (float) totalSize * 100.0f);
              downloadProgressCallback.done(progressToReport);
            }
          }
          return null;
        } finally {
          BEIOUtils.closeQuietly(responseStream);
        }
      }
    }, BEExecutors.io());
  }
}
