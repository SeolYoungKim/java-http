package study;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 자바는 스트림(Stream)으로부터 I/O를 사용한다.
 * 입출력(I/O)은 하나의 시스템에서 다른 시스템으로 데이터를 이동 시킬 때 사용한다.
 *
 * InputStream은 데이터를 읽고, OutputStream은 데이터를 쓴다.
 * FilterStream은 InputStream이나 OutputStream에 연결될 수 있다.
 * FilterStream은 읽거나 쓰는 데이터를 수정할 때 사용한다. (e.g. 암호화, 압축, 포맷 변환)
 *
 * Stream은 데이터를 바이트(byte) 단위로 읽고 쓴다.
 * 바이트가 아닌 텍스트(문자열)를 읽고 쓰려면 Reader와 Writer 클래스를 연결한다.
 * Reader, Writer는 다양한 문자 인코딩(e.g. UTF-8)을 처리할 수 있다.
 */
@DisplayName("Java I/O Stream 클래스 학습 테스트")
class IOStreamTest {

    /**
     * OutputStream 학습하기
     *
     * 자바의 기본 출력 클래스는 java.io.OutputStream이다.
     * OutputStream의 write(int b) 메서드는 기반 메서드이다.
     * <code>public abstract void write(int b) throws IOException;</code>
     */
    @Nested
    class OutputStream_학습_테스트 {

        /**
         * OutputStream은 다른 매체에 바이트로 데이터를 쓸 때 사용한다.
         * OutputStream의 서브 클래스(subclass)는 특정 매체에 데이터를 쓰기 위해 write(int b) 메서드를 사용한다.
         * 예를 들어, FilterOutputStream은 파일로 데이터를 쓸 때,
         * 또는 DataOutputStream은 자바의 primitive type data를 다른 매체로 데이터를 쓸 때 사용한다.
         *
         * write 메서드는 데이터를 바이트로 출력하기 때문에 비효율적이다.
         * <code>write(byte[] data)</code>와 <code>write(byte b[], int off, int len)</code> 메서드는
         * 1바이트 이상을 한 번에 전송 할 수 있어 훨씬 효율적이다.
         */
        @Test
        void OutputStream은_데이터를_바이트로_처리한다() throws IOException {
            final byte[] bytes = {110, 101, 120, 116, 115, 116, 101, 112};
            final OutputStream outputStream = new ByteArrayOutputStream(bytes.length);  // 내부에 byte[] buffer를 가지고 있다

            outputStream.write(bytes);  // write를 사용하면 내부에 가지고 있는 buffer에 데이터를 담는다
            final String actual = outputStream.toString();  // buffer에 담은 데이터를 String으로 변환한다

            assertThat(actual).isEqualTo("nextstep");
            outputStream.close();  // 꼭 닫아주자
        }

