// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

import com.microsoft.playwright.Page;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;
import redix.support.Ledger;
import redix.support.People;
import redix.support.Web;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static redix.support.Web.element;

/** The web's Settings (redix-api #175), for a new manager whose project was made through the API. */
public class SettingsTest {
    @Test(description = "Settings keeps the appearance chosen and how many entries the home lists", testName = "2d9f6b1e-8a3c-4f5d-b7e2-6c4a0e8d1f95", priority = 1)
    public void settingsKeepsTheAppearanceChosenAndHowManyEntriesTheHomeLists() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(), List.of());
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("web.item.settings").click();
        assertThat(element("sharedSettings.theme.system")).hasAttribute("aria-current", "true");
        element("sharedSettings.theme.dark").click();
        assertThat(page.locator("html")).hasAttribute("data-theme", "dark");
        element("web.item.settings").click();
        assertThat(element("sharedSettings.theme.dark")).hasAttribute("aria-current", "true");
        element("sharedSettings.entriesShownChoice").selectOption("8");
        assertThat(element("sharedSettings.entriesShownOption.8")).hasAttribute("selected", "");
        element("sharedSettings.theme.system").click();
        assertThat(page.locator("html")).not().hasAttribute("data-theme", "dark");
    }
}
