// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

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

/** The web's My permissions, Ask for permissions and Permission requests (redix-api #112, #173), on a new manager's project made through the API. */
public class PermissionRequestsTest {
    /** My permissions of kept's project, signed in as its manager. */
    private static @NotNull Page onMyPermissions(final @NotNull Ledger.Kept kept) {
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        Web.openItem("myPermissions");
        assertThat(element("myPermissions.title")).isVisible();
        return page;
    }

    /** Asks, from My permissions, to give up Generate reports, with reason. */
    private static void askToGiveUpGeneratingReports(final @NotNull String reason) {
        element("myPermissions.ask").click();
        assertThat(element("permissionRequest.send")).isDisabled();
        element("permissionRequest.generateReports.input").uncheck();
        element("permissionRequest.send").click();
        element("permissionRequest.reason").fill(reason);
        element("permissionRequest.save").click();
        assertThat(element("myPermissions.notice")).hasText(Words.of("ar", "permissionRequests.sent"));
    }

    @Test(description = "Ask for permissions sends a request, My permissions says it waits, and Withdraw takes it back", testName = "8d2c7b51-3e4a-4f6b-9c1d-7a5e2f8b0c63",
            priority = 1)
    public void askForPermissionsSendsARequestMyPermissionsSaysItWaitsAndWithdrawTakesItBack() {
        onMyPermissions(Ledger.withEntries(List.of(), List.of()));
        assertThat(element("myPermissions.generateReports.input")).isChecked();
        askToGiveUpGeneratingReports("Reports are someone else's");
        assertThat(element("myPermissions.request")).containsText(Words.of("ar", "permissionRequests.waiting"));
        assertThat(element("myPermissions.request")).containsText("Reports are someone else's");
        element("myPermissions.withdraw").click();
        assertThat(element("myPermissions.notice")).hasText(Words.of("ar", "permissionRequests.withdrawn"));
        assertThat(element("myPermissions.request")).isHidden();
    }

    @Test(description = "A request rejected with its reason is said rejected; one approved changes the permissions", testName = "4f9e1a27-6b3c-4d8e-a5f2-0c7b9d3e6a14",
            priority = 2)
    public void aRequestRejectedWithItsReasonIsSaidRejectedOneApprovedChangesThePermissions() {
        final @NotNull Page page = onMyPermissions(Ledger.withEntries(List.of(), List.of()));
        askToGiveUpGeneratingReports("Reports are someone else's");
        Web.openItem("requests");
        page.locator("[id^='permissionRequests.reject.']").click();
        element("permissionRequests.rejectReason").fill("Keep them for now");
        element("permissionRequests.rejectSend").click();
        assertThat(element("permissionRequests.empty")).isVisible();
        Web.openItem("myPermissions");
        assertThat(element("myPermissions.request")).containsText("Keep them for now");
        askToGiveUpGeneratingReports("Reports are someone else's");
        Web.openItem("requests");
        page.locator("[id^='permissionRequests.approve.']").click();
        assertThat(element("permissionRequests.empty")).isVisible();
        Web.openItem("myPermissions");
        assertThat(element("myPermissions.generateReports.input")).not().isChecked();
    }
}
