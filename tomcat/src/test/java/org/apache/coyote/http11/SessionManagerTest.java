package org.apache.coyote.http11;

import org.apache.catalina.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionManagerTest {
    @DisplayName("세션이 있으면 세션을 반환한다")
    @Test
    void findSession() {
        SessionManager sessionManager = SessionManager.create();
        Session session = DefaultSession.of("id1", sessionManager);

        Session findSession = sessionManager.findSession(session.getId());

        assertThat(findSession).isEqualTo(session);
    }

    @DisplayName("세션이 없으면 null을 반환한다")
    @Test
    void findSession2() {
        SessionManager sessionManager = SessionManager.create();
        Session session = DefaultSession.of("id1", sessionManager);
        sessionManager.add(session);

        Session findSession = sessionManager.findSession("newId");

        assertThat(findSession).isNull();
    }
}