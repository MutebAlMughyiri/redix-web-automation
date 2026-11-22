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

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static redix.support.Web.element;

/** The web's Contracts and a contract in the details panel (redix-api #170, UC-020), on a new manager's project made through the API. */
public class ContractsTest {
    /** Contracts of kept's project, signed in as its manager. */
    private static @NotNull Page onContracts(final @NotNull Ledger.Kept kept) {
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        element("web.item.contracts").click();
        assertThat(element("contracts.title")).isVisible();
        return page;
    }

    /** The row of the contract with Contractor, the only one the ledger makes. */
    private static @NotNull Locator contractor(final @NotNull Page page) {
        return page.locator("[id^='contracts.row.']").filter(new Locator.FilterOptions().setHasText("Contractor"));
    }

    @Test(description = "Contracts lists a contract with its value, paid and remaining, and its panel says it has not started", testName = "c235adad-421a-47a6-b19e-887ded11c894", priority = 1)
    public void contractsListsAContractWithItsValuePaidAndRemainingAndItsPanelSaysItHasNotStarted() {
        final @NotNull Page page = onContracts(Ledger.withContract(100));
        final @NotNull Locator row = contractor(page);
        assertThat(row.locator("[id^='contracts.valueOf.']")).hasText("100.00");
        assertThat(row.locator("[id^='contracts.paid.']")).hasText("0.00");
        assertThat(row.locator("[id^='contracts.remaining.']")).hasText("100.00");
        row.click();
        assertThat(element("contract.title")).hasText("Contractor");
        assertThat(element("contract.standing")).hasText(Words.of("ar", "contracts.notStarted"));
    }

    @Test(description = "A contract disabled from its panel stays in Contracts, said disabled, and Enable brings it back", testName = "426e87ca-83b7-4db0-8fc7-8e42b49fe307", priority = 2)
    public void aContractDisabledFromItsPanelStaysInContractsSaidDisabledAndEnableBringsItBack() {
        final @NotNull Page page = onContracts(Ledger.withContract(100));
        contractor(page).click();
        element("contracts.disable").click();
        assertThat(contractor(page)).containsText(Words.of("ar", "contracts.disabled"));
        element("contracts.enable").click();
        assertThat(element("contracts.disable")).isVisible();
    }
}
