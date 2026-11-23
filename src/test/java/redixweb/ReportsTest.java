// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;
import redix.support.Ledger;
import redix.support.People;
import redix.support.Web;
import redix.support.Words;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static redix.support.Web.element;

/** The web's Reports, a side's item reports and an item's report (redix-api #171, UC-053, UC-050), on a new manager's project made through the API. */
public class ReportsTest {
    /** Reports of kept's project, signed in as its manager. */
    private static @NotNull Page onReports(final @NotNull Ledger.Kept kept) {
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        element("web.item.reports").click();
        assertThat(element("reports.title")).isVisible();
        return page;
    }

    @Test(description = "Issue report 1, asked first, lists report 1 and offers report 2", testName = "e9ba8cbe-e48c-47c3-b882-7efbb1123c01", priority = 1)
    public void issueReport1AskedFirstListsReport1AndOffersReport2() {
        onReports(Ledger.withEntries(List.of(100), List.of()));
        assertThat(element("reports.noneIssued")).hasText(Words.of("ar", "reports.noneIssued"));
        element("reports.issue").click();
        assertThat(element("reports.askQuestion")).isVisible();
        element("reports.issueConfirm").click();
        assertThat(element("reports.notice")).hasText(Words.of("ar", "reports.issued"));
        assertThat(element("reports.periodic.1")).isVisible();
        assertThat(element("reports.issue")).containsText("2");
    }

    @Test(description = "Expenses, from the side menu, opens the item reports, and an item opens its report with its payments", testName = "806982f0-58f0-492f-9ce8-3084fa7e4e9a",
            priority = 2)
    public void expensesOpensTheItemReportsAndAnItemOpensItsReportWithItsPayments() {
        final @NotNull Page page = onReports(Ledger.withEntries(List.of(100), List.of()));
        Web.openItem("expenses");
        assertThat(element("itemReports.total")).hasText("100.00");
        page.locator("[id^='itemReports.category.'][id$='.body']").filter(new Locator.FilterOptions().setHasText("Works")).click();
        page.locator("[id^='itemReports.item.']").filter(new Locator.FilterOptions().setHasText("Excavation")).first().click();
        assertThat(element("itemReport.title")).hasText("Excavation");
        assertThat(page.locator("[id^='itemReport.row.']").filter(new Locator.FilterOptions().setHasText("Payment 1")).first()).isVisible();
    }

    @Test(description = "Report 1, once issued, opens with its expenses against the previous report", testName = "fcace67f-7155-4843-b16e-3f459e2a804a", priority = 3)
    public void report1OnceIssuedOpensWithItsExpensesAgainstThePreviousReport() {
        onReports(Ledger.withEntries(List.of(100), List.of()));
        element("reports.issue").click();
        element("reports.issueConfirm").click();
        element("reports.periodic.1").click();
        assertThat(element("periodicReport.title")).containsText("1");
        assertThat(element("periodicReport.difference")).hasText("100.00");
    }

    @Test(description = "Report 1 hands its PDF and its Excel", testName = "d14c3940-84ef-4331-9b97-9e8ebaf0d228", priority = 4)
    public void report1HandsItsPdfAndItsExcel() {
        final @NotNull Page page = onReports(Ledger.withEntries(List.of(100), List.of()));
        element("reports.issue").click();
        element("reports.issueConfirm").click();
        element("reports.periodic.1").click();
        assertThat(element("periodicReport.title")).isVisible();
        final @NotNull Download pdf = page.waitForDownload(() -> element("periodicReport.file.pdf").click());
        Assert.assertEquals(pdf.suggestedFilename(), "report-1.pdf");
        final @NotNull Download excel = page.waitForDownload(() -> element("periodicReport.file.excel").click());
        Assert.assertEquals(excel.suggestedFilename(), "report-1.xlsx");
    }

    @Test(description = "Back on Expenses from an item's report, Expenses stays in front", testName = "117c7313-7171-4156-8402-64aee15b3f9e", priority = 5)
    public void backOnExpensesFromAnItemsReportExpensesStaysInFront() {
        final @NotNull Page page = onReports(Ledger.withEntries(List.of(100), List.of()));
        Web.openItem("expenses");
        page.locator("[id^='itemReports.category.'][id$='.body']").filter(new Locator.FilterOptions().setHasText("Works")).click();
        page.locator("[id^='itemReports.item.']").filter(new Locator.FilterOptions().setHasText("Excavation")).first().click();
        assertThat(element("itemReport.title")).hasText("Excavation");
        element("web.tab.expenses.title").click();
        assertThat(element("itemReports.total")).hasText("100.00");
        page.waitForTimeout(1000);
        assertThat(element("web.tab.expenses")).hasAttribute("aria-selected", "true");
        assertThat(element("itemReport.title")).not().isAttached();
    }

    @Test(description = "An item's report, after a reload and Expenses, still shows its item", testName = "c24ba704-3b4d-4460-af7f-50e080db5bc0", priority = 6)
    public void anItemsReportAfterAReloadAndExpensesStillShowsItsItem() {
        final @NotNull Page page = onReports(Ledger.withEntries(List.of(100), List.of()));
        Web.openItem("expenses");
        page.locator("[id^='itemReports.category.'][id$='.body']").filter(new Locator.FilterOptions().setHasText("Works")).click();
        page.locator("[id^='itemReports.item.']").filter(new Locator.FilterOptions().setHasText("Excavation")).first().click();
        assertThat(element("itemReport.title")).hasText("Excavation");
        page.reload();
        assertThat(element("itemReport.title")).hasText("Excavation");
        element("web.tab.expenses.title").click();
        assertThat(element("itemReports.total")).isVisible();
        element("web.tab.itemReport.title").click();
        assertThat(element("itemReport.title")).hasText("Excavation");
    }
}
