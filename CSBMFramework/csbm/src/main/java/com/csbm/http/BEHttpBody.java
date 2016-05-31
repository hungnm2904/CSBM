package com.csbm.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by akela on 29/05/2016.
 * The base interface of a http body. It can be implemented by different http libraries such as
 * Apache http, Android URLConnection, Square OKHttp and so on.
 */
public abstract class BEHttpBody {

    private final String contentType;
    private final long contentLength;

    /**
     * Returns the content of this body.
     *
     * @return The content of this body.
     * @throws IOException
     *           Throws an exception if the content of this body is inaccessible.
     */
    public abstract InputStream getContent() throws IOException;

    /**
     * Writes the content of this request to {@code out}.
     *
     * @param out
     *          The outputStream the content of this body needs to be written to.
     * @throws IOException
     *           Throws an exception if the content of this body can not be written to {@code out}.
     */
    public abstract void writeTo(OutputStream out) throws IOException;

    /**
     * Creates an {@code BEHttpBody} with given {@code Content-Type} and {@code Content-Length}.
     *
     * @param contentType
     *          The {@code Content-Type} of the {@code BEHttpBody}.
     * @param contentLength
     *          The {@code Content-Length} of the {@code BEHttpBody}.
     */
    public BEHttpBody(String contentType, long contentLength) {
        this.contentType = contentType;
        this.contentLength = contentLength;
    }

    /**
     * Returns the number of bytes which will be written to {@code out} when {@link #writeTo} is
     * called, or {@code -1} if that count is unknown.
     *
     * @return The Content-Length of this body.
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * Returns the {@code Content-Type} of this body.
     *
     * @return The {@code Content-Type} of this body.
     */
    public String getContentType() {
        return contentType;
    }
}
