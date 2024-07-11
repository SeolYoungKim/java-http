package nextstep.org.apache.coyote.http11;

import org.junit.jupiter.api.DisplayName;
import support.StubSocket;
import org.apache.coyote.http11.Http11Processor;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

class Http11ProcessorTest {
    @DisplayName("Http11Processor 프로세스를 수행하면 응답이 반환된다")
    @Test
    void process() {
        // given
        final var socket = new StubSocket();
        final var processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        var expected = String.join("\r\n",
                "HTTP/1.1 200 OK ",
                "Content-Type: text/html;charset=utf-8 ",
                "Content-Length: 5564 ",
                "");

        assertThat(socket.output()).contains(expected);
    }

    @DisplayName("/index.html로 요청하면 responseBody에 index.html 파일의 내용이 추가된다.")
    @Test
    void index() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /index.html HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final Http11Processor processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        String responseBody = getResponseBody("static/index.html");
        var expected = String.join("\r\n",
                "HTTP/1.1 200 OK ",
                "Content-Type: text/html;charset=utf-8 ",
                "Content-Length: 5564 ", // 운영체제 환경에 따라 다른 값이 나올 수 있음. 자신의 개발 환경에 맞춰 수정할 것.
                "",
                responseBody);

        assertThat(socket.output()).isEqualTo(expected);
    }

    @DisplayName("/css/styles.css로 요청하면 responseBody에 styles.css 파일의 내용이 추가된다.")
    @Test
    void stylesCss() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /css/styles.css HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final Http11Processor processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        String responseBody = getResponseBody("static/css/styles.css");
        var expected = String.join("\r\n",
                "HTTP/1.1 200 OK ",
                "Content-Type: text/css;charset=utf-8 ",
                "Content-Length: 211991 ", // 운영체제 환경에 따라 다른 값이 나올 수 있음. 자신의 개발 환경에 맞춰 수정할 것.
                "",
                responseBody);

        assertThat(socket.output()).isEqualTo(expected);
    }

    private String getResponseBody(String resourcePath) throws IOException {
        final URL resource = getClass().getClassLoader().getResource(resourcePath);
        return new String(Files.readAllBytes(new File(resource.getFile()).toPath()));
    }

    @DisplayName("/login + GET 요청은 login.html 파일을 응답한다")
    @Test
    void getLogin() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final Http11Processor processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        String responseBody = getResponseBody("static/login.html");
        var expected = String.join("\r\n",
                "HTTP/1.1 200 OK ",
                "Content-Type: text/html;charset=utf-8 ",
                "Content-Length: 3797 ",
                "",
                responseBody);

        assertThat(socket.output()).isEqualTo(expected);
    }

    @DisplayName("/login + POST 요청은 성공 시 302 FOUND를 응답하고 Location으로 /index.html을 제공한다")
    @Test
    void postLogin() {
        // given
        String body = "account=gugu&password=password";
        final String httpRequest = String.join("\r\n",
                "POST /login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Content-Length: %d ".formatted(body.getBytes().length),
                "Connection: keep-alive ",
                "",
                body);

        final var socket = new StubSocket(httpRequest);
        final Http11Processor processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        var expected = String.join("\r\n",
                "HTTP/1.1 302 Found ",
                "Location: /index.html ",
                "Content-Length: 0 ",
                "",
                "");

        assertThat(socket.output()).isEqualTo(expected);
    }

    @DisplayName("/login + POST 요청은 실패 시 302 FOUND를 응답하고 Location으로 /401.html을 제공한다")
    @Test
    void postLoginFail() {
        // given
        String body = "account=gugu&password=passwordddd!";
        final String httpRequest = String.join("\r\n",
                "POST /login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Content-Length: %d ".formatted(body.getBytes().length),
                "Connection: keep-alive ",
                "",
                body);

        final var socket = new StubSocket(httpRequest);
        final Http11Processor processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        var expected = String.join("\r\n",
                "HTTP/1.1 302 Found ",
                "Location: /401.html ",
                "Content-Length: 0 ",
                "",
                "");

        assertThat(socket.output()).isEqualTo(expected);
    }
}
