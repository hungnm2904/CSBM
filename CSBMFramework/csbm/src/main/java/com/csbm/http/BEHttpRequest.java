package com.csbm.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by akela on 29/05/2016.
 * The http request we send to csbm server. Instances of this class are not immutable. The
 * request body may be consumed only once. The other fields are immutable.
 */
public final class BEHttpRequest {

    /**
     * The {@code BEHttpRequest} method type.
     */
    public enum Method {

        GET, POST, PUT, DELETE;

        /**
         * Creates a {@code Method} from the given string. Valid stings are {@code GET}, {@code POST},
         * {@code PUT} and {@code DELETE}.
         *
         * @param string
         *          The string value of this {@code Method}.
         * @return A {@code Method} based on the given string.
         */
        public static Method fromString(String string) {
            Method method;
            switch (string) {
                case "GET":
                    method = GET;
                    break;
                case "POST":
                    method = POST;
                    break;
                case "PUT":
                    method = PUT;
                    break;
                case "DELETE":
                    method = DELETE;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid http method: <" + string + ">");
            }
            return method;
        }

        /**
         * Returns a string value of this {@code Method}.
         * @return The string value of this {@code Method}.
         */
        @Override
        public String toString() {
            String string;
            switch (this) {
                case GET:
                    string = "GET";
                    break;
                case POST:
                    string = "POST";
                    break;
                case PUT:
                    string = "PUT";
                    break;
                case DELETE:
                    string = "DELETE";
                    break;
                default:
                    throw new IllegalArgumentException("Invalid http method: <" + this+ ">");
            }
            return string;
        }
    }

    /**
     * Builder of {@code BEHttpRequest}.
     */
    public static final class Builder {

        private String url;
        private Method method;
        private Map<String, String> headers;
        private BEHttpBody body;

        /**
         * Creates an empty {@code Builder}.
         */
        public Builder() {
            this.headers = new HashMap<>();
        }

        /**
         * Creates a new {@code Builder} based on the given {@code BEHttpRequest}.
         *
         * @param request
         *          The {@code BEHttpRequest} where the {@code Builder}'s values come from.
         */
        public Builder(BEHttpRequest request) {
            this.url = request.url;
            this.method = request.method;
            this.headers = new HashMap<>(request.headers);
            this.body = request.body;
        }

        /**
         * Sets the url of this {@code Builder}.
         *
         * @param url
         *          The url of this {@code Builder}.
         * @return This {@code Builder}.
         */
        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        /**
         * Sets the {@link com.csbm.http.BEHttpRequest.Method} of this {@code Builder}.
         *
         * @param method
         *          The {@link com.csbm.http.BEHttpRequest.Method} of this {@code Builder}.
         * @return This {@code Builder}.
         */
        public Builder setMethod(BEHttpRequest.Method method) {
            this.method = method;
            return this;
        }

        /**
         * Sets the {@link BEHttpBody} of this {@code Builder}.
         *
         * @param body
         *          The {@link BEHttpBody} of this {@code Builder}.
         * @return This {@code Builder}.
         */
        public Builder setBody(BEHttpBody body) {
            this.body = body;
            return this;
        }

        /**
         * Adds a header to this {@code Builder}.
         *
         * @param name
         *          The name of the header.
         * @param value
         *          The value of the header.
         * @return This {@code Builder}.
         */
        public Builder addHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }

        /**
         * Adds headers to this {@code Builder}.
         *
         * @param headers
         *          The headers that need to be added.
         * @return This {@code Builder}.
         */
        public Builder addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * Sets headers of this {@code Builder}. All existing headers will be cleared.
         *
         * @param headers
         *          The headers of this {@code Builder}.
         * @return This {@code Builder}.
         */
        public Builder setHeaders(Map<String, String> headers) {
            this.headers = new HashMap<>(headers);
            return this;
        }

        /**
         * Builds a {@link BEHttpRequest} based on this {@code Builder}.
         *
         * @return A {@link BEHttpRequest} built on this {@code Builder}.
         */
        public BEHttpRequest build() {
            return new BEHttpRequest(this);
        }
    }

    private final String url;
    private final Method method;
    private final Map<String, String> headers;
    private final BEHttpBody body;

    private BEHttpRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
        this.body = builder.body;
    }

    /**
     * Gets the url of this {@code BEHttpRequest}.
     *
     * @return The url of this {@code BEHttpRequest}.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the {@code Method} of this {@code BEHttpRequest}.
     *
     * @return The {@code Method} of this {@code BEHttpRequest}.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Gets all headers from this {@code BEHttpRequest}.
     *
     * @return The headers of this {@code BEHttpRequest}.
     */
    public Map<String, String> getAllHeaders() {
        return headers;
    }

    /**
     * Retrieves the header value from this {@code BEHttpRequest} by the given header name.
     *
     * @param name
     *          The name of the header.
     * @return The value of the header.
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * Gets http body of this {@code BEHttpRequest}.
     *
     * @return The http body of this {@code BEHttpRequest}.
     */
    public BEHttpBody getBody() {
        return body;
    }
}
