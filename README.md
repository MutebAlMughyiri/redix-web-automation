# REDIX web automation

The REDIX web's functional tests (redix-api #161): Java 21, TestNG and Playwright, with an Extent report, the twin of
[redix-api-automation](https://github.com/MutebAlMughyiri/redix-api-automation). They drive the web in Chrome as a person does, and check
what it shows.

## How the tests are written

The test cases are managed in [Testin](https://github.com/MutebAlMughyiri/test-in), in their own repository,
[redix-web-test-cases](https://github.com/MutebAlMughyiri/redix-web-test-cases). `testin.yml` links the two, so whoever opens this
project in IntelliJ with Testin lands on that test project.

| In Testin | Here |
| --- | --- |
| A test set | A class under `src/test/java/redixweb`, which Testin writes |
| A test case | A `@Test` method in that class, which Testin writes with its `description`, `testName` and `priority` |
| The steps | The method's body, which the tester writes |

A body finds every element by its id, `Web.element("signIn.username")`: Playwright's `getByTestId`, set to read `id`. Where the app has the
same element, the id is the app's key name, as Patrol finds the app's widgets; elsewhere it is the web's own, under `web.`. redix-api's
[the web's design](https://github.com/MutebAlMughyiri/redix-api/blob/main/docs/web/DESIGN.md) lists every id.

What a case finds already in place is made through the API, so only the case goes through the screens: `People.signedUp()` is a new person
in a project of their own, their password `People.PASSWORD`; `People.newcomer()` one still holding the temporary `People.TEMPORARY`. A case
checks a page by the app's own words, `Words.of("ar", "signIn.invalidCredentials")`.

```java
final Person person = People.signedUp();
Web.page();
Web.signIn(person.username(), People.PASSWORD);
assertThat(element("web.person")).hasText(person.displayName());
```

## Running them

| Variable | What | Default |
| --- | --- | --- |
| `REDIX_WEB_URL` | The web the tests open | `http://127.0.0.1:5020`, the Dev server's web on this machine |
| `REDIX_API_URL` | The API that makes what a case finds in place | `http://127.0.0.1:5010`, the same Dev server |
| `REDIX_ADMIN_USERNAME`, `REDIX_ADMIN_PASSWORD` | The admin that server was started with | required |
| `REDIX_ADMIN_NEW_PASSWORD` | The password the first sign-in changes the admin's temporary one to; on a server whose admin already changed it, the one that signs in | required |
| `REDIX_HEADLESS` | `false` shows Chrome while the tests run | `true` |

Start the Dev server from redix-api (`go-backend`, `REDIX_Environment=Dev`, which serves its web on 5020), then run `mvn test`, or a test
from Testin with `F5`. Never point them at qa: they make projects and people. They drive the Chrome installed on the machine; Playwright
downloads no browser of its own.

Each run writes `target/extent-report/index.html`: a page per test, named by its test case, and the page as the test left it when it failed.

CI (`.github/workflows/web-tests.yml`) builds the API from redix-api's `main`, starts it as Dev on an empty database and runs every test, on
every push, nightly and on demand; the report is the run's artifact.