        /**
         * 효율적인 전송을 위해 스트림에서 버퍼(buffer)를 사용 할 수 있다.
         * BufferedOutputStream 필터를 연결하면 버퍼링이 가능하다.
         *
         * 버퍼링을 사용하면 OutputStream을 사용할 때 flush를 사용하자.
         * flush() 메서드는 버퍼가 아직 가득 차지 않은 상황에서 강제로 버퍼의 내용을 전송한다.
         * Stream은 동기(synchronous)로 동작하기 때문에 버퍼가 찰 때까지 기다리면
         * 데드락(deadlock) 상태가 되기 때문에 flush로 해제해야 한다.
         */
        @Test
        void BufferedOutputStream을_사용하면_버퍼링이_가능하다() throws IOException {
            /*
              ByteArrayOutputStream과 어떤 차이가 있을까?
              - flush를 해야 결과 전송이 된다
              - ByteArrayOutputStream은 OutputStream을 바로 구현
              - BufferedOutputStream은 FilterOutputStream을 구현
              - BufferedOutputStream은 자신의 buffer에 내용물을 담아 두었다가, flush 시점에 자신이 가진 outputStream에 결과를 전송한다.

              결론:
              - BufferedOutputStream은 다른 OutputStream을 Wrapping 하고, Wrapping한 OutputStream에게 버퍼를 제공하는 역할을 한다.
             */
            final byte[] bytes = {110, 101, 120, 116, 115, 116, 101, 112};
            try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bytes.length);
                 final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream)) {

                bufferedOutputStream.write(bytes);  // ByteArrayOutputStream에 데이터를 보내지 않고 BufferedOutputStream에 버퍼링
                assertThat(byteArrayOutputStream.toString()).isEmpty();

                bufferedOutputStream.flush();  // flush하면 ByteArrayOutputStream에 데이터를 전송하여 확인 가능
                assertThat(byteArrayOutputStream.toString()).isEqualTo("nextstep");
            }
        }

        /**
         * 스트림 사용이 끝나면 항상 close() 메서드를 호출하여 스트림을 닫는다.
         * 장시간 스트림을 닫지 않으면 파일, 포트 등 다양한 리소스에서 누수(leak)가 발생한다.
         */
        @Test
        void OutputStream은_사용하고_나서_close_처리를_해준다() throws IOException {
            final OutputStream outputStream = mock(OutputStream.class);

            /*
              java 9 이상에서는 변수를 try-with-resources로 처리할 수 있다.
             */
            try (outputStream) {
                outputStream.write(1);
            }

            verify(outputStream, atLeastOnce()).close();
        }
    }

    /**
     * InputStream 학습하기
     *
     * 자바의 기본 입력 클래스는 java.io.InputStream이다.
     * InputStream은 다른 매체로부터 바이트로 데이터를 읽을 때 사용한다.
     * InputStream의 read() 메서드는 기반 메서드이다.
     * <code>public abstract int read() throws IOException;</code>
     *
     * InputStream의 서브 클래스(subclass)는 특정 매체에 데이터를 읽기 위해 read() 메서드를 사용한다.
     */
    @Nested
    class InputStream_학습_테스트 {

        /**
         * read() 메서드는 매체로부터 단일 바이트를 읽는데, 0부터 255 사이의 값을 int 타입으로 반환한다.
         * int 값을 byte 타입으로 변환하면 -128부터 127 사이의 값으로 변환된다.
         * 그리고 Stream 끝에 도달하면 -1을 반환한다.
         */
        @Test
        void InputStream은_데이터를_바이트로_읽는다() throws IOException {
            byte[] bytes = {-16, -97, -92, -87};
            final InputStream inputStream = new ByteArrayInputStream(bytes);

            byte[] allBytes = inputStream.readAllBytes();
            final String actual = new String(allBytes);

            assertThat(actual).isEqualTo("🤩");
            assertThat(inputStream.read()).isEqualTo(-1);
            inputStream.close();
        }

        /**
         * 스트림 사용이 끝나면 항상 close() 메서드를 호출하여 스트림을 닫는다.
         * 장시간 스트림을 닫지 않으면 파일, 포트 등 다양한 리소스에서 누수(leak)가 발생한다.
         */
        @Test
        void InputStream은_사용하고_나서_close_처리를_해준다() throws IOException {
            final InputStream inputStream = mock(InputStream.class);

            /*
              java 9 이상에서는 변수를 try-with-resources로 처리할 수 있다.
             */
            try (inputStream) {
                int read = inputStream.read();
            }

            verify(inputStream, atLeastOnce()).close();
        }
    }

    /**
     * FilterStream 학습하기
     *
     * 필터는 필터 스트림, reader, writer로 나뉜다.
     * 필터는 바이트를 다른 데이터 형식으로 변환 할 때 사용한다.
     * reader, writer는 UTF-8, ISO 8859-1 같은 형식으로 인코딩된 텍스트를 처리하는 데 사용된다.
     */
    @Nested
    class FilterStream_학습_테스트 {

        /**
         * BufferedInputStream은 데이터 처리 속도를 높이기 위해 데이터를 버퍼에 저장한다.
         * InputStream 객체를 생성하고 필터 생성자에 전달하면 필터에 연결된다.
         * 버퍼 크기를 지정하지 않으면 버퍼의 기본 사이즈는 얼마일까? -> 8192
         */
        @Test
        void 필터인_BufferedInputStream를_사용해보자() throws IOException {
            final String text = "필터에 연결해보자.";
            try (final InputStream inputStream = new ByteArrayInputStream(text.getBytes());
                 final InputStream bufferedInputStream = new BufferedInputStream(inputStream);) {
                final byte[] actual = bufferedInputStream.readAllBytes();

                assertThat(bufferedInputStream).isInstanceOf(FilterInputStream.class);
                assertThat(actual).isEqualTo("필터에 연결해보자.".getBytes());
            }
        }
    }

    /**
     * 자바의 기본 문자열은 UTF-16 유니코드 인코딩을 사용한다.
     * 문자열이 아닌 바이트 단위로 처리하려니 불편하다.
     * 그리고 바이트를 문자(char)로 처리하려면 인코딩을 신경 써야 한다.
     * reader, writer를 사용하면 입출력 스트림을 바이트가 아닌 문자 단위로 데이터를 처리하게 된다.
     * 그리고 InputStreamReader를 사용하면 지정된 인코딩에 따라 유니코드 문자로 변환할 수 있다.
     */
    @Nested
    class InputStreamReader_학습_테스트 {

        /**
         * InputStreamReader를 사용해서 바이트를 문자(char)로 읽어온다.
         * 읽어온 문자(char)를 문자열(String)로 처리하자.
         * 필터인 BufferedReader를 사용하면 readLine 메서드를 사용해서 문자열(String)을 한 줄 씩 읽어올 수 있다.
         */
        @Test
        void BufferedReader를_사용하여_문자열을_읽어온다() throws IOException {
            final String emoji = String.join("\r\n",
                    "😀😃😄😁😆😅😂🤣🥲☺️😊",
                    "😇🙂🙃😉😌😍🥰😘😗😙😚",
                    "😋😛😝😜🤪🤨🧐🤓😎🥸🤩",
                    "");

            try (final InputStream inputStream = new ByteArrayInputStream(emoji.getBytes());
                 final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));) {
                final StringBuilder actual = new StringBuilder();

                /*
                  아래의 코드는 잘못 구현한 것이다. 의도와는 다른 동작을 할 수 있다고 한다.
                  ready()는 단순히 스트림이 준비가 되었는지(버퍼가 비어있지 않거나 입력 스트림이 준비된 경우 true)를 확인하는 것이다.
                  나는 비었는지 여부를 확인할 수 있겠구나 싶어서 사용했지만 올바르지 않게 동작할 확률이 높다고 한다. 아래의 개념을 참고하자.
                   - 데이터가 도착하기 전에 ready()가 호출되어 false를 반환할 수 있기도 함
                   - 반면에 read(), readLine()은 데이터가 도착할 때 까지 기다림
                  따라서, ready()를 bufferedReader가 비었는지 여부를 알기위해 사용하는 것은 하면 안된다고 한다.
                  참고: https://stackoverflow.com/questions/5244839/does-bufferedreader-ready-method-ensure-that-readline-method-does-not-return
                  참고: https://beatmejy.tistory.com/33
                 */
//                while (bufferedReader.ready()) {
//                    actual.append(bufferedReader.readLine()).append("\r\n");
//                }

                // 그러므로 아래와 같이 구현해야 한다.
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    actual.append(line).append("\r\n");
                }

                assertThat(actual).hasToString(emoji);
            }
        }
    }
}
