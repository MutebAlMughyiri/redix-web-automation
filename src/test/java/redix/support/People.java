// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redix.support;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;

/**
 * The people a case signs in as on the web: each made fresh through the API, in a project of its own the admin made and approved, so no case leans
 * on another's login, as redix-api-automation's People makes them (UC-007).
 */
public final class People {
    /** The password every person made here signs in with once their temporary one is changed. */
    public static final @NotNull String PASSWORD = "Chosen-passw0rd-1";

    /** The temporary password the manager gives every person made here, which they must change at their first sign-in (AC-02.1). */
    public static final @NotNull String TEMPORARY = "Temporary-passw0rd-1";

    private People() {
    }

    /** Someone a case signs in as: what they type, and the name the top bar shows. */
    public record Person(@NotNull String username, @NotNull String displayName) {
    }

    /** A person whose temporary password is already changed to PASSWORD. */
    public static @NotNull Person signedUp() {
        final @NotNull Person person = newcomer();
        final @NotNull String token = given(Api.anonymous()).body(Map.of("username", person.username(), "password", TEMPORARY)).post("/auth/sign-in")
                .then().statusCode(200).extract().path("accessToken");
        given(Api.as(token)).body(Map.of("currentPassword", TEMPORARY, "newPassword", PASSWORD)).post("/auth/change-password").then().statusCode(200);
        return person;
    }

    /** A person who still holds the temporary password TEMPORARY. */
    public static @NotNull Person newcomer() {
        final @NotNull String tag = String.valueOf(ThreadLocalRandom.current().nextInt(10_000_000, 99_999_999));
        final @NotNull Person person = new Person("person" + tag, "Person " + tag);
        given(Api.asAdmin()).body(Map.of("displayName", person.displayName(), "username", person.username(), "mobile", "05" + tag,
                        "email", "person" + tag + "@redix.test", "temporaryPassword", TEMPORARY, "permissions", List.of(), "roleName", "", "partnerIds", List.of()))
                .post("/projects/" + project() + "/users").then().statusCode(201);
        return person;
    }

    /** A project the admin made and approved, with one unit (UC-006). */
    private static @NotNull String project() {
        final @NotNull String id = given(Api.asAdmin()).body(Map.of("name", "Project " + UUID.randomUUID().toString().substring(0, 8),
                "startDate", LocalDate.now().toString(), "units", List.of("A1"), "description", "", "partners", List.of())).post("/projects").then()
                .statusCode(201).extract().path("id");
        given(Api.asAdmin()).post("/projects/" + id + "/approve").then().statusCode(200);
        return id;
    }
}
