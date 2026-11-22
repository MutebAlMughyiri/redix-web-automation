// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;
import redix.support.Ledger;
import redix.support.People;
import redix.support.Web;
import redix.support.Words;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static redix.support.Web.element;

/** The web's project history and My history (redix-api #172, UC-069), on a new manager's project made through the API. */
public class HistoryTest {
    /** The home of kept's project, signed in as its manager. */
    private static @NotNull Page onProject(final @NotNull Ledger.Kept kept) {
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        return page;
    }

    @Test(description = "The project history lists the entries approved, and narrows to a kind", testName = "ef14bcf2-2bf6-459f-8980-d6c8baca4c19", priority = 1)
    public void theProjectHistoryListsTheEntriesApprovedAndNarrowsToAKind() {
        final @NotNull Page page = onProject(Ledger.withEntries(List.of(100), List.of()));
        element("web.item.projectHistory").click();
        assertThat(element("projectHistory.title")).hasText(Words.of("ar", "history.projectTitle"));
        assertThat(page.locator("[id^='projectHistory.line.']").first()).isVisible();
        element("projectHistory.kindChoice").selectOption(new SelectOption().setValue("accounts"));
        assertThat(page.locator("[id^='projectHistory.line.']").filter(new Locator.FilterOptions().setHasText("Bank")).first()).isVisible();
    }

    @Test(description = "My history, from the top bar, lists my signing in", testName = "7f48c39b-09af-4d0e-b148-89c69a4f39d9", priority = 2)
    public void myHistoryFromTheTopBarListsMySigningIn() {
        final @NotNull Page page = onProject(Ledger.withEntries(List.of(), List.of()));
        element("web.myHistory").click();
        assertThat(element("myHistory.title")).hasText(Words.of("ar", "history.myTitle"));
        assertThat(page.locator("[id^='myHistory.line.']").filter(new Locator.FilterOptions().setHasText(Words.of("ar", "history.login.signIn"))).first()).isVisible();
    }
}
