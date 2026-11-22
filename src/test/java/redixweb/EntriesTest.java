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

    @Test(description = "The manager approves a waiting entry from the panel, and its row follows", testName = "018988e4-a613-491b-bb5a-5edae4905477", priority = 5)
    public void theManagerApprovesAWaitingEntryFromThePanelAndItsRowFollows() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(100), List.of(300));
        onTheList(kept);
        element("entries.row." + kept.entryIds().get(1)).click();
        assertThat(element("entry.status")).hasText(Words.of("ar", "entries.pendingApproval"));
        element("entry.approve").click();
        assertThat(element("entry.status")).hasText(Words.of("ar", "entries.approved"));
        assertThat(element("entries.row." + kept.entryIds().get(1))).containsText(Words.of("ar", "entries.approved"));
        assertThat(element("entry.approve")).hasCount(0);
    }

    /** Add entry opened from the Entries tab of kept's project, its Excavation table chosen and drawn again for it. */
    private static void addingToExcavation(final @NotNull Ledger.Kept kept) {
        onTheList(kept);
        element("entries.new").click();
        assertThat(element("addEntry.title")).isVisible();
        element("addEntry.table").selectOption(new SelectOption().setLabel("Excavation"));
        assertThat(element("addEntry.table").locator("option[selected]")).hasText("Excavation");
    }

    @Test(description = "Add entry sends a purchase for approval, and the new entry shows in the panel", testName = "883bacdf-d1ee-4e83-a84b-99e3475d97ae", priority = 6)
    public void addEntrySendsAPurchaseForApprovalAndTheNewEntryShowsInThePanel() {
        addingToExcavation(Ledger.withEntries(List.of(), List.of()));
        element("addEntry.description").fill("Web payment");
        element("addEntry.amount").fill("1,500");
        element("addEntry.send").click();
        assertThat(element("addEntry.message")).hasText(Words.of("ar", "expenses.sent"));
        assertThat(element("entry.status")).hasText(Words.of("ar", "entries.pendingApproval"));
        assertThat(element("entry.amount")).hasText("1,500.00");
        assertThat(element("addEntry.description")).hasValue("");
    }

    @Test(description = "Add entry says why an amount that is no number cannot be saved, and keeps what was typed", testName = "584cf050-39d9-464a-932a-373858b7bc75", priority = 7)
    public void addEntrySaysWhyAnAmountThatIsNoNumberCannotBeSavedAndKeepsWhatWasTyped() {
        addingToExcavation(Ledger.withEntries(List.of(), List.of()));
        element("addEntry.description").fill("Web payment");
        element("addEntry.amount").fill("abc");
        element("addEntry.send").click();
        assertThat(element("addEntry.message")).hasText(Words.of("ar", "expenses.invalidAmount"));
        assertThat(element("addEntry.description")).hasValue("Web payment");
        assertThat(element("entry.amount")).hasCount(0);
    }

    @Test(description = "Change fills the form with a waiting entry, and saving changes its amount", testName = "642a5dde-a0f4-4855-8ca9-a75a979363c3", priority = 8)
    public void changeFillsTheFormWithAWaitingEntryAndSavingChangesItsAmount() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(), List.of(300));
        onTheList(kept);
        element("entries.row." + kept.entryIds().get(0)).click();
        element("entry.change").click();
        assertThat(element("addEntry.title")).hasText(Words.of("ar", "entries.edit"));
        assertThat(element("addEntry.amount")).hasValue("300");
        element("addEntry.amount").fill("350");
        element("addEntry.send").click();
        assertThat(element("addEntry.message")).hasText(Words.of("ar", "expenses.sent"));
        assertThat(element("entry.amount")).hasText("350.00");
    }

    @Test(description = "A contract payment shows the contract's value and what remains after it, and warns past the value", testName = "e5b8ddf3-8c2b-460b-974a-630253b626a7", priority = 9)
    public void aContractPaymentShowsTheContractsValueAndWhatRemainsAfterItAndWarnsPastTheValue() {
        onTheList(Ledger.withContract(1000));
        element("entries.new").click();
        element("addEntry.table").selectOption(new SelectOption().setLabel("Contractor (Excavation)"));
        assertThat(element("addEntry.value")).hasText("1,000.00");
        assertThat(element("addEntry.remaining")).hasText("1,000.00");
        element("addEntry.amount").fill("1200");
        element("addEntry.amount").press("Tab");
        assertThat(element("addEntry.remaining")).hasText("(200.00)");
        assertThat(element("addEntry.overrun")).hasText(Words.of("ar", "contractorPayments.overrun"));
    }

    @Test(description = "The details panel follows the tab in front: each tab brings back what it selected", testName = "f3d82fe5-d344-4323-9c8e-71c07a7f2e1d", priority = 10)
    public void theDetailsPanelFollowsTheTabInFrontEachTabBringsBackWhatItSelected() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(100, 200), List.of());
        final @NotNull Page page = onTheList(kept);
        element("entries.row." + kept.entryIds().get(0)).click();
        assertThat(element("entry.amount")).hasText("100.00");
        element("web.item.accounts").click();
        assertThat(element("accounts.title")).isVisible();
        assertThat(page.locator("[id='entry.amount']")).hasCount(0);
        page.locator("[id^='accounts.row.']").filter(new Locator.FilterOptions().setHasText("Bank")).click();
        assertThat(element("account.title")).hasText("Bank");
        element("web.tab.entries.title").click();
        assertThat(element("entry.amount")).hasText("100.00");
        assertThat(element("entries.row." + kept.entryIds().get(0))).hasAttribute("aria-selected", "true");
        element("web.tab.accounts.title").click();
        assertThat(element("account.title")).hasText("Bank");
    }
}

