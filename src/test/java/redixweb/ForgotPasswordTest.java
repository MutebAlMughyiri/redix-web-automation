// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;
import redix.support.People;
import redix.support.Web;
import redix.support.Words;

import java.util.UUID;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static redix.support.Web.element;

/**
 * Forgot password on the web (UC-002, AC-02.6 to AC-02.8). A reset with the right code needs the code from the email, which no dev instance lets a
 * test read, so it is proved by the API's Go tests; these cases prove everything a person sees up to it.
 */
public class ForgotPasswordTest {
    /**
     * Opens Forgot password from the sign-in and asks for a code for login, once the code's field has taken the focus: htmx gives a form it swapped
     * in its autofocus a moment later, and typing before then lands in the wrong field.
     */
    private static void codeAskedFor(final @NotNull String login) {
        Web.page();
        element("auth.forgotPassword").click();
        assertThat(element("forgotPassword.login")).isFocused();
        element("forgotPassword.login").fill(login);
        element("forgotPassword.send").click();
        assertThat(element("forgotPassword.code")).isFocused();
    }

    private static void reset(final @NotNull String code, final @NotNull String newPassword, final @NotNull String confirm) {
        element("forgotPassword.code").fill(code);
        element("forgotPassword.new").fill(newPassword);
        element("forgotPassword.confirm").fill(confirm);
        element("forgotPassword.reset").click();
    }

    @Test(description = "Forgot your password? opens its form in the sign-in's place, and Cancel goes back", testName = "1d09c726-54cb-41c9-9b6d-4dc9a7198ee3", priority = 1)
    public void forgotYourPasswordOpensItsFormInTheSignInsPlaceAndCancelGoesBack() {
        Web.page();
        element("auth.forgotPassword").click();
        assertThat(element("forgotPassword.title")).hasText(Words.of("ar", "auth.forgotPassword"));
        assertThat(element("forgotPassword.login")).isFocused();
        assertThat(element("forgotPassword.code")).hasCount(0);
        assertThat(element("signIn.username")).hasCount(0);
        element("forgotPassword.cancel").click();
        assertThat(element("signIn.username")).isFocused();
        assertThat(element("forgotPassword.login")).hasCount(0);
    }

    @Test(description = "Send says a code was sent and asks for the code and a new password", testName = "1bb976c4-d7d2-4168-b0fd-8b4d1633ffa7", priority = 2)
    public void sendSaysACodeWasSentAndAsksForTheCodeAndANewPassword() {
        codeAskedFor(People.newcomer().username());
        assertThat(element("forgotPassword.sent")).hasText(Words.of("ar", "auth.codeSent"));
        assertThat(element("forgotPassword.new")).isVisible();
        assertThat(element("forgotPassword.confirm")).isVisible();
        assertThat(element("forgotPassword.reset")).hasText(Words.of("ar", "auth.resetWithCode"));
        assertThat(element("forgotPassword.message")).isHidden();
    }

    @Test(description = "A login that names nobody is answered as one that does", testName = "519de421-6a84-450c-bb13-c542cbeae705", priority = 3)
    public void aLoginThatNamesNobodyIsAnsweredAsOneThatDoes() {
        codeAskedFor("nobody-" + UUID.randomUUID());
        assertThat(element("forgotPassword.sent")).hasText(Words.of("ar", "auth.codeSent"));
        assertThat(element("forgotPassword.message")).isHidden();
    }

    @Test(description = "A wrong code is refused with the app's words", testName = "f30d7a72-887a-400f-9ffa-e643fa071525", priority = 4)
    public void aWrongCodeIsRefusedWithTheAppsWords() {
        final @NotNull String login = People.newcomer().username();
        codeAskedFor(login);
        reset("000000", People.PASSWORD, People.PASSWORD);
        assertThat(element("forgotPassword.message")).hasText(Words.of("ar", "auth.codeWrong"));
        assertThat(element("forgotPassword.login")).hasValue(login);
        assertThat(element("forgotPassword.code")).isVisible();
    }

    @Test(description = "Two new passwords that differ are refused", testName = "270634c0-ba53-4caf-b544-16e2afeab11c", priority = 5)
    public void twoNewPasswordsThatDifferAreRefused() {
        codeAskedFor(People.newcomer().username());
        reset("000000", "Chosen-passw0rd-2", "Chosen-passw0rd-3");
        assertThat(element("forgotPassword.message")).hasText(Words.of("ar", "changePassword.mismatch"));
        assertThat(element("forgotPassword.code")).isVisible();
    }
}
