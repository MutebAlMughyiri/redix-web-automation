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

/** The web's Categories and items (redix-api #174, UC-012), on a new manager's project made through the API. */
public class StructureTest {
    /** Categories and items of a new manager's project, signed in as its manager. */
    private static @NotNull Page onCategories() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(), List.of());
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        element("web.item.categories").click();
        assertThat(element("categories.title")).isVisible();
        return page;
    }

    /** Adds the category named name through New category; its panel shows it. */
    private static void newCategory(final @NotNull String name) {
        element("categories.add").click();
        element("category.name").fill(name);
        element("category.save").click();
        assertThat(element("category.title")).hasText(name);
    }

    /** Adds, from the category's panel in front, the item named name of type, through Add item; its panel shows it. */
    private static void addItem(final @NotNull Page page, final @NotNull String name, final @NotNull String type) {
        page.locator("[id^='categories.addItem.']").click();
        element("item.name").fill(name);
        element("categories.itemType." + type + ".input").check();
        element("item.save").click();
        assertThat(element("item.title")).hasText(name);
    }

    /** The row of the item named name. */
    private static @NotNull Locator itemRow(final @NotNull Page page, final @NotNull String name) {
        return page.locator("tr[id^='categories.item.']").filter(new Locator.FilterOptions().setHasText(name));
    }

    @Test(description = "New category, Add item and Rename work as the app's dialogs", testName = "a7e2c9d4-1b3f-4e6a-9c8d-2f5b7e0a1c36", priority = 1)
    public void newCategoryAddItemAndRenameWorkAsTheAppsDialogs() {
        final @NotNull Page page = onCategories();
        newCategory("Finishing");
        addItem(page, "Paint", "contractorPayments");
        assertThat(element("item.type")).hasText(Words.of("ar", "categories.typeContractorPayments"));
        assertThat(itemRow(page, "Paint")).isVisible();
        element("categories.renameItem").click();
        element("item.name").fill("Painting");
        element("item.save").click();
        assertThat(element("item.title")).hasText("Painting");
        assertThat(itemRow(page, "Painting")).isVisible();
    }

    @Test(description = "An item moved with Alt+Down, or dragged, keeps its new place", testName = "d4b8f1e3-6c2a-4b7d-8e9f-3a1c5d7b2e60", priority = 2)
    public void anItemMovedWithAltDownOrDraggedKeepsItsNewPlace() {
        final @NotNull Page page = onCategories();
        newCategory("Finishing");
        addItem(page, "Paint", "purchases");
        page.locator("tr[id^='categories.tile.']").filter(new Locator.FilterOptions().setHasText("Finishing")).click();
        addItem(page, "Tiles", "purchases");
        itemRow(page, "Paint").focus();
        page.keyboard().press("Alt+ArrowDown");
        assertThat(page.locator("tr[id^='categories.item.']").filter(new Locator.FilterOptions().setHasText("Tiles")).locator("xpath=following-sibling::tr[1]"))
                .containsText("Paint");
        element("web.item.categories").click();
        assertThat(itemRow(page, "Tiles").locator("xpath=following-sibling::tr[1]")).containsText("Paint");
        itemRow(page, "Paint").dragTo(itemRow(page, "Tiles"), new Locator.DragToOptions().setTargetPosition(10, 2));
        assertThat(itemRow(page, "Paint").locator("xpath=following-sibling::tr[1]")).containsText("Tiles");
        element("web.item.categories").click();
        assertThat(itemRow(page, "Paint").locator("xpath=following-sibling::tr[1]")).containsText("Tiles");
    }
}
