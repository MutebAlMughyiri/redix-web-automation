// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Mouse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.FilePayload;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;
import redix.support.Ledger;
import redix.support.People;
import redix.support.Web;
import redix.support.Words;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static redix.support.Web.element;

/** The web's Documents (redix-api #175 D3, UC-064), on a new manager's project made through the API. */
public class DocumentsTest {
    /** A picture of one pixel, as a PNG. */
    private static final byte @NotNull [] PICTURE = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

    /** A PDF of one empty page. */
    private static final byte @NotNull [] PDF = ("%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj 2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj "
            + "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 200 200]>>endobj\ntrailer<</Root 1 0 R>>\n%%EOF").getBytes(StandardCharsets.US_ASCII);

    /** Documents of kept's project, signed in as its manager. */
    private static @NotNull Page onDocuments(final @NotNull Ledger.Kept kept) {
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        Web.openItem("documents");
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

    @Test(description = "A PDF document shows in a small frame in its panel, and its file is handed over", testName = "186cb968-ae7f-4813-8417-c294a7e4f269",
            priority = 3)
    public void aPdfDocumentShowsInASmallFrameInItsPanelAndItsFileIsHandedOver() {
        final @NotNull Page page = onDocuments(Ledger.withEntries(List.of(), List.of()));
        element("documents.add").click();
        element("documentForm.name").fill("Contractor's contract");
        element("documentForm.file").setInputFiles(new FilePayload("contract.pdf", "application/pdf", PDF));
        element("documentForm.save").click();
        assertThat(element("document.notice")).hasText(Words.of("ar", "documents.saved"));
        assertThat(element("document.pdf")).isVisible();
        assertThat(element("document.pdf")).hasAttribute("title", "Contractor's contract");
        assertThat(element("document.picture")).hasCount(0);
        final @NotNull Download file = page.waitForDownload(() -> element("document.download").click());
        Assert.assertEquals(file.suggestedFilename(), "contract.pdf");
    }

    /** The details panel's width on screen, in whole pixels. */
    private static int detailsWidth() {
        return ((Number) element("web.details").evaluate("panel => Math.round(panel.getBoundingClientRect().width)")).intValue();
    }

    /**
     * Drags the details panel's line by pixels toward the content, to the right in Arabic: a positive number widens the panel. It waits for the
     * View menu to keep the width, so drags one after another are kept in their order over a slow network too.
     */
    private static void dragTheDetailsLine(final @NotNull Page page, final int pixels) {
        final @NotNull BoundingBox line = element("web.detailsLine").boundingBox();
        final double x = line.x + line.width / 2, y = line.y + 200;
        page.waitForResponse(saved -> saved.url().endsWith("/view"), () -> {
            page.mouse().move(x, y);
            page.mouse().down();
            page.mouse().move(x + pixels, y, new Mouse.MoveOptions().setSteps(8));
            page.mouse().up();
        });
    }

    @Test(description = "The details panel's line drags it wider and narrower within its limits, and a reload keeps its width",
            testName = "3f3e58ef-68d0-4f54-b198-c6447b8cc404", priority = 4)
    public void theDetailsPanelsLineDragsItWiderAndNarrowerWithinItsLimitsAndAReloadKeepsItsWidth() {
        final @NotNull Page page = onDocuments(Ledger.withEntries(List.of(), List.of()));
        addDocument("Ground floor plan", "Plans", "plan.png");
        final int before = detailsWidth();
        dragTheDetailsLine(page, 100);
        Assert.assertTrue(Math.abs(detailsWidth() - (before + 100)) <= 4, "100 pixels toward the content widen it by 100: " + detailsWidth());
        dragTheDetailsLine(page, 2000);
        Assert.assertEquals(detailsWidth(), 560, "no wider than 560");
        dragTheDetailsLine(page, -2000);
        Assert.assertEquals(detailsWidth(), 240, "no narrower than 240");
        dragTheDetailsLine(page, 60);
        final int width = detailsWidth();
        page.reload();
        row(page, "Ground floor plan").click();
        assertThat(element("document.title")).hasText("Ground floor plan");
        Assert.assertTrue(Math.abs(detailsWidth() - width) <= 1, "the width is kept: " + detailsWidth() + " for " + width);
    }
}

