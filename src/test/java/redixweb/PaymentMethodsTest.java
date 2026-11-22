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

/** The web's Payment methods (redix-api #174, UC-014), on a new manager's project made through the API. */
public class PaymentMethodsTest {
    /** Payment methods of kept's project, signed in as its manager. */
    private static @NotNull Page onPaymentMethods(final @NotNull Ledger.Kept kept) {
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        Web.openItem("paymentMethods");
        assertThat(element("paymentMethods.title")).isVisible();
        return page;
    }

    /** Adds the method named name through New payment method, paying from every active account; its panel shows it. */
    private static void newMethod(final @NotNull String name) {
        element("paymentMethods.add").click();
        element("paymentMethod.name").fill(name);
        element("paymentMethod.save").click();
        assertThat(element("paymentMethod.title")).hasText(name);
    }

    /** The row of the method named name. */
    private static @NotNull Locator row(final @NotNull Page page, final @NotNull String name) {
        return page.locator("tr[id^='paymentMethods.row.']").filter(new Locator.FilterOptions().setHasText(name));
    }

    @Test(description = "New payment method, Rename, Disable and Enable, as the app's dialog and menu", testName = "6d3f9b2a-7e1c-4a8d-b5f4-0c2e8a6d9b13", priority = 1)
    public void newPaymentMethodRenameDisableAndEnableAsTheAppsDialogAndMenu() {
        final @NotNull Page page = onPaymentMethods(Ledger.withEntries(List.of(), List.of()));
        newMethod("Transfer");
        assertThat(element("paymentMethod.paysFrom")).containsText(Words.of("ar", "paymentMethods.paysFrom"));
        element("paymentMethods.rename").click();
        element("paymentMethod.name").fill("Bank transfer");
        element("paymentMethod.save").click();
        assertThat(element("paymentMethod.title")).hasText("Bank transfer");
        element("paymentMethods.disable").click();
        assertThat(element("paymentMethod.disabled")).hasText(Words.of("ar", "paymentMethods.disabled"));
        assertThat(row(page, "Bank transfer")).containsText(Words.of("ar", "paymentMethods.disabled"));
        element("paymentMethods.enable").click();
        assertThat(element("paymentMethods.disable")).isVisible();
    }

    @Test(description = "A payment method's Delete is asked first, and one an expense used is refused", testName = "a1c7e4f9-3b6d-4c2a-8e5b-9f0d2a7c4e68", priority = 2)
    public void aPaymentMethodsDeleteIsAskedFirstAndOneAnExpenseUsedIsRefused() {
        final @NotNull Page page = onPaymentMethods(Ledger.withEntries(List.of(100), List.of()));
        final @NotNull Locator used = page.locator("tr[id^='paymentMethods.row.']").first();
        newMethod("Cheque");
        row(page, "Cheque").click();
        assertThat(element("paymentMethod.title")).hasText("Cheque");
        element("paymentMethods.delete").click();
        assertThat(element("paymentMethod.deleteQuestion")).hasText(Words.of("ar", "swipe.question"));
        element("paymentMethod.deleteConfirm").click();
        assertThat(row(page, "Cheque")).isHidden();
        used.click();
        element("paymentMethods.delete").click();
        element("paymentMethod.deleteConfirm").click();
        assertThat(element("paymentMethod.refusal")).hasText(Words.of("ar", "paymentMethods.paymentMethodUsed"));
    }
}
