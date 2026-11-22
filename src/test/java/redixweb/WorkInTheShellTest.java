// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Mouse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;
import redix.support.People;
import redix.support.People.Person;
import redix.support.Web;
import redix.support.Words;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static redix.support.Web.element;

public class WorkInTheShellTest {
    /** The side menu's items no feature has built yet, which stand gray (#161 R15). */
    private static final @NotNull List<String> NOT_BUILT = List.of("history");

    /** A page signed in as a new person, the shell as a first visit leaves it. */
    private static @NotNull Page signedIn() {
        final @NotNull Person person = People.signedUp();
        final @NotNull Page page = Web.page();
        Web.signIn(person.username(), People.PASSWORD);
        return page;
    }

    /** The side menu's width on screen, in whole pixels; 0 or 1 once it is folded. */
    private static int sideMenuWidth() {
        return ((Number) element("web.sideMenu").evaluate("side => Math.round(side.getBoundingClientRect().width)")).intValue();
    }

    /** Drags the side menu's line by pixels toward the content, to the left in Arabic: a positive number widens the menu, a negative narrows it. */
    private static void dragTheLine(final @NotNull Page page, final int pixels) {
        final @NotNull BoundingBox line = element("web.sideLine").boundingBox();
        final double x = line.x + line.width / 2, y = line.y + 200;
        page.mouse().move(x, y);
        page.mouse().down();
        page.mouse().move(x - pixels, y, new Mouse.MoveOptions().setSteps(8));
        page.mouse().up();
    }

    @Test(description = "Settings opens in a tab, and the items not built stand gray", testName = "03cdbc0f-a1f7-4872-b54b-0d962f91913f", priority = 1)
    public void settingsOpensInATabAndTheItemsNotBuiltStandGray() {
        signedIn();
        assertThat(element("web.tab.home")).hasAttribute("aria-selected", "true");
        element("web.item.settings").click();
        assertThat(element("web.tab.settings")).hasAttribute("aria-selected", "true");
        assertThat(element("sharedSettings.title")).hasText(Words.of("ar", "sharedSettings.title"));
        assertThat(element("web.item.settings")).hasAttribute("aria-current", "true");
        for (final @NotNull String item : NOT_BUILT) {
            assertThat(element("web.item." + item)).hasAttribute("aria-disabled", "true");
        }
    }

    @Test(description = "Closing the last tab asks to pick from the side menu", testName = "90259f56-671c-4054-88f6-cfddafcefd62", priority = 2)
    public void closingTheLastTabAsksToPickFromTheSideMenu() {
        signedIn();
        element("web.item.settings").click();
        assertThat(element("sharedSettings.title")).isVisible();
        element("web.tab.settings.close").click();
        assertThat(element("web.tab.settings")).hasCount(0);
        assertThat(element("web.tab.home")).hasAttribute("aria-selected", "true");
        element("web.tab.home.close").click();
        assertThat(element("web.tab.home")).hasCount(0);
        assertThat(element("web.pickFromMenu")).hasText(Words.of("ar", "web.pickFromMenu"));
        assertThat(element("web.item.settings")).hasAttribute("aria-current", "false");
    }

    @Test(description = "The side menu folds with its arrow and comes back with the other", testName = "3d98276a-60dc-46b4-a5da-e3d7b16cc65f", priority = 3)
    public void theSideMenuFoldsWithItsArrowAndComesBackWithTheOther() {
        signedIn();
        assertThat(element("web.fold")).hasText("»");
        element("web.fold").click();
        assertThat(element("web.unfold")).isVisible();
        Web.sideMenuSettled(true);
        assertTrue(sideMenuWidth() <= 1, "folded, the side menu takes no room");
        element("web.unfold").click();
        assertThat(element("web.unfold")).isHidden();
        Web.sideMenuSettled(false);
        assertEquals(sideMenuWidth(), 248, "unfolded, the side menu is back at its width");
    }

    @Test(description = "Alt+1 folds and unfolds the side menu", testName = "d2af2916-06cd-49bc-afb5-9a53a97c2d54", priority = 4)
    public void alt1FoldsAndUnfoldsTheSideMenu() {
        final @NotNull Page page = signedIn();
        page.keyboard().press("Alt+1");
        assertThat(element("web.unfold")).isVisible();
        assertThat(element("web.view.sideMenu")).not().isChecked();
        Web.sideMenuSettled(true);
        page.keyboard().press("Alt+1");
        assertThat(element("web.unfold")).isHidden();
        assertThat(element("web.view.sideMenu")).isChecked();
        Web.sideMenuSettled(false);
    }

