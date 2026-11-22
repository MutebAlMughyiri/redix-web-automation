// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;
import redix.support.People;
import redix.support.People.Person;
import redix.support.Web;
import redix.support.Words;

import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static redix.support.Web.element;

public class SignInWithUsernameAndPasswordTest {
    @Test(description = "The right password opens the shell, its parts arriving", testName = "7545083b-99e2-445b-9323-1038517af2ba", priority = 1)
    public void theRightPasswordOpensTheShellItsPartsArriving() {
        final @NotNull Person person = People.signedUp();
        Web.page();
        Web.signIn(person.username(), People.PASSWORD);
        assertThat(element("web.shell")).hasClass(Pattern.compile("\\barriving\\b"));
        assertThat(element("web.tabs")).isVisible();
        assertThat(element("web.account")).hasText(person.displayName());
        assertThat(element("signIn.username")).hasCount(0);
    }

    @Test(description = "A wrong password is refused with the app's words", testName = "6b31c4c1-3ff3-4f67-98e2-7c3063f350bb", priority = 2)
    public void aWrongPasswordIsRefusedWithTheAppsWords() {
        final @NotNull Person person = People.signedUp();
        Web.page();
        element("signIn.username").fill(person.username());
        element("signIn.password").fill("Wrong-passw0rd-1");
        element("signIn.submit").click();
        assertThat(element("signIn.message")).hasText(Words.of("ar", "signIn.invalidCredentials"));
        assertThat(element("signIn.username")).hasValue(person.username());
        assertThat(element("signIn.password")).isFocused();
        assertThat(element("web.sideMenu")).hasCount(0);
    }

    @Test(description = "The fifth wrong password in a row locks the account", testName = "e94641b4-be3e-4a31-8227-ad444c4e14e3", priority = 3)
    public void theFifthWrongPasswordInARowLocksTheAccount() {
        final @NotNull Person person = People.signedUp();
        final @NotNull Page page = Web.page();
        for (int wrong = 0; wrong < 5; wrong++) {
            element("signIn.username").fill(person.username());
            element("signIn.password").fill("Wrong-passw0rd-1");
            page.waitForResponse(answer -> answer.url().endsWith("/sign-in"), () -> element("signIn.submit").click());
            assertThat(element("signIn.message")).isVisible();
            assertThat(element("signIn.password")).isFocused();
        }
        element("signIn.password").fill(People.PASSWORD);
        element("signIn.submit").click();
        assertThat(element("signIn.message")).hasText(Words.of("ar", "signIn.accountLocked"));
        assertThat(element("web.sideMenu")).hasCount(0);
    }

    @Test(description = "The sign-in form has what the app's has, Register new project and Forgot your password links", testName = "47149d0a-ff76-45cc-a6c3-a65c1c152b01", priority = 4)
    public void theSignInFormHasWhatTheAppsHasRegisterAndForgotPasswordLinks() {
        Web.page();
        for (final @NotNull String id : List.of("signIn.title", "signIn.hint", "signIn.username", "signIn.password", "signIn.submit", "myAccount.arabic",
                "myAccount.english")) {
            assertThat(element(id)).isVisible();
        }
        assertThat(element("signIn.message")).isHidden();
        assertThat(element("signIn.registerProject")).hasAttribute("hx-get", "/register");
        assertThat(element("auth.forgotPassword")).hasAttribute("hx-get", "/forgot-password");
        assertThat(element("appVersion.line")).hasText(Pattern.compile(Words.of("ar", "app.version") + " \\d+\\.\\d+\\.\\d+"));
        assertThat(element("web.sideMenu")).hasCount(0);
    }

    @Test(description = "The username takes the focus and Enter signs in", testName = "8fffb6b3-f96e-4cb6-b95d-35c66700f3aa", priority = 5)
    public void theUsernameTakesTheFocusAndEnterSignsIn() {
        final @NotNull Person person = People.signedUp();
        final @NotNull Page page = Web.page();
        assertThat(element("signIn.username")).isFocused();
        page.keyboard().type(person.username());
        page.keyboard().press("Tab");
        page.keyboard().type(People.PASSWORD);
        page.keyboard().press("Enter");
        assertThat(element("web.sideMenu")).isVisible();
    }

    @Test(description = "The language is chosen at the sign-in form's foot", testName = "9271ce12-62c8-4510-acb2-ac183988e62d", priority = 6)
    public void theLanguageIsChosenAtTheSignInFormsFoot() {
        final @NotNull Page page = Web.page();
        assertThat(page.locator("html")).hasAttribute("dir", "rtl");
        element("myAccount.english").click();
        assertThat(page.locator("html")).hasAttribute("dir", "ltr");
        assertThat(element("signIn.submit")).hasText(Words.of("en", "signIn.submit"));
        page.reload();
        assertThat(page.locator("html")).hasAttribute("lang", "en");
        assertThat(element("myAccount.english")).hasAttribute("aria-current", "true");
    }

    @Test(description = "The session's cookies cannot be read by the page's script", testName = "3f0be4f0-72d7-47ce-909b-97dd9809e4f6", priority = 7)
    public void theSessionsCookiesCannotBeReadByThePagesScript() {
        final @NotNull Person person = People.signedUp();
        final @NotNull Page page = Web.page();
        Web.signIn(person.username(), People.PASSWORD);
        final @NotNull String readable = String.valueOf(page.evaluate("document.cookie"));
        assertFalse(readable.contains("redix_access") || readable.contains("redix_refresh"), readable);
        final @NotNull List<Cookie> session = page.context().cookies().stream().filter(cookie -> cookie.name.startsWith("redix_access")
                || cookie.name.startsWith("redix_refresh")).toList();
        assertEquals(session.size(), 2);
        for (final @NotNull Cookie cookie : session) {
            assertTrue(cookie.httpOnly && cookie.secure && cookie.sameSite == SameSiteAttribute.STRICT, cookie.name);
        }
    }

    @Test(description = "A reload keeps the person signed in", testName = "5d0402a3-c2ad-44ab-9c8c-ef216a5e8bd1", priority = 8)
    public void aReloadKeepsThePersonSignedIn() {
        final @NotNull Person person = People.signedUp();
        final @NotNull Page page = Web.page();
        Web.signIn(person.username(), People.PASSWORD);
        page.reload();
        assertThat(element("web.sideMenu")).isVisible();
        assertThat(element("web.account")).hasText(person.displayName());
        assertThat(element("signIn.username")).hasCount(0);
        assertThat(element("web.shell")).not().hasClass(Pattern.compile("\\barriving\\b"));
    }
}
