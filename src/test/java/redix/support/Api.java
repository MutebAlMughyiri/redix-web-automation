// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redix.support;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.jetbrains.annotations.NotNull;

/** The REDIX API that makes what a case finds already in place: its address, the JSON it speaks, and who calls it. */
public final class Api {
    /** The API's address: REDIX_API_URL, or the Dev server on this machine, never qa. */
    public static final @NotNull String URL = System.getenv().getOrDefault("REDIX_API_URL", "http://127.0.0.1:5010");

    private Api() {
    }

    /** A request as nobody: signing in, the public routes. */
    public static @NotNull RequestSpecification anonymous() {
        return new RequestSpecBuilder().setBaseUri(URL).setContentType(ContentType.JSON).build();
    }

    /** A request as the admin, whose session Admin keeps signed in for the whole run. */
    public static @NotNull RequestSpecification asAdmin() {
        return as(Admin.token());
    }

    /** A request with token, the access token of a session. */
    public static @NotNull RequestSpecification as(final @NotNull String token) {
        return new RequestSpecBuilder().addRequestSpecification(anonymous()).addHeader("Authorization", "Bearer " + token).build();
    }
}
