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

    /** A new manager's approved project, as started holds it: its manager and token, its path, its Excavation item, that item's table, its bank account and first payment method. */
    private record Started(@NotNull People.Person manager, @NotNull String projectId, @NotNull String token, @NotNull String path, @NotNull String item,
                           @NotNull String table, @NotNull String account, @NotNull String method) {
    }

    /** A new manager's project, registered with partners and approved, with a bank account and a purchases item, Excavation, as the app records them. */
    private static @NotNull Started started(final @NotNull List<String> partners) {
        final @NotNull String tag = String.valueOf(ThreadLocalRandom.current().nextInt(10_000_000, 99_999_999));
        final @NotNull People.Person manager = new People.Person("manager" + tag, "Manager " + tag);
        final @NotNull String projectId = given(Api.anonymous()).body(Map.of("name", "Project " + tag, "startDate", LocalDate.now().minusDays(30).toString(), "units", List.of("A1"),
                        "partners", partners, "description", "", "displayName", manager.displayName(), "username", manager.username(), "mobile", "05" + tag,
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
        return new Started(manager, projectId, token, path, item, table, account, method);
    }

    /** A new manager's approved project with approved entries of the amounts approved, then entries of the amounts waiting, sent for approval. */
    public static @NotNull Kept withEntries(final @NotNull List<Integer> approved, final @NotNull List<Integer> waiting) {
        final @NotNull Started project = started(List.of());
        final @NotNull List<String> ids = new ArrayList<>();
        final @NotNull List<Integer> amounts = new ArrayList<>(approved);
        amounts.addAll(waiting);
        for (int k = 0; k < amounts.size(); k++) {
            final @NotNull String id = java.util.UUID.randomUUID().toString();
            given(Api.as(project.token())).body(Map.of("id", id, "itemId", project.item(), "tableId", project.table(), "date", LocalDate.now().minusDays(amounts.size() - k).toString(),
                    "amount", amounts.get(k), "accountId", project.account(), "paymentMethodId", project.method(), "description", "Payment " + (k + 1), "columnValues", Map.of(),
                    "submit", true)).post(project.path() + "/entries").then().statusCode(201);
            ids.add(id);
        }
        if (!approved.isEmpty()) {
            given(Api.as(project.token())).body(Map.of("entryIds", ids.subList(0, approved.size()))).post(project.path() + "/entries/approve").then().statusCode(200);
        }
        return new Kept(project.manager(), project.projectId(), ids);
    }

    /** A new manager's approved project whose Excavation item takes contracts, with one of value to Contractor, and no entries. */
    public static @NotNull Kept withContract(final int value) {
        final @NotNull Started project = started(List.of());
        given(Api.as(project.token())).body(Map.of("type", "both")).put(project.path() + "/items/" + project.item() + "/type").then().statusCode(200);
        final @NotNull String supplier = given(Api.as(project.token())).body(Map.of("name", "Contractor")).post(project.path() + "/suppliers").then().statusCode(201)
                .extract().path("id");
        given(Api.as(project.token())).body(Map.of("supplierId", supplier, "itemId", project.item(), "value", value, "isOpenValue", false))
                .post(project.path() + "/contracts").then().statusCode(200);
        return new Kept(project.manager(), project.projectId(), List.of());
    }

    /** A new manager's approved project registered with the partner partner, who has put nothing in yet. */
    public static @NotNull Kept withPartner(final @NotNull String partner) {
        final @NotNull Started project = started(List.of(partner));
        return new Kept(project.manager(), project.projectId(), List.of());
    }
}
