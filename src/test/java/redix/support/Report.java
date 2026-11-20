// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redix.support;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.jetbrains.annotations.NotNull;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

/**
 * The run's Extent report, target/extent-report/index.html: a page per case, named by its test case, then whether it passed, and the page as the
 * case left it when it failed. Before the first case it waits for the web to answer, so a run never starts against nothing.
 */
public final class Report implements ISuiteListener, ITestListener {
    private static final @NotNull ExtentReports REPORT = new ExtentReports();
    private static final @NotNull ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();

    @Override
    public void onStart(final @NotNull ISuite suite) {
        REPORT.attachReporter(new ExtentSparkReporter("target/extent-report/index.html"));
        REPORT.setSystemInfo("Web", Web.URL);
        REPORT.setSystemInfo("API", Api.URL);
        waitForTheWeb();
    }

    @Override
    public void onFinish(final @NotNull ISuite suite) {
        Web.quit();
        REPORT.flush();
    }

    @Override
    public void onTestStart(final @NotNull ITestResult result) {
        final @NotNull String description = String.valueOf(result.getMethod().getDescription());
        CURRENT.set(REPORT.createTest(description.isBlank() || description.equals("null") ? result.getMethod().getMethodName() : description)
                .assignCategory(result.getMethod().getGroups()));
    }

    @Override
    public void onTestSuccess(final @NotNull ITestResult result) {
        CURRENT.get().pass("Passed");
        Web.close();
    }

    @Override
    public void onTestFailure(final @NotNull ITestResult result) {
        final @NotNull String screenshot = Base64.getEncoder().encodeToString(Web.current().screenshot());
        CURRENT.get().fail(result.getThrowable(), MediaEntityBuilder.createScreenCaptureFromBase64String(screenshot).build());
        Web.close();
    }

    @Override
    public void onTestSkipped(final @NotNull ITestResult result) {
        onTestStart(result);
        CURRENT.get().skip(String.valueOf(result.getThrowable()));
    }

    /** Waits up to a minute for the web's first page, as the server may still be bringing its database up to date. */
    private static void waitForTheWeb() {
        final @NotNull HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        for (int tried = 0; tried < 60; tried++) {
            try {
                if (client.send(HttpRequest.newBuilder(URI.create(Web.URL + "/")).build(), HttpResponse.BodyHandlers.discarding()).statusCode() == 200) {
                    return;
                }
            } catch (final java.io.IOException notYet) {
                pause();
            } catch (final InterruptedException stopped) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        throw new IllegalStateException("The web at " + Web.URL + " did not answer for a minute: start the Dev server with its web first");
    }

    private static void pause() {
        try {
            Thread.sleep(Duration.ofSeconds(1));
        } catch (final InterruptedException stopped) {
            Thread.currentThread().interrupt();
        }
    }
}
