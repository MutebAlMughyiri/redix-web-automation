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

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static redix.support.Web.element;

/** The web's Users and a person in the details panel (redix-api #173, UC-007, UC-008), on a new manager's project made through the API. */
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
}
