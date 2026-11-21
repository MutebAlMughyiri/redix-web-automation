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

/** The web's Suppliers and a supplier in the details panel (redix-api #170, UC-017), on a new manager's project made through the API. */
public class SuppliersTest {
    /** Suppliers of kept's project, signed in as its manager. */
    private static @NotNull Page onSuppliers(final @NotNull Ledger.Kept kept) {
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        element("web.item.suppliers").click();
        assertThat(element("suppliers.title")).isVisible();
        return page;
    }

    /** The row of the supplier named name. */
    private static @NotNull Locator row(final @NotNull Page page, final @NotNull String name) {
        return page.locator("[id^='suppliers.row.']").filter(new Locator.FilterOptions().setHasText(name));
    }

    @Test(description = "Suppliers lists the general cash supplier, and New supplier adds one with its VAT number", testName = "9fef25bd-7fbe-43b0-bcb4-6b05932c2173", priority = 1)
    public void suppliersListsTheGeneralCashSupplierAndNewSupplierAddsOneWithItsVatNumber() {
        final @NotNull Page page = onSuppliers(Ledger.withEntries(List.of(), List.of()));
        assertThat(row(page, "مورد نقدي عام")).isVisible();
        element("suppliers.add").click();
        element("suppliers.supplierName").fill("Supplier B");
        element("suppliers.vatNumber").fill("300123");
        element("supplier.save").click();
        assertThat(element("supplier.title")).hasText("Supplier B");
        assertThat(row(page, "Supplier B")).containsText("300123");
    }

    @Test(description = "A supplier disabled from its panel stays in Suppliers, said disabled, and Enable brings it back", testName = "c103f30c-fc58-44bc-b8b7-8643b9791acc", priority = 2)
    public void aSupplierDisabledFromItsPanelStaysInSuppliersSaidDisabledAndEnableBringsItBack() {
        final @NotNull Page page = onSuppliers(Ledger.withContract(100));
        row(page, "Contractor").click();
        assertThat(element("supplier.title")).hasText("Contractor");
        element("suppliers.disable").click();
        assertThat(row(page, "Contractor")).containsText(Words.of("ar", "suppliers.disabled"));
        element("suppliers.enable").click();
        assertThat(element("suppliers.disable")).isVisible();
    }
}
