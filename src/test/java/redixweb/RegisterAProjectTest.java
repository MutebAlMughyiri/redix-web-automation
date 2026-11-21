// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

import com.microsoft.playwright.Page;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;
import redix.support.People;
import redix.support.Web;
import redix.support.Words;

import java.util.concurrent.ThreadLocalRandom;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static redix.support.Web.element;

/** Register new project on the web (redix-api #164, UC-066): from the sign-in, with the manager's own new login. */
public class RegisterAProjectTest {
    /** Opens Register new project from the sign-in, once the form has given the project's name the focus. */
    private static @NotNull Page onTheForm() {
        final @NotNull Page page = Web.page();
        element("signIn.registerProject").click();
        assertThat(element("newProject.name")).isFocused();
        return page;
    }

    /**
     * Adds what to the list through its field and Enter, once the list came back with the cursor in its field: the new field has no value, and
     * takes the focus only after htmx has settled it and bound its Enter, so the next one typed is not pressed into a field still being set up.
     */
    private static void added(final @NotNull String field, final @NotNull String what) {
        element(field).fill(what);
        element(field).press("Enter");
        assertThat(element(field)).hasValue("");
        assertThat(element(field)).isFocused();
    }

    /** Fills the whole form for a manager whose username is username, with two units and a partner. */
    private static void filled(final @NotNull String username) {
        element("newProject.name").fill("Project " + username);
        added("newProject.unitName", "A1");
        added("newProject.unitName", "A2");
        added("newProject.partnerName", "Partner A");
        element("newProject.managerName").fill("Manager " + username);
        element("newProject.username").fill(username);
        element("newProject.mobile").fill("0500000000");
        element("newProject.password").fill(People.PASSWORD);
    }

    private static @NotNull String newUsername() {
        return "manager" + ThreadLocalRandom.current().nextInt(10_000_000, 99_999_999);
    }

    @Test(description = "Register new project opens its form in the sign-in's place, and Cancel goes back", testName = "1f475da5-779f-475d-a87c-0e04ecb2c2e5", priority = 1)
    public void registerNewProjectOpensItsFormInTheSignInsPlaceAndCancelGoesBack() {
        onTheForm();
        assertThat(element("newProject.title")).hasText(Words.of("ar", "projects.new"));
        assertThat(element("newProject.hint")).hasText(Words.of("ar", "register.hint"));
        assertThat(element("legal.agreement")).hasText(Words.of("ar", "legal.agreement"));
        assertThat(element("signIn.username")).hasCount(0);
        element("newProject.cancel").click();
        assertThat(element("signIn.username")).isFocused();
    }

    @Test(description = "Units and partners are added one at a time, by Enter or Add, and removed", testName = "ca44adb9-bce5-4fcf-8836-4aacf0eead9c", priority = 2)
    public void unitsAndPartnersAreAddedOneAtATimeByEnterOrAddAndRemoved() {
        onTheForm();
        added("newProject.unitName", "A1");
        element("newProject.unitName").fill("A2");
        element("newProject.addUnit").click();
        assertThat(element("newProject.unit.1")).containsText("A2");
        assertThat(element("newProject.unitName")).isFocused();
        added("newProject.partnerName", "Partner A");
        assertThat(element("newProject.partner.0")).containsText("Partner A");
        element("newProject.unit.0").locator("button").click();
        assertThat(element("newProject.unit.0")).containsText("A2");
        assertThat(element("newProject.unit.1")).hasCount(0);
        assertThat(element("newProject.partner.0")).containsText("Partner A");
    }

    @Test(description = "A registration waits for the admin, and its manager signs in to follow it", testName = "360d5384-8921-464e-bb39-8d050566892b", priority = 3)
    public void aRegistrationWaitsForTheAdminAndItsManagerSignsInToFollowIt() {
        final @NotNull String username = newUsername();
        onTheForm();
        filled(username);
        element("newProject.submit").click();
        assertThat(element("signIn.notice")).hasText(Words.of("ar", "register.sent"));
        Web.signIn(username, People.PASSWORD);
        assertThat(element("projects.title")).isVisible();
        assertThat(Web.current().locator("[id^='projects.card.']").first()).isDisabled();
        assertThat(Web.current().locator("[id^='projects.card.']").first()).containsText(Words.of("ar", "projects.pending"));
    }

    @Test(description = "A taken username is refused with the app's words, and what was typed stays", testName = "a5d4c5a5-a7a4-4b46-9267-8a94c6ddb941", priority = 4)
    public void aTakenUsernameIsRefusedWithTheAppsWordsAndWhatWasTypedStays() {
        final @NotNull String username = People.signedUp().username();
        onTheForm();
        filled(username);
        element("newProject.submit").click();
        assertThat(element("newProject.message")).hasText(Words.of("ar", "register.usernameTaken"));
        assertThat(element("newProject.name")).hasValue("Project " + username);
        assertThat(element("newProject.unit.1")).containsText("A2");
        assertThat(element("newProject.partner.0")).containsText("Partner A");
        assertThat(element("newProject.password")).hasValue("");
    }
}
