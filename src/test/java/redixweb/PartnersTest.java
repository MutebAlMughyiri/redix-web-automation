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

/** The web's Partners and a partner in the details panel (redix-api #169, UC-016, UC-040), on a new manager's project made through the API. */
public class PartnersTest {
    /** Partners of kept's project, signed in as its manager. */
    private static @NotNull Page onPartners(final @NotNull Ledger.Kept kept) {
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        element("web.item.partners").click();
        assertThat(element("partners.title")).isVisible();
        return page;
    }

    @Test(description = "Partners lists the capital and a partner with no login, whose panel says no movements yet", testName = "4d8151a3-e15b-4e97-864e-3c05ebfa7633", priority = 1)
    public void partnersListsTheCapitalAndAPartnerWithNoLoginWhosePanelSaysNoMovementsYet() {
        final @NotNull Page page = onPartners(Ledger.withPartner("Partner A"));
        assertThat(element("partners.total")).hasText("0.00");
        final @NotNull Locator partner = page.locator("[id^='partners.row.']").filter(new Locator.FilterOptions().setHasText("Partner A"));
        assertThat(partner).containsText(Words.of("ar", "partners.noLogin"));
        partner.click();
        assertThat(element("partner.title")).hasText("Partner A");
        assertThat(element("partner.noMovements")).hasText(Words.of("ar", "capital.noMovements"));
    }

    @Test(description = "New partner adds a partner, and one with no capital is deleted, asked first", testName = "c764702a-cfc1-48c1-9831-d495a69ea3ab", priority = 2)
    public void newPartnerAddsAPartnerAndOneWithNoCapitalIsDeletedAskedFirst() {
        final @NotNull Page page = onPartners(Ledger.withPartner("Partner A"));
        element("partners.add").click();
        element("partners.partnerName").fill("Partner B");
        element("partner.save").click();
        assertThat(element("partner.title")).hasText("Partner B");
        final @NotNull Locator added = page.locator("[id^='partners.row.']").filter(new Locator.FilterOptions().setHasText("Partner B"));
        assertThat(added).isVisible();
        element("partners.delete").click();
        element("partner.deleteConfirm").click();
        assertThat(element("partner.title")).hasText(Words.of("ar", "partners.deleted"));
        assertThat(added).hasCount(0);
    }

    @Test(description = "The partner history tells a partner added from Partners", testName = "34a54463-dbdf-45d5-ba28-b5c9df491c0d", priority = 3)
    public void thePartnerHistoryTellsAPartnerAddedFromPartners() {
        final @NotNull Page page = onPartners(Ledger.withPartner("Partner A"));
        element("partners.add").click();
        element("partners.partnerName").fill("Partner B");
        element("partner.save").click();
        assertThat(element("partner.title")).hasText("Partner B");
        element("partners.history").click();
        assertThat(element("partnerHistory.title")).isVisible();
        assertThat(page.locator("[id^='partnerHistory.line.']").filter(new Locator.FilterOptions().setHasText("Partner B")).first()).isVisible();
    }
}
