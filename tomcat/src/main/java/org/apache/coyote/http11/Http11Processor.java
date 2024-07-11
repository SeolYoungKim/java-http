package org.apache.coyote.http11;

import camp.nextstep.db.InMemoryUserRepository;
import camp.nextstep.exception.UncheckedServletException;
import camp.nextstep.model.User;
import org.apache.coyote.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.StringJoiner;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.info("connect host: {}, port: {}", connection.getInetAddress(), connection.getPort());
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             OutputStream outputStream = connection.getOutputStream()) {

            String httpRequestMessage = readHttpRequestMessage(br);
            HttpRequest httpRequest = HttpRequest.from(httpRequestMessage);
            String path = httpRequest.getPath();
            handleLoginRequest(path, httpRequest);

            File resource = new ResourceFinder().findByPath(path);
            MediaType mediaType = MediaType.from(resource);
            List<HttpHeader> httpHeaders = List.of(HttpHeader.of(HttpHeaderName.CONTENT_TYPE.getValue(), mediaType.getValue() + ";charset=utf-8"));
            String responseBody = new String(Files.readAllBytes(resource.toPath()));
            HttpResponse response = HttpResponse.of(httpRequest.getProtocol(), HttpStatus.OK, httpHeaders, responseBody);
            outputStream.write(response.createFormat().getBytes());  // 바디에 응답이 될 때도 있지만 되지 않을 때도 있다
            outputStream.flush();
        } catch (IOException | UncheckedServletException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String readHttpRequestMessage(final BufferedReader br) throws IOException {
        StringJoiner stringJoiner = new StringJoiner("\n");
        while (true) {
            String line = br.readLine();
            if (line == null || line.isEmpty()) {
                break;
            }
            stringJoiner.add(line);
        }
        return stringJoiner.toString();
    }

    private void handleLoginRequest(final String path, final HttpRequest httpRequest) {
        if (path.contains("login") && httpRequest.hasQueryParams()) {
            checkUserPassword(httpRequest);
        }
    }

    private void checkUserPassword(final HttpRequest httpRequest) {
        User user = InMemoryUserRepository.findByAccount(httpRequest.getQueryParamValue("account"))
                .orElseThrow();
        String password = httpRequest.getQueryParamValue("password");
        if (user.checkPassword(password)) {
            log.info(user.toString());
        }
    }
}
