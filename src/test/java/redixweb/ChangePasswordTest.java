// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

import com.microsoft.playwright.Page;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;
import redix.support.People;
import redix.support.People.Person;
import redix.support.Web;
import redix.support.Words;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static redix.support.Web.element;

public class ChangePasswordTest {
    /**
     * Signs a newcomer in with their temporary password, which lands on the change-password form, once the form has taken the focus: htmx gives a
     * form it swapped in its autofocus a moment later, and typing before then lands in the wrong field.
     */
    private static @NotNull Page newcomerSignedIn() {
        final @NotNull Person person = People.newcomer();
        final @NotNull Page page = Web.page();
        element("signIn.username").fill(person.username());
        element("signIn.password").fill(People.TEMPORARY);
        element("signIn.submit").click();
        assertThat(element("changePassword.current")).isFocused();
        return page;
    }

    private static void change(final @NotNull String newPassword, final @NotNull String confirm) {
        element("changePassword.current").fill(People.TEMPORARY);
        element("changePassword.new").fill(newPassword);
        element("changePassword.confirm").fill(confirm);
        element("changePassword.submit").click();
    }

    @Test(description = "A temporary password opens the change-password form before anything else", testName = "57e325bf-3fb2-4b9b-8e7e-d81390e67c6e", priority = 1)
    public void aTemporaryPasswordOpensTheChangePasswordFormBeforeAnythingElse() {
        newcomerSignedIn();
        assertThat(element("changePassword.title")).hasText(Words.of("ar", "changePassword.title"));
        assertThat(element("changePassword.current")).isFocused();
        assertThat(element("web.sideMenu")).hasCount(0);
        assertThat(element("web.account")).isVisible();
    }

    @Test(description = "Two new passwords that differ are refused", testName = "cc1d471e-4bff-4e1a-834c-48f8fcb02bc3", priority = 2)
    public void twoNewPasswordsThatDifferAreRefused() {
        newcomerSignedIn();
        change("Chosen-passw0rd-2", "Chosen-passw0rd-3");
        assertThat(element("changePassword.message")).hasText(Words.of("ar", "changePassword.mismatch"));
        assertThat(element("web.sideMenu")).hasCount(0);
    }

    @Test(description = "A weak new password is refused with the app's words", testName = "90675e40-a8d0-46a5-afc7-3effcf22a596", priority = 3)
    public void aWeakNewPasswordIsRefusedWithTheAppsWords() {
        newcomerSignedIn();
        change("short", "short");
        assertThat(element("changePassword.message")).hasText(Words.of("ar", "changePassword.weakPassword"));
        assertThat(element("web.sideMenu")).hasCount(0);
    }

    @Test(description = "The right change opens the shell", testName = "7120100f-8873-4217-941a-4ca3f0f79300", priority = 4)
    public void theRightChangeOpensTheShell() {
        final @NotNull Page page = newcomerSignedIn();
        change(People.PASSWORD, People.PASSWORD);
        assertThat(element("web.sideMenu")).isVisible();
        assertThat(element("web.tabs")).isVisible();
        page.reload();
        assertThat(element("web.sideMenu")).isVisible();
        assertThat(element("changePassword.current")).hasCount(0);
    }
}
