// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.FilePayload;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;
import redix.support.Ledger;
import redix.support.People;
import redix.support.Web;
import redix.support.Words;

import java.util.Base64;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static redix.support.Web.element;

/** The web's Documents (redix-api #175 D3, UC-064), on a new manager's project made through the API. */
public class DocumentsTest {
    /** A picture of one pixel, as a PNG. */
    private static final byte @NotNull [] PICTURE = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

    /** Documents of kept's project, signed in as its manager. */
    private static @NotNull Page onDocuments(final @NotNull Ledger.Kept kept) {
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        element("web.item.documents").click();
        assertThat(element("documents.title")).isVisible();
        return page;
    }

    /** Keeps the picture called fileName through Add document, named name in group; its panel says it was saved. */
    private static void addDocument(final @NotNull String name, final @NotNull String group, final @NotNull String fileName) {
        element("documents.add").click();
        element("documentForm.name").fill(name);
        element("documentForm.group").fill(group);
        element("documentForm.file").setInputFiles(new FilePayload(fileName, "image/png", PICTURE));
        element("documentForm.save").click();
        assertThat(element("document.notice")).hasText(Words.of("ar", "documents.saved"));
    }

    /** The row of the document named name. */
    private static @NotNull Locator row(final @NotNull Page page, final @NotNull String name) {
        return page.locator("tr[id^='documents.document.']").filter(new Locator.FilterOptions().setHasText(name));
    }

    @Test(description = "Add document keeps a picture under its name and group, shows it, and hands its file over", testName = "3f8a1d6c-9b2e-4c7a-a5d1-6e0b4f8c2a93",
            priority = 1)
    public void addDocumentKeepsAPictureUnderItsNameAndGroupShowsItAndHandsItsFileOver() {
        final @NotNull Page page = onDocuments(Ledger.withEntries(List.of(), List.of()));
        assertThat(element("documents.empty")).hasText(Words.of("ar", "documents.none"));
        addDocument("Ground floor plan", "Plans", "plan.png");
        assertThat(element("document.title")).hasText("Ground floor plan");
        assertThat(element("document.picture")).isVisible();
        assertThat(page.locator("th[id^='documents.groupHeading.']")).hasText("Plans");
        assertThat(row(page, "Ground floor plan")).containsText("plan.png");
        final @NotNull Download file = page.waitForDownload(() -> element("document.download").click());
        Assert.assertEquals(file.suggestedFilename(), "plan.png");
    }

    @Test(description = "Remove is asked first, and Add document without a file says to choose one", testName = "c4e7b2a9-5d1f-4e8b-9a3c-7b6d0f2e5c18", priority = 2)
    public void removeIsAskedFirstAndAddDocumentWithoutAFileSaysToChooseOne() {
        final @NotNull Page page = onDocuments(Ledger.withEntries(List.of(), List.of()));
        element("documents.add").click();
        element("documentForm.name").fill("Permit");
        element("documentForm.save").click();
        assertThat(element("documentForm.refusal")).hasText(Words.of("ar", "documents.fileRequired"));
        assertThat(element("documentForm.name")).hasValue("Permit");
        element("documentForm.file").setInputFiles(new FilePayload("permit.png", "image/png", PICTURE));
        element("documentForm.save").click();
        assertThat(element("document.notice")).hasText(Words.of("ar", "documents.saved"));
        element("document.remove").click();
        assertThat(element("document.removeQuestion")).hasText(Words.of("ar", "swipe.question"));
        element("document.removeConfirm").click();
        assertThat(row(page, "Permit")).isHidden();
        assertThat(element("documents.empty")).isVisible();
    }
}
