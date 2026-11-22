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

    @Test(description = "My email asks for the code sent to the new email, and a wrong code is refused with the app's words", testName = "8b4e1d7a-2c9f-4a6e-b3d5-7f0c2a9e6b48",
            priority = 2)
    public void myEmailAsksForTheCodeSentToTheNewEmailAndAWrongCodeIsRefusedWithTheAppsWords() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(), List.of());
        Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("web.item.settings").click();
        element("sharedSettings.changeEmail").click();
        assertThat(element("myEmail.email")).isFocused();
        final @NotNull String address = "new-" + kept.manager().username() + "@redix.test";
        element("myEmail.email").fill(address);
        element("myEmail.send").click();
        assertThat(element("myEmail.sentTo")).containsText(address);
        assertThat(element("myEmail.code")).isFocused();
        element("myEmail.code").fill("000000");
        element("myEmail.save").click();
        assertThat(element("myEmail.refusal")).hasText(Words.of("ar", "auth.codeWrong"));
    }
}
