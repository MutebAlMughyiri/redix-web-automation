// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

import com.microsoft.playwright.Page;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;
import redix.support.People;
import redix.support.People.Person;
import redix.support.Web;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.testng.Assert.assertTrue;
import static redix.support.Web.element;

public class SignOutTest {
    @Test(description = "Sign out returns to the sign-in form, and a reload stays signed out", testName = "ce38b992-ebc0-4c6c-8e45-4ce24e5f3ac1", priority = 1)
    public void signOutReturnsToTheSignInFormAndAReloadStaysSignedOut() {
        final @NotNull Person person = People.signedUp();
        final @NotNull Page page = Web.page();
        Web.signIn(person.username(), People.PASSWORD);
        Web.openAccount("myAccount.signOut");
        assertThat(element("signIn.username")).isVisible();
        assertThat(element("signIn.message")).isHidden();
        assertThat(element("web.sideMenu")).hasCount(0);
        assertTrue(page.context().cookies().stream().noneMatch(cookie -> cookie.name.equals("redix_access") || cookie.name.equals("redix_refresh")),
                "the session's cookies are gone");
        page.reload();
        assertThat(element("signIn.username")).isVisible();
        assertThat(element("signIn.message")).isHidden();
    }
}
