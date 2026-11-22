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

/** The web's Notifications (redix-api #175, UC-036), for a new manager whose project the admin approved through the API. */
public class NotificationsTest {
    @Test(description = "A project's approval shows in Notifications, counted in the top bar, and opening it brings the project's Home in front",
            testName = "7e3a1c5b-9d4f-4b2e-a6c8-2d0f5b9e3a71", priority = 1)
    public void aProjectsApprovalShowsInNotificationsCountedInTheTopBarAndOpeningItBringsTheProjectsHomeInFront() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(), List.of());
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        assertThat(element("web.notifications.count")).hasText("1");
        element("web.notifications").click();
        assertThat(element("notifications.title")).isVisible();
        final @NotNull Locator approval = page.locator("tr[id^='notifications.row.']").filter(new Locator.FilterOptions().setHasText(Words.of("ar", "notifications.projectApproved")));
        assertThat(approval).hasClass("unread");
        approval.click();
        assertThat(element("web.tab.home")).hasAttribute("aria-selected", "true");
        assertThat(element("home.title")).isVisible();
        assertThat(element("web.notifications.count")).isHidden();
    }
}
