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

/** The web's Entries tab and its details panel (redix-api #166, UC-028), on a new manager's project of entries made through the API. */
public class EntriesTest {
    /** The Entries tab of kept's project, signed in as its manager. */
    private static @NotNull Page onTheList(final @NotNull Ledger.Kept kept) {
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        element("web.item.entries").click();
        assertThat(element("entries.title")).isVisible();
        return page;
    }

    private static @NotNull Locator rows(final @NotNull Page page) {
        return page.locator("[id^='entries.row.']");
    }

    @Test(description = "The Entries tab lists the project's entries, newest first, with their status", testName = "5adf966c-7eca-4597-84aa-a4790d73ad7b", priority = 1)
    public void theEntriesTabListsTheProjectsEntriesNewestFirstWithTheirStatus() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(100, 200), List.of(300));
        final @NotNull Page page = onTheList(kept);
        assertThat(rows(page)).hasCount(3);
        assertThat(rows(page).first()).hasId("entries.row." + kept.entryIds().get(2));
        assertThat(element("entries.row." + kept.entryIds().get(2))).containsText(Words.of("ar", "entries.pendingApproval"));
        assertThat(element("entries.row." + kept.entryIds().get(0))).containsText(Words.of("ar", "entries.approved"));
        assertThat(element("entries.row." + kept.entryIds().get(0))).containsText("100.00");
    }

    @Test(description = "A status filter narrows the list, and Clear brings it all back", testName = "802b5170-7c44-44c9-974f-a8f1c8150770", priority = 2)
    public void aStatusFilterNarrowsTheListAndClearBringsItAllBack() {
        final @NotNull Page page = onTheList(Ledger.withEntries(List.of(100, 200), List.of(300)));
        element("entries.status.approved").click();
        assertThat(rows(page)).hasCount(2);
        assertThat(element("entries.status.approved").locator("input")).isChecked();
        element("entries.clear").click();
        assertThat(rows(page)).hasCount(3);
        assertThat(element("entries.clear")).hasCount(0);
    }

    @Test(description = "A row opens its entry in the details panel, the arrows move it, and Esc hides it", testName = "0e022588-fe31-45e4-be14-43e0055801e2", priority = 3)
    public void aRowOpensItsEntryInTheDetailsPanelTheArrowsMoveItAndEscHidesIt() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(100, 200), List.of(300));
        final @NotNull Page page = onTheList(kept);
        element("entries.row." + kept.entryIds().get(2)).click();
        assertThat(element("entry.amount")).hasText("300.00");
        assertThat(element("entry.status")).hasText(Words.of("ar", "entries.pendingApproval"));
        page.keyboard().press("ArrowDown");
        assertThat(element("entry.amount")).hasText("200.00");
        assertThat(element("entries.row." + kept.entryIds().get(1))).hasAttribute("aria-selected", "true");
        page.keyboard().press("Escape");
        assertThat(element("entry.amount")).hasCount(0);
        assertThat(element("entries.row." + kept.entryIds().get(1))).isFocused();
    }

    @Test(description = "The search finds an entry by its amount", testName = "4c2089c2-4059-4063-9def-12ccc4938883", priority = 4)
    public void theSearchFindsAnEntryByItsAmount() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(100, 4875), List.of(300));
        final @NotNull Page page = onTheList(kept);
        element("entries.search").fill("4,875");
        assertThat(rows(page)).hasCount(1);
        assertThat(element("entries.row." + kept.entryIds().get(1))).isVisible();
    }
}
