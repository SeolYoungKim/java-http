package org.apache.coyote.http11;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class HttpRequest {
        private static final int REQUEST_LINE_INDEX = 0;
        private static final int HEADER_START_INDEX = 1;
        private static final String HEADER_KEY_VALUE_SEPARATOR = ":";

        private final RequestLine requestLine;
        private final HttpHeaders httpHeaders;

        private HttpRequest(final RequestLine requestLine, final HttpHeaders httpHeaders) {
            this.requestLine = requestLine;
            this.httpHeaders = httpHeaders;
        }

        public static HttpRequest from(final String httpRequestMessage) {
            String[] httpRequestMessages = httpRequestMessage.split("\n");

            List<String> httpHeaders = Arrays.stream(httpRequestMessages, HEADER_START_INDEX, httpRequestMessages.length)
                    .takeWhile(Predicate.not(String::isBlank))
                    .toList();

            return new HttpRequest(RequestLine.from(httpRequestMessages[REQUEST_LINE_INDEX]), HttpHeaders.from(httpHeaders));
        }

    public String getPath() {
        return requestLine.getPath();
    }
}