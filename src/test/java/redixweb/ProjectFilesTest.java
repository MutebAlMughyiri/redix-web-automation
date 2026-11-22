// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redixweb;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.FilePayload;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;
import redix.support.Ledger;
import redix.support.People;
import redix.support.Web;
import redix.support.Words;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static redix.support.Web.element;

/** The web's Load file, Archive and Retrieve (redix-api #175 D3, UC-071, UC-073), on a new manager's project made through the API. */
public class ProjectFilesTest {
    /** The home of kept's project, signed in as its manager. */
    private static @NotNull Page onHome(final @NotNull Ledger.Kept kept) {
        final @NotNull Page page = Web.page();
        Web.signIn(kept.manager().username(), People.PASSWORD);
        element("projects.card." + kept.projectId()).click();
        assertThat(element("home.title")).isVisible();
        return page;
    }

    /** A project file holding no rows, whose one account is called account, or none when it is empty. */
    private static @NotNull FilePayload projectFile(final @NotNull String account) {
        final @NotNull String accounts = account.isEmpty() ? "" : "{\"name\":\"" + account + "\",\"actualBalance\":10}";
        final @NotNull String file = "{\"format\":\"redix.project\",\"version\":1,\"project\":\"Project\",\"report\":{\"number\":0},\"accounts\":[" + accounts
                + "],\"lists\":[],\"categories\":[]}";
        return new FilePayload("project.json", "application/json", file.getBytes(StandardCharsets.UTF_8));
    }

    @Test(description = "Load file lists every row a project file was refused for, and loads one it takes", testName = "9a4c2e7b-1f5d-4b8a-a3e6-5d0c8b2f7e14",
            priority = 1)
    public void loadFileListsEveryRowAProjectFileWasRefusedForAndLoadsOneItTakes() {
        onHome(Ledger.withEntries(List.of(), List.of()));
        Web.openItem("loadFile");
        assertThat(element("loadFile.what")).hasText(Words.of("ar", "projects.loadFileWhat"));
        element("loadFile.file").setInputFiles(projectFile("No such account"));
        element("loadFile.load").click();
        assertThat(element("loadFile.refusal")).hasText(Words.of("ar", "projects.loadFile.rowsRefused"));
        assertThat(element("loadFile.reason.0")).hasText(Words.of("ar", "projects.loadFile.accountNotInProject").replace("{0}", "No such account"));
        element("loadFile.file").setInputFiles(projectFile(""));
        element("loadFile.load").click();
        assertThat(element("loadFile.done")).hasText(Words.of("ar", "projects.loadFileDoneNoReport").replace("{0}", "0").replace("{1}", "0"));
    }

    @Test(description = "Archive hands the project's file over, and Retrieve previews it, then retrieves it", testName = "e6b1d8f3-4a2c-4e9b-8d7f-1c5a9e3b6d20",
            priority = 2)
    public void archiveHandsTheProjectsFileOverAndRetrievePreviewsItThenRetrievesIt() throws Exception {
        final @NotNull Page page = onHome(Ledger.withEntries(List.of(100), List.of()));
        final @NotNull Download archive = page.waitForDownload(() -> Web.openItem("archive"));
        Assert.assertTrue(archive.suggestedFilename().endsWith(".json"), archive.suggestedFilename());
        final @NotNull Path kept = Files.createTempFile("archive", ".json");
        archive.saveAs(kept);
        Web.openItem("retrieve");
        element("retrieve.file").setInputFiles(kept);
        assertThat(element("retrieve.takenBy")).isVisible();
        assertThat(element("retrieve.part.entries")).containsText(Words.of("ar", "projects.retrieve.part.entries"));
        element("retrieve.confirm").click();
        assertThat(element("retrieve.done")).containsText(": 1");
    }
}