    @Test(description = "The View menu hides the top bar and the status bar", testName = "31603eea-62cd-4ae3-90a5-5369f9dd6a6a", priority = 5)
    public void theViewMenuHidesTheTopBarAndTheStatusBar() {
        signedIn();
        element("web.view").click();
        assertThat(element("web.viewMenu")).isVisible();
        element("web.view.topBar").click();
        element("web.view.statusBar").click();
        assertThat(element("web.topBar")).isHidden();
        assertThat(element("web.statusBar")).isHidden();
        element("web.view.topBar").click();
        assertThat(element("web.topBar")).isVisible();
    }

    @Test(description = "The side menu's line drags wider and narrower within its limits", testName = "55563c48-3b35-451e-be1d-0d973c3eb982", priority = 6)
    public void theSideMenusLineDragsWiderAndNarrowerWithinItsLimits() {
        final @NotNull Page page = signedIn();
        final int before = sideMenuWidth();
        dragTheLine(page, 100);
        assertTrue(Math.abs(sideMenuWidth() - (before + 100)) <= 4, "100 pixels toward the content widen it by 100: " + sideMenuWidth());
        dragTheLine(page, 2000);
        assertEquals(sideMenuWidth(), 480, "no wider than 480");
        dragTheLine(page, -2000);
        assertEquals(sideMenuWidth(), 180, "no narrower than 180");
    }

    @Test(description = "A reload keeps the bars, the side menu, its width and the open tab", testName = "7da7c1ee-7523-4db9-82dd-846d0dfef39c", priority = 7)
    public void aReloadKeepsTheBarsTheSideMenuItsWidthAndTheOpenTab() {
        final @NotNull Page page = signedIn();
        element("web.item.settings").click();
        page.waitForResponse(saved -> saved.url().endsWith("/view"), () -> dragTheLine(page, 52));
        final int width = sideMenuWidth();
        element("web.view").click();
        page.waitForResponse(saved -> saved.url().endsWith("/view"), () -> element("web.view.statusBar").click());
        page.keyboard().press("Escape");
        page.reload();
        assertThat(element("web.statusBar")).isHidden();
        assertThat(element("web.topBar")).isVisible();
        assertThat(element("web.tab.settings")).hasAttribute("aria-selected", "true");
        assertThat(element("sharedSettings.title")).isVisible();
        assertTrue(Math.abs(sideMenuWidth() - width) <= 1, "the width is kept: " + sideMenuWidth() + " for " + width);
    }

    @Test(description = "The language is chosen in Settings, not in the top bar", testName = "adc698ce-41cb-4b89-8c89-6a4d51b0a772", priority = 8)
    public void theLanguageIsChosenInSettingsNotInTheTopBar() {
        final @NotNull Page page = signedIn();
        final @NotNull Locator topBar = element("web.topBar");
        assertThat(topBar.locator("a[href^='/language/']")).hasCount(0);
        element("web.item.settings").click();
        assertThat(element("myAccount.language")).hasText(Words.of("ar", "myAccount.language"));
        element("myAccount.english").click();
        assertThat(page.locator("html")).hasAttribute("dir", "ltr");
        assertThat(element("sharedSettings.title")).hasText(Words.of("en", "sharedSettings.title"));
        assertThat(element("web.fold")).hasText("«");
    }

    @Test(description = "The page never scrolls", testName = "fff489e1-517b-4d6b-a81e-f8792ce3e9b5", priority = 9)
    public void thePageNeverScrolls() {
        final @NotNull Page page = signedIn();
        element("web.item.settings").click();
        assertFalse((Boolean) page.evaluate("() => document.documentElement.scrollHeight > innerHeight || document.documentElement.scrollWidth > innerWidth"),
                "at a laptop's width");
        page.setViewportSize(390, 760);
        assertFalse((Boolean) page.evaluate("() => document.documentElement.scrollHeight > innerHeight || document.documentElement.scrollWidth > innerWidth"),
                "at a phone's width");
    }
}
