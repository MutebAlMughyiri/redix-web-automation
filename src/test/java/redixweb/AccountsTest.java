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

    @Test(description = "New account adds a cash custody held by the manager, under the custodies", testName = "9c0dd5fd-1add-4130-87eb-fa29cb1e262f", priority = 3)
    public void newAccountAddsACashCustodyHeldByTheManagerUnderTheCustodies() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(), List.of());
        final @NotNull Page page = onAccounts(kept);
        element("accounts.add").click();
        element("accounts.accountName").fill("Site cash");
        element("accounts.type.cashCustody").click();
        element("account.save").click();
        assertThat(element("account.refusal")).hasText(Words.of("ar", "accounts.custodianRequired"));
        element("accounts.custodian").selectOption(new SelectOption().setLabel(kept.manager().displayName()));
        element("account.save").click();
        assertThat(element("account.title")).hasText("Site cash");
        assertThat(element("accounts.group.cashCustody")).hasText(Words.of("ar", "accounts.custodies"));
        assertThat(page.locator("[id^='accounts.row.']").filter(new Locator.FilterOptions().setHasText("Site cash"))).isVisible();
    }

    @Test(description = "Transfer opens once two accounts are active, and refuses more than the account holds", testName = "927b1cb7-398c-4f58-976b-6e4d2b14ecb8", priority = 4)
    public void transferOpensOnceTwoAccountsAreActiveAndRefusesMoreThanTheAccountHolds() {
        onAccounts(Ledger.withEntries(List.of(), List.of()));
        assertThat(element("accounts.transfer")).isDisabled();
        element("accounts.add").click();
        element("accounts.accountName").fill("Reserve");
        element("account.save").click();
        assertThat(element("account.title")).hasText("Reserve");
        element("accounts.transfer").click();
        assertThat(element("transfer.title")).hasText(Words.of("ar", "transfers.title"));
        element("transfer.amount").fill("100");
        element("transfer.save").click();
        assertThat(element("transfer.refusal")).hasText(Words.of("ar", "transfers.insufficientBalance"));
    }

    @Test(description = "Updating the actual balances keeps the count, and Accounts shows its total", testName = "db784129-4839-4483-b064-dc21898e6e4c", priority = 5)
    public void updatingTheActualBalancesKeepsTheCountAndAccountsShowsItsTotal() {
        final @NotNull Page page = onAccounts(Ledger.withEntries(List.of(100), List.of()));
        element("accounts.update").click();
        assertThat(element("reconcile.title")).hasText(Words.of("ar", "reconciliations.enterTitle"));
        final @NotNull Locator bankField = page.locator("[id^='reconcile.balance.'][name]");
        bankField.fill("-100");
        bankField.press("Tab");
        assertThat(element("reconcile.difference")).containsText("0.00");
        element("reconcile.save").click();
        assertThat(element("accounts.title")).isVisible();
        assertThat(element("reconciliation.actualTotal")).hasText("(100.00)");
    }
}
