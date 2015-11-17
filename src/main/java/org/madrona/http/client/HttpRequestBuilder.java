package org.madrona.http.client;

/**
 * Builds Http Request, allows for adding listeners for response events
 * and for launching such a request.
 */
public interface HttpRequestBuilder {

    <T> HttpRequestBuilder addHeader(HeaderValueType<T> type, T value);

    HttpRequestBuilder addPathElement(String element);

    HttpRequestBuilder addQueryPair(String key, String value);

    HttpRequestBuilder setAnchor(String anchor);

    HttpRequestBuilder setHost(String host);

    HttpRequestBuilder setPath(String path);


}
