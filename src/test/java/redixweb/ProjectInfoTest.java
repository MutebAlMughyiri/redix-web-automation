// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

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

/** The web's More info (redix-api #175, UC-006), on a new manager's project made through the API. */
public class ProjectInfoTest {
    /** More info of a new manager's project, signed in as its manager. */
    private static @NotNull Page onProjectInfo() {
        final @NotNull Ledger.Kept kept = Ledger.withEntries(List.of(), List.of());
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        Web.openItem("projectInfo");
        assertThat(element("projectInfo.title")).isVisible();
        return page;
    }

    @Test(description = "More info's Rename, Add unit and the description are kept as the app's dialogs", testName = "9a4e2c7b-5d1f-4b3a-8e6c-0f2d7a9c4b15", priority = 1)
    public void moreInfosRenameAddUnitAndTheDescriptionAreKeptAsTheAppsDialogs() {
        final @NotNull Page page = onProjectInfo();
        element("projectInfo.rename").click();
        element("projectInfo.field").fill("Garden villas");
        element("projectInfo.formSave").click();
        assertThat(element("projectInfo.name")).hasText("Garden villas");
        assertThat(element("web.sideProject")).hasText("Garden villas");
        element("projectInfo.addUnit").click();
        element("projectInfo.field").fill("B7");
        element("projectInfo.formSave").click();
        assertThat(page.locator("[id^='projectInfo.unit.']").filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText("B7"))).isVisible();
        element("projectInfo.description").fill("Six villas around a garden");
        element("projectInfo.saveDescription").click();
        assertThat(element("projectInfo.notice")).hasText(Words.of("ar", "projectInfo.saved"));
    }

    @Test(description = "Close the project, asked first, leaves it read only", testName = "c5d8a1f3-7b2e-4c9d-a4f6-3e1b0c8d2a74", priority = 2)
    public void closeTheProjectAskedFirstLeavesItReadOnly() {
        onProjectInfo();
        element("projectInfo.close").click();
        assertThat(element("projectInfo.question")).hasText(Words.of("ar", "projects.closeQuestion"));
        element("projectInfo.formSave").click();
        assertThat(element("projectInfo.details")).containsText(Words.of("ar", "projects.readOnly"));
        assertThat(element("projectInfo.rename")).isHidden();
        assertThat(element("projectInfo.close")).isHidden();
    }
}
