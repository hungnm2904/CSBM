package com.csbm;

import android.net.SSLSessionCache;
import android.os.Build;

import com.csbm.http.BEHttpRequest;
import com.csbm.http.BEHttpResponse;
import com.csbm.http.BENetworkInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The base class of a httpclient. It takes an http request, sends it to the server
 * and gets response. It can be implemented by different http library such as Apache http,
 * Android URLConnection, Square OKHttp and so on.
 */

/** package */ abstract class BEHttpClient<LibraryRequest, LibraryResponse> {
  private static final String TAG = "com.csbm.BEHttpClient";

  private static final String APACHE_HTTPCLIENT_NAME = "org.apache.http";
  private static final String URLCONNECTION_NAME = "net.java.URLConnection";
  private static final String OKHTTP_NAME = "com.squareup.okhttp";

  private static final String OKHTTPCLIENT_PATH = "com.squareup.okhttp.OkHttpClient";

  private static final String MAX_CONNECTIONS_PROPERTY_NAME = "http.maxConnections";
  private static final String KEEP_ALIVE_PROPERTY_NAME = "http.keepAlive";

  public static BEHttpClient createClient(int socketOperationTimeout,
                                             SSLSessionCache sslSessionCache) {
    String httpClientLibraryName;
    BEHttpClient httpClient;
    if (hasOkHttpOnClasspath()) {
      httpClientLibraryName = OKHTTP_NAME;
      httpClient =  new BEOkHttpClient(socketOperationTimeout, sslSessionCache);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      httpClientLibraryName = URLCONNECTION_NAME;
      httpClient =  new BEURLConnectionHttpClient(socketOperationTimeout, sslSessionCache);
    } else {
      httpClientLibraryName = APACHE_HTTPCLIENT_NAME;
      httpClient =  new BEApacheHttpClient(socketOperationTimeout, sslSessionCache);
    }
    PLog.i(TAG, "Using " + httpClientLibraryName + " library for networking communication.");
    return httpClient;
  }

  public static void setMaxConnections(int maxConnections) {
    if (maxConnections <= 0) {
      throw new IllegalArgumentException("Max connections should be large than 0");
    }
    System.setProperty(MAX_CONNECTIONS_PROPERTY_NAME, String.valueOf(maxConnections));
  }

  public static void setKeepAlive(boolean isKeepAlive) {
    System.setProperty(KEEP_ALIVE_PROPERTY_NAME, String.valueOf(isKeepAlive));
  }

  private static boolean hasOkHttpOnClasspath() {
    try {
      Class.forName(OKHTTPCLIENT_PATH);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private boolean hasExecuted;

  // There is no need to keep locks for interceptor lists since they will only be changed before
  // we make network request
  private List<BENetworkInterceptor> internalInterceptors;
  private List<BENetworkInterceptor> externalInterceptors;

  /* package */ abstract BEHttpResponse executeInternal(BEHttpRequest request)
      throws IOException;

  /* package */ abstract LibraryRequest getRequest(BEHttpRequest beRequest)
      throws IOException;

  /* package */ abstract BEHttpResponse getResponse(LibraryResponse okHttpResponse)
      throws IOException;

  /* package */ void addInternalInterceptor(BENetworkInterceptor interceptor) {
    // If we do not have the restriction, we may have read/write conflict on the interceptorList
    // and need to add lock to protect it. If in the future we need to add interceptor after
    // httpclient start to execute, it is safe to remove this check and add lock.
    if (hasExecuted) {
      throw new IllegalStateException(
          "`BEHttpClient#addInternalInterceptor(BENetworkInterceptor)` can only be invoked " +
              "before `BEHttpClient` execute any request");
    }

    if (internalInterceptors == null) {
      internalInterceptors = new ArrayList<>();
    }
    internalInterceptors.add(interceptor);
  }

   /* package */ void addExternalInterceptor(BENetworkInterceptor interceptor) {
    // No need to check hasExecuted since this method will only be called before CSBM.initialize()
    if (externalInterceptors == null) {
      externalInterceptors = new ArrayList<>();
    }
    externalInterceptors.add(interceptor);
  }

  public final BEHttpResponse execute(BEHttpRequest request) throws IOException {
    if (!hasExecuted) {
      hasExecuted = true;
    }
    BENetworkInterceptor.Chain chain = new BENetworkInterceptorChain(0, 0, request);
    return chain.proceed(request);
  }

  private class BENetworkInterceptorChain implements BENetworkInterceptor.Chain {
    private final int internalIndex;
    private final int externalIndex;
    private final BEHttpRequest request;

    BENetworkInterceptorChain(int internalIndex, int externalIndex, BEHttpRequest request) {
      this.internalIndex = internalIndex;
      this.externalIndex = externalIndex;
      this.request = request;
    }

    @Override
    public BEHttpRequest getRequest() {
      return request;
    }

    @Override
    public BEHttpResponse proceed(BEHttpRequest request) throws IOException {
      if (internalInterceptors != null && internalIndex < internalInterceptors.size()) {
        // There's another internal interceptor in the chain. Call that.
        BENetworkInterceptor.Chain chain =
            new BENetworkInterceptorChain(internalIndex + 1, externalIndex, request);
        return internalInterceptors.get(internalIndex).intercept(chain);
      }

      if (externalInterceptors != null && externalIndex < externalInterceptors.size()) {
        // There's another external interceptor in the chain. Call that.
        BENetworkInterceptor.Chain chain =
            new BENetworkInterceptorChain(internalIndex, externalIndex + 1, request);
        return externalInterceptors.get(externalIndex).intercept(chain);
      }

      // No more interceptors. Do HTTP.
      return executeInternal(request);
    }
  }

  /**
   * When we find developers use interceptors, since we need expose the raw
   * response(ungziped response) to interceptors, we need to disable the transparent ungzip.
   *
   * @return {@code true} if we should disable the http library level auto decompress.
   */
  /* package */ boolean disableHttpLibraryAutoDecompress() {
    return externalInterceptors != null && externalInterceptors.size() > 0;
  }
}
