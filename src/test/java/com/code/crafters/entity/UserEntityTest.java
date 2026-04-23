package com.code.crafters.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User Entity Tests")
class UserEntityTest {

    @Test
    void shouldNormalizeEmailAndAliasOnPrePersist() {
        User user = new User();
        user.setEmail("  TEST@EXAMPLE.COM  ");
        user.setAlias("  alias-test  ");

        user.onPrePersist();

        assertEquals("test@example.com", user.getEmail());
        assertEquals("alias-test", user.getAlias());
    }

    @Test
    void shouldNormalizeEmailAndAliasOnPreUpdate() {
        User user = new User();
        user.setEmail("  USER@MAIL.COM ");
        user.setAlias("  myalias ");

        user.onPreUpdate();

        assertEquals("user@mail.com", user.getEmail());
        assertEquals("myalias", user.getAlias());
    }

    @Test
    void shouldKeepNullAliasAndEmailWithoutFailing() {
        User user = new User();
        user.setEmail(null);
        user.setAlias(null);

        user.onPrePersist();

        assertEquals(null, user.getEmail());
        assertEquals(null, user.getAlias());
    }
}
