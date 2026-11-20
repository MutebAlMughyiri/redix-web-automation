// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redix.support;

import io.restassured.common.mapper.TypeRef;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.restassured.RestAssured.given;

/** The words the web says things in, as the API's translations bundle holds them (UC-001): a case checks a page by the app's own words. */
public final class Words {
    private static final @NotNull Map<String, Map<String, String>> BUNDLES = new ConcurrentHashMap<>();

    private Words() {
    }

    /** The word key names in language, "ar" or "en". */
    public static @NotNull String of(final @NotNull String language, final @NotNull String key) {
        return BUNDLES.computeIfAbsent(language, asked -> given(Api.anonymous()).get("/translations/" + asked).then().statusCode(200).extract()
                .as(new TypeRef<Map<String, String>>() {})).getOrDefault(key, key);
    }
}
