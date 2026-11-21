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
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static redix.support.Web.element;

/** The web's Accounts and an account in the details panel (redix-api #168, UC-015), on a new manager's project made through the API. */
public class AccountsTest {
    /** Accounts of kept's project, signed in as its manager. */
    private static @NotNull Page onAccounts(final @NotNull Ledger.Kept kept) {
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        element("web.item.accounts").click();
        assertThat(element("accounts.title")).isVisible();
        return page;
    }

    /** The row of the project's bank account, which the ledger calls Bank. */
    private static @NotNull Locator bank(final @NotNull Page page) {
        return page.locator("[id^='accounts.row.']").filter(new Locator.FilterOptions().setHasText("Bank"));
    }

    @Test(description = "Accounts lists the bank account with its balance, and says nothing was counted yet", testName = "6a1f9055-f037-4b60-88a7-827a45edad45", priority = 1)
    public void accountsListsTheBankAccountWithItsBalanceAndSaysNothingWasCountedYet() {
        final @NotNull Page page = onAccounts(Ledger.withEntries(List.of(), List.of()));
        assertThat(element("accounts.group.bank")).hasText(Words.of("ar", "accounts.banks"));
        assertThat(bank(page)).isVisible();
        assertThat(element("reconciliation.never")).hasText(Words.of("ar", "reconciliations.never"));
    }

    @Test(description = "An account in use is disabled and enabled again, and cannot be deleted", testName = "8c15d310-e393-4056-9406-0857a6ddf6e5", priority = 2)
    public void anAccountInUseIsDisabledAndEnabledAgainAndCannotBeDeleted() {
        final @NotNull Page page = onAccounts(Ledger.withEntries(List.of(100), List.of()));
        bank(page).click();
        assertThat(element("accounts.delete")).isDisabled();
        element("accounts.disable").click();
        assertThat(bank(page)).hasClass(Pattern.compile("disabled"));
        element("accounts.enable").click();
        assertThat(bank(page)).not().hasClass(Pattern.compile("disabled"));
    }
}
