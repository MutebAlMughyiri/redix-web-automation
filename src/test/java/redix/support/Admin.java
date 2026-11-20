// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redix.support;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * The admin the API was started with: REDIX_ADMIN_USERNAME and REDIX_ADMIN_PASSWORD, the server's REDIX_Admin__Username and its admin's
 * password. The API makes that password temporary (BR-29), so the first sign-in changes it to REDIX_ADMIN_NEW_PASSWORD; on a server whose admin
 * already did, the new password is the one that signs in. Signed in at the first case that asks, and again before the access token runs out (15
 * minutes), as redix-api-automation keeps its admin.
 */
final class Admin {
    private static final @NotNull Duration RENEWED_AFTER = Duration.ofMinutes(10);
    private static @NotNull String token = "";
    private static @NotNull Instant signedInAt = Instant.EPOCH;

    private Admin() {
    }

    /** The admin's access token: the first sign-in's, then a new one each RENEWED_AFTER, with the password the first one chose. */
    static synchronized @NotNull String token() {
        if (!token.isEmpty() && Duration.between(signedInAt, Instant.now()).compareTo(RENEWED_AFTER) >= 0) {
            token = signIn(required("REDIX_ADMIN_USERNAME"), required("REDIX_ADMIN_NEW_PASSWORD")).then().statusCode(200).extract().path("accessToken");
            signedInAt = Instant.now();
        }
        if (token.isEmpty()) {
            final @NotNull String username = required("REDIX_ADMIN_USERNAME");
            final @NotNull String newPassword = required("REDIX_ADMIN_NEW_PASSWORD");
            final @NotNull Response first = signIn(username, required("REDIX_ADMIN_PASSWORD"));
            if (first.statusCode() == 200 && Boolean.TRUE.equals(first.path("mustChangePassword"))) {
                RestAssured.given(Api.anonymous()).header("Authorization", "Bearer " + first.path("accessToken"))
                        .body(Map.of("currentPassword", required("REDIX_ADMIN_PASSWORD"), "newPassword", newPassword))
                        .post("/auth/change-password").then().statusCode(200);
            }
            final @NotNull Response signedIn = first.statusCode() == 200 && !Boolean.TRUE.equals(first.path("mustChangePassword"))
                    ? first : signIn(username, newPassword);
            token = signedIn.then().statusCode(200).extract().path("accessToken");
            signedInAt = Instant.now();
        }
        return token;
    }

    private static @NotNull Response signIn(final @NotNull String username, final @NotNull String password) {
        return RestAssured.given(Api.anonymous()).body(Map.of("username", username, "password", password)).post("/auth/sign-in");
    }

    private static @NotNull String required(final @NotNull String name) {
        final @NotNull String value = System.getenv().getOrDefault(name, "");
        if (value.isBlank()) {
            throw new IllegalStateException("Set " + name + ": the tests make their people as the admin the API was started with");
        }
        return value;
    }
}
