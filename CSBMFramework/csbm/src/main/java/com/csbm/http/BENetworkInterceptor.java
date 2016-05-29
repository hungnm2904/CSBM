package com.csbm.http;

import java.io.IOException;

/**
 * Created by akela on 29/05/2016.
 * @code BRNetworkInterceptor} is used to observe requests going out and the corresponding
 * responses coming back in.
 */
public interface BENetworkInterceptor {
    /**
     * Intercepts a {@link BEHttpRequest} with the help of
     * {@link com.csbm.http.BENetworkInterceptor.Chain} and returns the intercepted
     * {@link BEHttpResponse}.
     *
     * @param chain
     *          The helper chain we use to get the request, proceed the request and receive the
     *          response.
     * @return The intercepted response.
     * @throws IOException
     */
    BEHttpResponse intercept(Chain chain) throws IOException;

    /**
     * {@code Chain} is used to chain the interceptors. It can get the request from the previous
     * interceptor, proceed the request to the next interceptor and get the response from the next
     * interceptor. In most of the cases, you don't need to implement this interface.
     */
    interface Chain {

        /**
         * Gets the {@link BEHttpRequest} from this chain.
         *
         * @return The {@link BEHttpRequest} of this chain.
         */
        BEHttpRequest getRequest();

        /**
         * Proceeds the intercepted {@link BEHttpRequest} in this chain to next
         * {@code BENetworkInterceptor} or network and gets the {@link BEHttpResponse}.
         *
         * @param request
         *          The intercepted {@link BEHttpRequest}.
         * @return The {@link BEHttpResponse} from next {@code BENetworkInterceptor} or network.
         * @throws IOException
         */
        BEHttpResponse proceed(BEHttpRequest request) throws IOException;
    }
}
