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


import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static redix.support.Web.element;

public class WorkInTheShellTest {
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

    @Test(description = "Settings opens in a tab from My account, and no side-menu item stands gray", testName = "03cdbc0f-a1f7-4872-b54b-0d962f91913f",
            priority = 1)
    public void settingsOpensInATabFromMyAccountAndNoSideMenuItemStandsGray() {
        final @NotNull Page page = signedIn();
        assertThat(element("web.tab.home")).hasAttribute("aria-selected", "true");
        Web.openAccount("web.sharedSettings");
        assertThat(element("web.tab.settings")).hasAttribute("aria-selected", "true");
        assertThat(element("sharedSettings.title")).hasText(Words.of("ar", "sharedSettings.title"));
        assertThat(element("web.item.settings")).hasCount(0);
        assertThat(page.locator("#web\\.tree [aria-disabled='true']")).hasCount(0);
        assertThat(element("web.item.documents")).hasAttribute("hx-get", "/tab/documents");
    }

    @Test(description = "Closing the last tab asks to pick from the side menu", testName = "90259f56-671c-4054-88f6-cfddafcefd62", priority = 2)
    public void closingTheLastTabAsksToPickFromTheSideMenu() {
        signedIn();
        Web.openAccount("web.sharedSettings");
        assertThat(element("sharedSettings.title")).isVisible();
        element("web.tab.settings.close").click();
        assertThat(element("web.tab.settings")).hasCount(0);
        assertThat(element("web.tab.home")).hasAttribute("aria-selected", "true");
        element("web.tab.home.close").click();
        assertThat(element("web.tab.home")).hasCount(0);
        assertThat(element("web.pickFromMenu")).hasText(Words.of("ar", "web.pickFromMenu"));
        assertThat(element("web.item.home")).hasAttribute("aria-current", "false");
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

    @Test(description = "The View menu hides the top bar, and the status bar, which holds My account, stays", testName = "31603eea-62cd-4ae3-90a5-5369f9dd6a6a",
            priority = 5)
    public void theViewMenuHidesTheTopBarAndTheStatusBarWhichHoldsMyAccountStays() {
        signedIn();
        element("web.view").click();
        assertThat(element("web.viewMenu")).isVisible();
        assertThat(element("web.view.statusBar")).hasCount(0);
        element("web.view.topBar").click();
        assertThat(element("web.topBar")).isHidden();
        assertThat(element("web.statusBar")).isVisible();
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

    @Test(description = "A reload keeps the top bar, the side menu, its width and the open tab", testName = "7da7c1ee-7523-4db9-82dd-846d0dfef39c", priority = 7)
    public void aReloadKeepsTheTopBarTheSideMenuItsWidthAndTheOpenTab() {
        final @NotNull Page page = signedIn();
        Web.openAccount("web.sharedSettings");
        page.waitForResponse(saved -> saved.url().endsWith("/view"), () -> dragTheLine(page, 52));
        final int width = sideMenuWidth();
        element("web.view").click();
        page.waitForResponse(saved -> saved.url().endsWith("/view"), () -> element("web.view.topBar").click());
        page.keyboard().press("Escape");
        page.reload();
        assertThat(element("web.topBar")).isHidden();
        assertThat(element("web.statusBar")).isVisible();
        assertThat(element("web.tab.settings")).hasAttribute("aria-selected", "true");
        assertThat(element("sharedSettings.title")).isVisible();
        assertTrue(Math.abs(sideMenuWidth() - width) <= 1, "the width is kept: " + sideMenuWidth() + " for " + width);
    }

    @Test(description = "The language is chosen in Settings, not in the top bar", testName = "adc698ce-41cb-4b89-8c89-6a4d51b0a772", priority = 8)
    public void theLanguageIsChosenInSettingsNotInTheTopBar() {
        final @NotNull Page page = signedIn();
        final @NotNull Locator topBar = element("web.topBar");
        assertThat(topBar.locator("a[href^='/language/']")).hasCount(0);
        Web.openAccount("web.sharedSettings");
        assertThat(element("myAccount.language")).hasText(Words.of("ar", "myAccount.language"));
        element("myAccount.english").click();
        assertThat(page.locator("html")).hasAttribute("dir", "ltr");
        assertThat(element("sharedSettings.title")).hasText(Words.of("en", "sharedSettings.title"));
        assertThat(element("web.fold")).hasText("«");
    }

    @Test(description = "The page never scrolls", testName = "fff489e1-517b-4d6b-a81e-f8792ce3e9b5", priority = 9)
    public void thePageNeverScrolls() {
        final @NotNull Page page = signedIn();
        Web.openAccount("web.sharedSettings");
        assertFalse((Boolean) page.evaluate("() => document.documentElement.scrollHeight > innerHeight || document.documentElement.scrollWidth > innerWidth"),
                "at a laptop's width");
        page.setViewportSize(390, 760);
        assertFalse((Boolean) page.evaluate("() => document.documentElement.scrollHeight > innerHeight || document.documentElement.scrollWidth > innerWidth"),
                "at a phone's width");
    }

    @Test(description = "My account in the status bar opens My history, Shared settings and Sign out", testName = "fcbd40c5-62bd-4511-8b65-12a298695356",
            priority = 10)
    public void myAccountInTheStatusBarOpensMyHistorySharedSettingsAndSignOut() {
        signedIn();
        assertThat(element("web.topBar").locator("[id='myAccount.signOut']")).hasCount(0);
        assertThat(element("web.statusBar").locator("[id='appVersion.line']")).hasCount(0);
        assertThat(element("web.accountMenu")).isHidden();
        element("web.account").click();
        assertThat(element("web.accountMenu")).isVisible();
        final @NotNull BoundingBox menu = element("web.accountMenu").boundingBox(), account = element("web.account").boundingBox();
        assertTrue(menu.y + menu.height <= account.y + 1 && Math.abs(menu.x - account.x) < 200, "My account opens above the person's name: " + menu.y + " for " + account.y);
        assertThat(element("web.accountMenu.name")).hasText(element("web.account.name").textContent());
        assertThat(element("web.myHistory")).hasText(Words.of("ar", "history.myTitle"));
        assertThat(element("web.sharedSettings")).hasText(Words.of("ar", "sharedSettings.title"));
        element("web.myHistory").click();
        assertThat(element("myHistory.title")).isVisible();
        assertThat(element("web.accountMenu")).isHidden();
        Web.openAccount("myAccount.signOut");
        assertThat(element("signIn.username")).isVisible();
    }

    @Test(description = "More folds the side menu's items used now and then until it is clicked, and opens with them when one is in front",
            testName = "3fe0ba62-3eec-4700-b2aa-47112660bcf5", priority = 11)
    public void moreFoldsTheSideMenusItemsUsedNowAndThenUntilItIsClicked() {
        final @NotNull Page page = signedIn();
        assertThat(element("web.item.documents")).isHidden();
        element("web.more.toggle").click();
        assertThat(element("web.item.documents")).isVisible();
        element("web.item.myPermissions").click();
        assertThat(element("web.tab.myPermissions")).hasAttribute("aria-selected", "true");
        page.reload();
        assertThat(element("web.more")).hasAttribute("open", "");
        assertThat(element("web.item.myPermissions")).hasAttribute("aria-current", "true");
    }

    @Test(description = "Alt+W closes the tab in front", testName = "6e608b7c-5c32-44ad-af97-c53edbd6f5c1", priority = 12)
    public void altWClosesTheTabInFront() {
        final @NotNull Page page = signedIn();
        Web.openAccount("web.sharedSettings");
        assertThat(element("web.tab.settings")).hasAttribute("aria-selected", "true");
        page.keyboard().press("Alt+KeyW");
        assertThat(element("web.tab.settings")).hasCount(0);
        assertThat(element("web.tab.home")).hasAttribute("aria-selected", "true");
    }
}
