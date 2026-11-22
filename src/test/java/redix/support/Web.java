// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redix.support;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * The REDIX web every case opens (redix-api #161): its address, one Chrome for the run, and a fresh page per case holding no cookie of another's.
 * Elements are found by their id, which getByTestId reads here: the app's key names where the app has the same element (signIn.username), the
 * web's own otherwise (web.fold).
 */
public final class Web {
    /** The web's address: REDIX_WEB_URL, or the Dev server's web on this machine, never qa. */
    public static final @NotNull String URL = System.getenv().getOrDefault("REDIX_WEB_URL", "http://127.0.0.1:5020");

    /** The window a case opens, a laptop's: wide enough for the side menu, the content and a panel. */
    public static final int WIDTH = 1366, HEIGHT = 700;

    private static @NotNull Playwright playwright = Playwright.create(new Playwright.CreateOptions().setEnv(driverEnvironment()));
    private static @NotNull Browser browser = launched();
    private static @NotNull BrowserContext context = browser.newContext();
    private static @NotNull Page page = context.newPage();

    private Web() {
    }

    /** A fresh page on the web, in Arabic as the browser asks, at the web's first page. */
    public static @NotNull Page page() {
        return page("ar");
    }

    /** A fresh page on the web, the browser asking for language, "ar" or "en", at the web's first page. */
    public static synchronized @NotNull Page page(final @NotNull String language) {
        close();
        context = browser.newContext(new Browser.NewContextOptions().setBaseURL(URL).setLocale(language).setViewportSize(WIDTH, HEIGHT));
        page = context.newPage();
        page.navigate("/");
        return page;
    }

    /** The page the running case opened. */
    public static @NotNull Page current() {
        return page;
    }

    /** The element of the running case's page whose id is id. */
    public static @NotNull Locator element(final @NotNull String id) {
        return page.getByTestId(id);
    }

    /** Signs username in with password through the sign-in form, and waits for the shell's side menu to open to its width. */
    public static void signIn(final @NotNull String username, final @NotNull String password) {
        element("signIn.username").fill(username);
        element("signIn.password").fill(password);
        element("signIn.submit").click();
        assertThat(element("web.sideMenu")).isVisible();
        sideMenuSettled(false);
    }

    /**
     * Opens the side menu's item called name, opening More first when the item is folded under it and More is closed (redix-api findings row 307).
     */
    public static void openItem(final @NotNull String name) {
        final @NotNull Locator more = element("web.more");
        if (more.getByTestId("web.item." + name).count() > 0 && more.getAttribute("open") == null) {
            element("web.more.toggle").click();
        }
        element("web.item." + name).click();
    }

    /** Opens My account in the status bar and hovers Settings, so its list stands open beside the menu (redix-api findings row 321). */
    public static void openSettings() {
        element("web.account").click();
        element("web.sharedSettings").hover();
        assertThat(element("web.settingsMenu")).isVisible();
    }

    /** Opens My account in the status bar and chooses the item whose id is id: My history or Sign out (redix-api findings row 306). */
    public static void openAccount(final @NotNull String id) {
        element("web.account").click();
        element(id).click();
    }

    /**
     * Waits until the side menu has finished moving: folded to nothing, or open to the width the shell holds. The shell moves it over 260 ms, on
     * signing in, folding and unfolding, so a case measuring it waits for the move to end.
     */
    public static void sideMenuSettled(final boolean folded) {
        page.waitForFunction("folded => { const width = document.getElementById('web.sideMenu').getBoundingClientRect().width;"
                + " return folded ? width <= 1 : Math.round(width) === parseInt(document.getElementById('web.shell').style.getPropertyValue('--side-w')) }", folded);
    }

    /** Closes the running case's page and its cookies. */
    static synchronized void close() {
        context.close();
    }

    /** Closes the browser at the run's end. */
    static synchronized void quit() {
        browser.close();
        playwright.close();
    }

    private static @NotNull Browser launched() {
        playwright.selectors().setTestIdAttribute("id");
        return playwright.chromium().launch(new BrowserType.LaunchOptions().setChannel("chrome")
                .setHeadless(!"false".equals(System.getenv().getOrDefault("REDIX_HEADLESS", "true"))));
    }

    /** The driver's environment: this process's, with no browser of Playwright's own to download, since the run drives the installed Chrome. */
    private static @NotNull Map<String, String> driverEnvironment() {
        final @NotNull Map<String, String> environment = new HashMap<>(System.getenv());
        environment.put("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1");
        return environment;
    }
}
