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

    /** Columns of the item Excavation, from its panel. */
    private static void onExcavationColumns(final @NotNull Page page) {
        itemRow(page, "Excavation").click();
        page.locator("[id^='categories.itemColumns.']").click();
        assertThat(element("columns.title")).containsText("Excavation");
    }

    @Test(description = "New column on this item, Edit, and Delete asked first, as the app's dialogs", testName = "5e1c8a3f-2d7b-4f9e-b6a4-8c0d3e7f1a52", priority = 3)
    public void newColumnOnThisItemEditAndDeleteAskedFirstAsTheAppsDialogs() {
        final @NotNull Page page = onCategories();
        onExcavationColumns(page);
        element("columns.add").click();
        element("columns.columnName").fill("Notes");
        element("column.save").click();
        assertThat(element("column.title")).hasText("Notes");
        element("columns.edit").click();
        element("columns.columnName").fill("Remarks");
        element("column.save").click();
        assertThat(element("column.title")).hasText("Remarks");
        final @NotNull Locator row = page.locator("tr[id^='columns.row.']").filter(new Locator.FilterOptions().setHasText("Remarks"));
        assertThat(row).isVisible();
        element("columns.delete").click();
        assertThat(element("column.deleteQuestion")).hasText(Words.of("ar", "swipe.question"));
        element("column.deleteConfirm").click();
        assertThat(row).isHidden();
    }

    @Test(description = "A template's new column asks whether it reaches every item", testName = "b9f4d2e7-3a6c-4e1b-8d5f-6a2c9e0b7d18", priority = 4)
    public void aTemplatesNewColumnAsksWhetherItReachesEveryItem() {
        final @NotNull Page page = onCategories();
        onExcavationColumns(page);
        element("columns.template").click();
        assertThat(element("columns.template")).hasAttribute("aria-pressed", "true");
        element("columns.add").click();
        element("columns.columnName").fill("Order number");
        element("applyToAll.apply").click();
        assertThat(element("column.title")).hasText("Order number");
        element("columns.thisItem").click();
        assertThat(page.locator("tr[id^='columns.row.']").filter(new Locator.FilterOptions().setHasText("Order number"))).isVisible();
    }

    @Test(description = "New subgroup, Rename, a table moved with Alt+Up, and Delete asked first, on an item's Tables", testName = "3c7a9e15-8b2d-4f6c-a1e9-5d0b7f2c4e83",
            priority = 5)
    public void newSubgroupRenameATableMovedWithAltUpAndDeleteAskedFirstOnAnItemsTables() {
        final @NotNull Page page = onCategories();
        itemRow(page, "Excavation").click();
        element("categories.tables").click();
        assertThat(element("tables.title")).containsText("Excavation");
        element("tables.add").click();
        element("table.name").fill("Ground floor");
        element("table.save").click();
        assertThat(element("table.title")).hasText("Ground floor");
        element("tables.rename").click();
        element("table.name").fill("Basement");
        element("table.save").click();
        assertThat(element("table.title")).hasText("Basement");
        final @NotNull Locator rows = page.locator("tr[id^='tables.row.']");
        final @NotNull Locator basement = rows.filter(new Locator.FilterOptions().setHasText("Basement"));
        basement.focus();
        page.keyboard().press("Alt+ArrowUp");
        assertThat(rows.first()).containsText("Basement");
        page.locator("[id='web.tab.itemTables.title']").click();
        assertThat(rows.first()).containsText("Basement");
        basement.click();
        element("tables.delete").click();
        assertThat(element("table.deleteQuestion")).hasText(Words.of("ar", "swipe.question"));
        element("table.deleteConfirm").click();
        assertThat(basement).isHidden();
    }
}
