// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;
import redix.support.Ledger;
import redix.support.People;
import redix.support.Web;
import redix.support.Words;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static redix.support.Web.element;

/** The web's Users, a person in the details panel and New user (redix-api #173, UC-007, UC-008), on a new manager's project made through the API. */
public class UsersTest {
    /** Users of kept's project, signed in as its manager. */
    private static @NotNull Page onUsers(final @NotNull Ledger.Kept kept) {
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        element("web.item.users").click();
        assertThat(element("users.title")).isVisible();
        return page;
    }

    /** The row of the person whose username is username. */
    private static @NotNull Locator row(final @NotNull Page page, final @NotNull String username) {
        return page.locator("[id^='users.row.']").filter(new Locator.FilterOptions().setHasText(username));
    }

    @Test(description = "Add an existing user puts them in the project, and Edit gives them a role name", testName = "2e884186-95a1-4dd0-8ad3-8236211ddbeb", priority = 1)
    public void addAnExistingUserPutsThemInTheProjectAndEditGivesThemARoleName() {
        final @NotNull People.Person someone = People.newcomer();
        final @NotNull Page page = onUsers(Ledger.withEntries(List.of(), List.of()));
        element("users.addExisting").click();
        element("users.existingUsername").fill(someone.username());
        element("user.add").click();
        assertThat(element("user.title")).hasText(someone.displayName());
        assertThat(row(page, someone.username())).isVisible();
        element("users.edit").click();
        element("users.editRoleName").fill("Site engineer");
        element("user.save").click();
        assertThat(element("user.roleName")).hasText("Site engineer");
    }

    @Test(description = "A person disabled, asked first, is said disabled, and Enable brings them back", testName = "ef741da9-2a53-4df5-ba9e-583cc9e825e4", priority = 2)
    public void aPersonDisabledAskedFirstIsSaidDisabledAndEnableBringsThemBack() {
        final @NotNull People.Person someone = People.newcomer();
        final @NotNull Page page = onUsers(Ledger.withEntries(List.of(), List.of()));
        element("users.addExisting").click();
        element("users.existingUsername").fill(someone.username());
        element("user.add").click();
        element("users.disable").click();
        element("user.disableConfirm").click();
        assertThat(row(page, someone.username())).containsText(Words.of("ar", "users.disabled"));
        element("users.enable").click();
        assertThat(element("users.disable")).isVisible();
    }

    @Test(description = "New user adds the person with the permissions ticked, and the details panel shows them", testName = "e72faae1-c43e-442d-a30f-9c5207a4d186", priority = 3)
    public void newUserAddsThePersonWithThePermissionsTickedAndTheDetailsPanelShowsThem() {
        final @NotNull String tag = String.valueOf(ThreadLocalRandom.current().nextInt(10_000_000, 99_999_999));
        final @NotNull Page page = onUsers(Ledger.withEntries(List.of(), List.of()));
        element("users.new").click();
        element("newUser.name").fill("Person " + tag);
        element("newUser.username").fill("person" + tag);
        element("newUser.roleName").fill("Accountant");
        element("newUser.mobile").fill("05" + tag);
        element("newUser.email").fill("person" + tag + "@redix.test");
        element("newUser.password").fill(People.TEMPORARY);
        assertThat(element("newUser.readProject.input")).isChecked();
        element("newUser.recordEntries.input").check();
        element("newUser.submit").click();
        assertThat(element("user.title")).hasText("Person " + tag);
        assertThat(element("user.roleName")).hasText("Accountant");
        assertThat(element("user.permissionsLine")).containsText("2");
        assertThat(element("newUser.name")).isEmpty();
        element("web.item.users").click();
        assertThat(row(page, "person" + tag)).isVisible();
    }

    @Test(description = "Ticking one capital tick takes the other away, and Specific partners' capital opens the partners", testName = "e0e92c8e-3033-4032-b74c-9a3d21bfb17b",
            priority = 4)
    public void tickingOneCapitalTickTakesTheOtherAwayAndSpecificPartnersCapitalOpensThePartners() {
        final @NotNull Page page = onUsers(Ledger.withPartner("Partner A"));
        element("users.new").click();
        final @NotNull Locator partners = page.locator("label[id^='permissions.partner.']").filter(new Locator.FilterOptions().setHasText("Partner A"));
        assertThat(partners).isHidden();
        element("newUser.seeAllCapital.input").check();
        element("newUser.seeChosenCapital.input").check();
        assertThat(element("newUser.seeAllCapital.input")).not().isChecked();
        assertThat(partners).isVisible();
        element("newUser.seeAllCapital.input").check();
        assertThat(element("newUser.seeChosenCapital.input")).not().isChecked();
        assertThat(partners).isHidden();
    }

    @Test(description = "Each tick on a person's Permissions is kept at once, and their panel counts it", testName = "5b0c3f0e-8f8e-4c4c-9a55-2f4b8f1d6a21", priority = 5)
    public void eachTickOnAPersonsPermissionsIsKeptAtOnceAndTheirPanelCountsIt() {
        final @NotNull People.Person someone = People.newcomer();
        onUsers(Ledger.withEntries(List.of(), List.of()));
        element("users.addExisting").click();
        element("users.existingUsername").fill(someone.username());
        element("user.add").click();
        element("users.permissions").click();
        assertThat(element("userPermissions.name")).hasText(someone.displayName());
        assertThat(element("userPermissions.readProject.input")).isChecked();
        element("userPermissions.recordEntries.input").check();
        assertThat(element("user.permissionsLine")).containsText("2");
        element("userPermissions.recordEntries.input").uncheck();
        assertThat(element("user.permissionsLine")).containsText("1");
        element("users.permissions").click();
        assertThat(element("userPermissions.recordEntries.input")).not().isChecked();
    }
}
