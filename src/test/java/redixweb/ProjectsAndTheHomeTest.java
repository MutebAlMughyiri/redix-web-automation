// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;
import redix.support.People;
import redix.support.People.Person;
import redix.support.Web;
import redix.support.Words;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static redix.support.Web.element;

/**
 * Projects and the home on the web (redix-api #165, UC-049). A new person is in one project with no default and no entries, and holds no approval,
 * so these cases prove the list, the choice and the summary's shape; the figures with entries are proved by the API's Go tests.
 */
public class ProjectsAndTheHomeTest {
    /** A page signed in as a new person, on the project list a first visit lands on. */
    private static @NotNull Page onTheList() {
        final @NotNull Person person = People.signedUp();
        final @NotNull Page page = Web.page();
        Web.signIn(person.username(), People.PASSWORD);
        assertThat(element("projects.title")).hasText(Words.of("ar", "projects.choose"));
        return page;
    }

    /** The first project the list offers. */
    private static @NotNull Locator firstChoice(final @NotNull Page page) {
        return page.locator("[id^='projects.card.']").first();
    }

    /** Signs a new person in and opens their project's home from the list; it is the project's id. */
    private static @NotNull String atHome(final @NotNull Page page) {
        final @NotNull String id = firstChoice(page).getAttribute("id").substring("projects.card.".length());
        firstChoice(page).click();
        assertThat(element("home.title")).isVisible();
        return id;
    }

    @Test(description = "With no default project, the home lists my projects and choosing one opens its home", testName = "b5123465-502b-407e-83db-c0917f9e95b2", priority = 1)
    public void withNoDefaultProjectTheHomeListsMyProjectsAndChoosingOneOpensItsHome() {
        final @NotNull Page page = onTheList();
        assertThat(element("web.project")).containsText(Words.of("ar", "projects.choose"));
        final @NotNull String name = firstChoice(page).locator("span").first().textContent();
        atHome(page);
        assertThat(element("home.title")).hasText(name);
        assertThat(element("web.project")).containsText(name);
        assertThat(element("web.sideProject")).hasText(name);
        assertThat(element("web.tab.home")).hasAttribute("aria-selected", "true");
    }

    @Test(description = "The home lists no entries, and Add entry stands gray for whoever may not enter them", testName = "450b3eed-31a2-480d-8962-ce60d2d6c63a",
            priority = 2)
    public void theHomeListsNoEntriesAndAddEntryStandsGrayForWhoeverMayNotEnterThem() {
        final @NotNull Page page = onTheList();
        final @NotNull String id = atHome(page);
        assertThat(page.locator("[id^='entries.row.'], [id^='home.entriesEmpty.']")).hasCount(0);
        assertThat(element("home.addEntry." + id)).isDisabled();
        assertThat(element("summary.surplus")).containsText("0.00");
        assertThat(element("summary.pending")).hasCount(0);
    }

    @Test(description = "Revenue and Expenses show their categories side by side, and nothing folds", testName = "34a45ba1-49fe-48b1-a437-e77cf592c9a8", priority = 3)
    public void revenueAndExpensesShowTheirCategoriesSideBySideAndNothingFolds() {
        atHome(onTheList());
        assertThat(element("summary.revenue.shares")).isVisible();
        assertThat(element("summary.expenses.shares")).isVisible();
        element("summary.revenue.body").click();
        assertThat(element("summary.expenses.shares")).isVisible();
        assertThat(element("summary.card").locator("details")).hasCount(0);
        final @NotNull BoundingBox page = element("home.page").boundingBox(), content = element("web.content").boundingBox();
        Assert.assertTrue(Math.abs((page.x + page.width / 2) - (content.x + content.width / 2)) < 20, "the home stands in the middle of the page");
    }

    @Test(description = "The home shows the summary always, with no Hide details", testName = "9a7484ac-812b-4fd8-9c44-32a075f836fc", priority = 4)
    public void theHomeShowsTheSummaryAlwaysWithNoHideDetails() {
        final @NotNull Page page = onTheList();
        atHome(page);
        assertThat(element("summary.card")).isVisible();
        assertThat(element("home.collapse")).hasCount(0);
        page.reload();
        assertThat(element("summary.card")).isVisible();
    }

    @Test(description = "A tab's buttons stand at its head's far end, away from its title", testName = "03e67e78-2aa2-48e3-a17f-21ba8d4a7625", priority = 6)
    public void aTabsButtonsStandAtItsHeadsFarEndAwayFromItsTitle() {
        final @NotNull Page page = onTheList();
        atHome(page);
        element("web.item.entries").click();
        assertThat(element("entries.title")).isVisible();
        final @NotNull BoundingBox title = element("entries.title").boundingBox(), actions = element("entries.actions").boundingBox(),
                content = element("web.content").boundingBox();
        Assert.assertTrue(actions.x + actions.width < title.x, "in Arabic the buttons stand left of the title");
        Assert.assertTrue(actions.x - content.x < 40, "at the head's far end: " + (actions.x - content.x));
    }

    @Test(description = "The project button lists my projects and opens one's home", testName = "6755d992-2ec1-4db7-9217-b7a0515070be", priority = 5)
    public void theProjectButtonListsMyProjectsAndOpensOnesHome() {
        final @NotNull Page page = onTheList();
        final @NotNull String id = firstChoice(page).getAttribute("id").substring("projects.card.".length());
        element("web.project").click();
        assertThat(element("web.projects")).isVisible();
        element("web.projects." + id).click();
        assertThat(element("home.title")).isVisible();
        assertThat(element("home.addEntry." + id)).isVisible();
    }
}
