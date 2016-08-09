package com.csbm;

import android.net.SSLCertificateSocketFactory;
import android.net.SSLSessionCache;

import com.csbm.http.BEHttpBody;
import com.csbm.http.BEHttpRequest;
import com.csbm.http.BEHttpResponse;
import com.csbm.http.BENetworkInterceptor;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import bolts.Capture;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/** package */ class BEOkHttpClient extends BEHttpClient<Request, Response> {

  private final static String OKHTTP_GET = "GET";
  private final static String OKHTTP_POST = "POST";
  private final static String OKHTTP_PUT = "PUT";
  private final static String OKHTTP_DELETE = "DELETE";

  private OkHttpClient okHttpClient;

  public BEOkHttpClient(int socketOperationTimeout, SSLSessionCache sslSessionCache) {

    okHttpClient = new OkHttpClient();

    okHttpClient.setConnectTimeout(socketOperationTimeout, TimeUnit.MILLISECONDS);
    okHttpClient.setReadTimeout(socketOperationTimeout, TimeUnit.MILLISECONDS);

    // Don't handle redirects. We copy the setting from AndroidHttpClient.
    // For detail, check https://quip.com/Px8jAxnaun2r
    okHttpClient.setFollowRedirects(false);

    okHttpClient.setSslSocketFactory(SSLCertificateSocketFactory.getDefault(
        socketOperationTimeout, sslSessionCache));
  }

  @Override
  /* package */ BEHttpResponse executeInternal(BEHttpRequest beRequest) throws IOException {
    Request okHttpRequest = getRequest(beRequest);
    Call okHttpCall = okHttpClient.newCall(okHttpRequest);

    Response okHttpResponse = okHttpCall.execute();

    return getResponse(okHttpResponse);
  }

  @Override
  /* package */ BEHttpResponse getResponse(Response okHttpResponse)
      throws IOException {
    // Status code
    int statusCode = okHttpResponse.code();

    // Content
    InputStream content = okHttpResponse.body().byteStream();

    // Total size
    int totalSize = (int)okHttpResponse.body().contentLength();

    // Reason phrase
    String reasonPhrase = okHttpResponse.message();

    // Headers
    Map<String, String> headers = new HashMap<>();
    for (String name : okHttpResponse.headers().names()) {
      headers.put(name, okHttpResponse.header(name));
    }

    // Content type
    String contentType = null;
    ResponseBody body = okHttpResponse.body();
    if (body != null && body.contentType() != null) {
      contentType = body.contentType().toString();
    }

    return new BEHttpResponse.Builder()
        .setStatusCode(statusCode)
        .setContent(content)
        .setTotalSize(totalSize)
        .setReasonPhrase(reasonPhrase)
        .setHeaders(headers)
        .setContentType(contentType)
        .build();
  }

  @Override
  /* package */ Request getRequest(BEHttpRequest beRequest) throws IOException {
    Request.Builder okHttpRequestBuilder = new Request.Builder();
    BEHttpRequest.Method method = beRequest.getMethod();
    // Set method
    switch (method) {
      case GET:
        okHttpRequestBuilder.get();
        break;
      case DELETE:
        okHttpRequestBuilder.delete();
        break;
      case POST:
      case PUT:
        // Since we need to set body and method at the same time for POST and PUT, we will do it in
        // the following.
        break;
      default:
        // This case will never be reached since we have already handled this case in
        // BERequest.newRequest().
        throw new IllegalStateException("Unsupported http method " + method.toString());
    }
    // Set url
    okHttpRequestBuilder.url(beRequest.getUrl());

    // Set Header
    Headers.Builder okHttpHeadersBuilder = new Headers.Builder();
    for (Map.Entry<String, String> entry : beRequest.getAllHeaders().entrySet()) {
      okHttpHeadersBuilder.add(entry.getKey(), entry.getValue());
    }
    // OkHttp automatically add gzip header so we do not need to deal with it
    Headers okHttpHeaders = okHttpHeadersBuilder.build();
    okHttpRequestBuilder.headers(okHttpHeaders);

    // Set Body
    BEHttpBody beBody = beRequest.getBody();
    BEOkHttpRequestBody okHttpRequestBody = null;
    if(beBody instanceof BEByteArrayHttpBody) {
      okHttpRequestBody = new BEOkHttpRequestBody(beBody);
    }
    switch (method) {
      case PUT:
        okHttpRequestBuilder.put(okHttpRequestBody);
        break;
      case POST:
        okHttpRequestBuilder.post(okHttpRequestBody);
        break;
    }
    return okHttpRequestBuilder.build();
  }

  private BEHttpRequest getBEHttpRequest(Request okHttpRequest) {
    BEHttpRequest.Builder beRequestBuilder = new BEHttpRequest.Builder();
    // Set method
    switch (okHttpRequest.method()) {
       case OKHTTP_GET:
           beRequestBuilder.setMethod(BEHttpRequest.Method.GET);
           break;
       case OKHTTP_DELETE:
           beRequestBuilder.setMethod(BEHttpRequest.Method.DELETE);
           break;
       case OKHTTP_POST:
           beRequestBuilder.setMethod(BEHttpRequest.Method.POST);
           break;
       case OKHTTP_PUT:
           beRequestBuilder.setMethod(BEHttpRequest.Method.PUT);
           break;
       default:
           // This should never happen
           throw new IllegalArgumentException(
               "Invalid http method " + okHttpRequest.method());
     }

    // Set url
    beRequestBuilder.setUrl(okHttpRequest.urlString());

    // Set Header
    for (Map.Entry<String, List<String>> entry : okHttpRequest.headers().toMultimap().entrySet()) {
      beRequestBuilder.addHeader(entry.getKey(), entry.getValue().get(0));
    }

    // Set Body
    BEOkHttpRequestBody okHttpBody = (BEOkHttpRequestBody) okHttpRequest.body();
    if (okHttpBody != null) {
      beRequestBuilder.setBody(okHttpBody.getBEHttpBody());
    }
    return beRequestBuilder.build();
  }

  /**
   * For OKHttpClient, since it does not expose any interface for us to check the raw response
   * stream, we have to use OKHttp networkInterceptors. Instead of using our own interceptor list,
   * we use OKHttp inner interceptor list.
   * @param beNetworkInterceptor
   */
  @Override
  /* package */ void addExternalInterceptor(final BENetworkInterceptor beNetworkInterceptor) {
    okHttpClient.networkInterceptors().add(new Interceptor() {
      @Override
      public Response intercept(final Chain okHttpChain) throws IOException {
        Request okHttpRequest = okHttpChain.request();
        // Transfer OkHttpRequest to BEHttpRequest
        final BEHttpRequest beRequest = getBEHttpRequest(okHttpRequest);
        // Capture OkHttpResponse
        final Capture<Response> okHttpResponseCapture = new Capture<>();
        final BEHttpResponse beResponse =
            beNetworkInterceptor.intercept(new BENetworkInterceptor.Chain() {
          @Override
          public BEHttpRequest getRequest() {
            return beRequest;
          }

          @Override
          public BEHttpResponse proceed(BEHttpRequest beRequest) throws IOException {
            // Use OKHttpClient to send request
            Request okHttpRequest = BEOkHttpClient.this.getRequest(beRequest);
            Response okHttpResponse = okHttpChain.proceed(okHttpRequest);
            okHttpResponseCapture.set(okHttpResponse);
            return getResponse(okHttpResponse);
          }
        });
        final Response okHttpResponse = okHttpResponseCapture.get();
        // Ideally we should build newOkHttpResponse only based on beResponse, however
        // BEHttpResponse does not have all the info we need to build the newOkHttpResponse, so
        // we rely on the okHttpResponse to generate the builder and change the necessary info
        // inside
        Response.Builder newOkHttpResponseBuilder =  okHttpResponse.newBuilder();
        // Set status
        newOkHttpResponseBuilder
            .code(beResponse.getStatusCode())
            .message(beResponse.getReasonPhrase());
        // Set headers
        if (beResponse.getAllHeaders() != null) {
          for (Map.Entry<String, String> entry : beResponse.getAllHeaders().entrySet()) {
            newOkHttpResponseBuilder.header(entry.getKey(), entry.getValue());
          }
        }
        // Set body
        newOkHttpResponseBuilder.body(new ResponseBody() {
          @Override
          public MediaType contentType() {
            if (beResponse.getContentType() == null) {
              return null;
            }
            return MediaType.parse(beResponse.getContentType());
          }

          @Override
          public long contentLength() throws IOException {
            return beResponse.getTotalSize();
          }

          @Override
          public BufferedSource source() throws IOException {
            // We need to use the proxy stream from interceptor to replace the origin network
            // stream, so when the stream is read by CSBM, the network stream is proxyed in the
            // interceptor.
            if (beResponse.getContent() == null) {
              return null;
            }
            return Okio.buffer(Okio.source(beResponse.getContent()));
          }
        });

        return newOkHttpResponseBuilder.build();
      }
    });
  }

  private static class BEOkHttpRequestBody extends RequestBody {

    private BEHttpBody beBody;

    public BEOkHttpRequestBody(BEHttpBody beBody) {
      this.beBody = beBody;
    }

    @Override
    public long contentLength() throws IOException {
      return beBody.getContentLength();
    }

    @Override
    public MediaType contentType() {
      String contentType = beBody.getContentType();
      return contentType == null ? null : MediaType.parse(beBody.getContentType());
    }

    @Override
    public void writeTo(BufferedSink bufferedSink) throws IOException {
      beBody.writeTo(bufferedSink.outputStream());
    }

    public BEHttpBody getBEHttpBody() {
      return beBody;
    }
  }
}
