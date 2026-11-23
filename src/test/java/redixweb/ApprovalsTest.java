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

/** The web's Approvals and Reject in the details panel (redix-api #167, UC-032), on a new manager's project of entries made through the API. */
public class ApprovalsTest {
    /** Approvals of kept's project, signed in as its manager. */
    private static @NotNull Page onApprovals(final @NotNull Ledger.Kept kept) {
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        element("web.item.approvals").click();
        assertThat(element("approvals.title")).isVisible();
        return page;
    }

    private static @NotNull Locator rows(final @NotNull Page page) {
        return page.locator("[id^='approvals.card.']");
    }

    @Test(description = "Approvals lists what waits, and a filter narrows it", testName = "4d3331c9-2e20-4e05-92e0-01611bd3f686", priority = 1)
    public void approvalsListsWhatWaitsAndAFilterNarrowsIt() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(100), List.of(200, 300));
        final @NotNull Page page = onApprovals(kept);
        assertThat(rows(page)).hasCount(2);
        assertThat(element("approvals.card." + kept.entryIds().get(0))).hasCount(0);
        Web.openSearch("approvals");
        element("approvals.contractorPayments").click();
        assertThat(element("approvals.empty")).hasText(Words.of("ar", "approvals.empty"));
    }

    @Test(description = "Approve selected approves the ticked entry, and it leaves the list", testName = "57953f12-d431-4a08-ab9f-f5cdb92dbe07", priority = 2)
    public void approveSelectedApprovesTheTickedEntryAndItLeavesTheList() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(), List.of(200, 300));
        final @NotNull Page page = onApprovals(kept);
        assertThat(element("approvals.approveSelected")).isHidden();
        element("approvals.select." + kept.entryIds().get(0)).check();
        element("approvals.approveSelected").click();
        assertThat(rows(page)).hasCount(1);
        assertThat(element("approvals.card." + kept.entryIds().get(1))).isVisible();
    }

    @Test(description = "Reject with a quick reason sends the entry back, and it leaves Approvals", testName = "e46b5d9d-efaa-4bf1-a88c-92890ea1ec80", priority = 3)
    public void rejectWithAQuickReasonSendsTheEntryBackAndItLeavesApprovals() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(), List.of(200));
        final @NotNull Page page = onApprovals(kept);
        element("approvals.item." + kept.entryIds().get(0)).click();
        element("entry.reject").click();
        element("reasonSheet.quickReason.3").click();
        element("reasonSheet.send").click();
        assertThat(element("entry.status")).hasText(Words.of("ar", "entries.rejected"));
        assertThat(element("entry.reason")).hasText(Words.of("ar", "approvals.reasonDuplicate"));
        assertThat(rows(page)).hasCount(0);
    }

    @Test(description = "The approval history tells what was approved and by whom", testName = "1f1f6406-e92e-4745-bcf8-12bf71fedff2", priority = 4)
    public void theApprovalHistoryTellsWhatWasApprovedAndByWhom() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(100), List.of());
        final @NotNull Page page = onApprovals(kept);
        element("approvals.history").click();
        assertThat(element("approvalHistory.title")).isVisible();
        final @NotNull Locator lines = page.locator("[id^='approvalHistory.line.']");
        assertThat(lines).hasCount(1);
        assertThat(lines.first()).containsText("Excavation");
        assertThat(lines.first()).containsText(kept.manager().displayName());
    }
}
