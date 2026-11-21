// Copyright 2026 Tech Details. All rights reserved.
// Use of this file is governed by the LICENSE file at the root of this repository.

package redix.support;

import io.restassured.common.mapper.TypeRef;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;

/**
 * A project with entries in it, for the web's Entries cases (redix-api #166): registered through the API by a new manager of its own, approved by
 * the admin, with a bank account, an item and entries recorded by the manager, as the app records them.
 */
public final class Ledger {
    private Ledger() {
    }

    /** The manager of the project, its id, and its entries' ids, newest last. */
    public record Kept(@NotNull People.Person manager, @NotNull String projectId, @NotNull List<String> entryIds) {
    }

    /** A new manager's approved project with approved entries of the amounts approved, then entries of the amounts waiting, sent for approval. */
    public static @NotNull Kept withEntries(final @NotNull List<Integer> approved, final @NotNull List<Integer> waiting) {
        final @NotNull String tag = String.valueOf(ThreadLocalRandom.current().nextInt(10_000_000, 99_999_999));
        final @NotNull People.Person manager = new People.Person("manager" + tag, "Manager " + tag);
        final @NotNull String projectId = given(Api.anonymous()).body(Map.of("name", "Project " + tag, "startDate", LocalDate.now().minusDays(30).toString(), "units", List.of("A1"),
                        "partners", List.of(), "description", "", "displayName", manager.displayName(), "username", manager.username(), "mobile", "05" + tag,
                        "password", People.PASSWORD))
                .post("/projects/register").then().statusCode(201).extract().path("id");
        given(Api.asAdmin()).post("/projects/" + projectId + "/approve").then().statusCode(200);
        final @NotNull String token = given(Api.anonymous()).body(Map.of("username", manager.username(), "password", People.PASSWORD)).post("/auth/sign-in")
                .then().statusCode(200).extract().path("accessToken");
        final @NotNull String path = "/projects/" + projectId;
        given(Api.as(token)).body(Map.of("name", "Bank", "type", "bank")).post(path + "/accounts").then().statusCode(200);
        final @NotNull String category = given(Api.as(token)).body(Map.of("name", "Works")).post(path + "/categories").then().statusCode(201).extract().path("id");
        final @NotNull String item = given(Api.as(token)).body(Map.of("name", "Excavation", "type", "purchases")).post(path + "/categories/" + category + "/items")
                .then().statusCode(201).extract().path("id");
        final @NotNull String table = given(Api.as(token)).get(path + "/entry-tables").then().statusCode(200).extract()
                .path("find { it.items.find { it.id == '" + item + "' } }.items.find { it.id == '" + item + "' }.tables[0].id");
        final @NotNull List<Map<String, Object>> accounts = given(Api.as(token)).get(path + "/accounts").then().statusCode(200).extract().as(new TypeRef<>() {});
        final @NotNull String account = accounts.stream().filter(it -> "bank".equals(it.get("type"))).findFirst().orElseThrow().get("id").toString();
        final @NotNull String method = given(Api.as(token)).get(path + "/payment-methods").then().statusCode(200).extract().path("[0].id");
        final @NotNull List<String> ids = new ArrayList<>();
        final @NotNull List<Integer> amounts = new ArrayList<>(approved);
        amounts.addAll(waiting);
        for (int k = 0; k < amounts.size(); k++) {
            final @NotNull String id = java.util.UUID.randomUUID().toString();
            given(Api.as(token)).body(Map.of("id", id, "itemId", item, "tableId", table, "date", LocalDate.now().minusDays(amounts.size() - k).toString(),
                    "amount", amounts.get(k), "accountId", account, "paymentMethodId", method, "description", "Payment " + (k + 1), "columnValues", Map.of(),
                    "submit", true)).post(path + "/entries").then().statusCode(201);
            ids.add(id);
        }
        if (!approved.isEmpty()) {
            given(Api.as(token)).body(Map.of("entryIds", ids.subList(0, approved.size()))).post(path + "/entries/approve").then().statusCode(200);
        }
        return new Kept(manager, projectId, ids);
    }
}
