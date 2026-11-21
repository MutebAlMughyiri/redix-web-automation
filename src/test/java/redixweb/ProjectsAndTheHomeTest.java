// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;
import redix.support.People;
import redix.support.People.Person;
import redix.support.Web;
import redix.support.Words;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static redix.support.Web.element;

/**
 * Projects and the home on the web (redix-api #165, UC-049). A new person is in one project with no default and no entries, and holds no approval,
 * so these cases prove the list, the choice, the summary's shape and its folding; the figures with entries are proved by the API's Go tests.
 */
public class ProjectsAndTheHomeTest {
    /** Reads a label as the person sees it: Hide details holds both its words, and shows one. */
    private static final @NotNull LocatorAssertions.HasTextOptions SEEN = new LocatorAssertions.HasTextOptions().setUseInnerText(true);

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

    @Test(description = "An empty project says it has no entries, and Add entry stands gray", testName = "450b3eed-31a2-480d-8962-ce60d2d6c63a", priority = 2)
    public void anEmptyProjectSaysItHasNoEntriesAndAddEntryStandsGray() {
        final @NotNull String id = atHome(onTheList());
        assertThat(element("home.entriesEmpty." + id)).hasText(Words.of("ar", "entries.empty"));
        assertThat(element("home.addEntry." + id)).isDisabled();
        assertThat(element("summary.surplus")).containsText("0.00");
        assertThat(element("summary.pending")).hasCount(0);
    }

    @Test(description = "Revenue opens its categories, and opening Expenses closes it", testName = "34a45ba1-49fe-48b1-a437-e77cf592c9a8", priority = 3)
    public void revenueOpensItsCategoriesAndOpeningExpensesClosesIt() {
        atHome(onTheList());
        element("summary.revenue.body").click();
        assertThat(element("summary.revenue.card")).hasAttribute("open", "");
        element("summary.expenses.body").click();
        assertThat(element("summary.expenses.card")).hasAttribute("open", "");
        assertThat(element("summary.revenue.card")).not().hasAttribute("open", "");
    }

    @Test(description = "Hide details folds the summary, and a reload keeps it folded", testName = "9a7484ac-812b-4fd8-9c44-32a075f836fc", priority = 4)
    public void hideDetailsFoldsTheSummaryAndAReloadKeepsItFolded() {
        final @NotNull Page page = onTheList();
        atHome(page);
        final @NotNull Page.WaitForResponseOptions kept = new Page.WaitForResponseOptions();
        page.waitForResponse(response -> response.url().endsWith("/view"), kept, () -> element("home.collapse").click());
        assertThat(element("summary.card")).isHidden();
        assertThat(element("home.collapse")).hasText(Words.of("ar", "home.showDetails"), SEEN);
        page.reload();
        assertThat(element("summary.card")).isHidden();
        assertThat(element("home.collapse")).hasText(Words.of("ar", "home.showDetails"), SEEN);
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
