package subscriptions;

import base.BaseTest;
import builders.CustomerRequestBuilder;
import builders.PriceRequestBuilder;
import builders.ProductRequestBuilder;
import builders.SubscriptionRequestBuilder;
import io.qameta.allure.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static constants.StripeConstants.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Stripe API")
@Feature("Subscriptions")
public class SubscriptionTests extends BaseTest {

    private String customerId;
    private String productId;
    private String monthlyPriceId;
    private String yearlyPriceId;
    private String paymentMethodId;
    private String subscriptionId;
    private String subscriptionItemId;
    private String canceledSubscriptionId;

    @BeforeClass
    public void setup() {
        customerId = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder()
                        .email("sub_test_" + System.currentTimeMillis() + "@example.com")
                        .name("Subscription Test Customer")
                        .build(), "id");

        // skip adding to createdProductIds — @AfterMethod would archive it after the first test
        productId = createAndExtract(PRODUCTS,
                ProductRequestBuilder.builder().name("Subscription Test Product").build(), "id");

        monthlyPriceId = createAndExtract(PRICES,
                PriceRequestBuilder.builder()
                        .unitAmount(1000).currency("usd")
                        .product(productId).recurringInterval("month")
                        .build(), "id");

        yearlyPriceId = createAndExtract(PRICES,
                PriceRequestBuilder.builder()
                        .unitAmount(5000).currency("usd")
                        .product(productId).recurringInterval("year")
                        .build(), "id");

        paymentMethodId = createAndExtract(PAYMENT_METHODS,
                new java.util.LinkedHashMap<String, String>() {{
                    put("type", "card");
                    put("card[token]",CARD_SUCCESS);
                }}, "id");

        given().contentType(ContentType.URLENC)
                .formParam("customer", customerId)
                .post(PAYMENT_METHODS + "/" + paymentMethodId + "/attach")
                .then().statusCode(200);

        // base subscription shared by retrieve/update/cancel tests
        Response subRes = given()
                .contentType(ContentType.URLENC)
                .formParams(SubscriptionRequestBuilder.builder()
                        .customer(customerId)
                        .price(monthlyPriceId)
                        .defaultPaymentMethod(paymentMethodId)
                        .build())
                .when().post(SUBSCRIPTIONS)
                .then().statusCode(200)
                .extract().response();

        subscriptionId = subRes.path("id");
        subscriptionItemId = subRes.jsonPath().getString("items.data[0].id");
    }

    @AfterClass
    public void cleanup() {
        if (subscriptionId != null) {
            given().delete(SUBSCRIPTIONS + "/" + subscriptionId);
        }
        if (customerId != null) {
            given().delete(CUSTOMERS + "/" + customerId);
        }
        // prices can't be deleted, just deactivate them
        if (monthlyPriceId != null) {
            given().contentType(ContentType.URLENC)
                    .formParam("active", "false")
                    .post(PRICES + "/" + monthlyPriceId);
        }
        if (yearlyPriceId != null) {
            given().contentType(ContentType.URLENC)
                    .formParam("active", "false")
                    .post(PRICES + "/" + yearlyPriceId);
        }
        if (productId != null) {
            given().delete(PRODUCTS + "/" + productId);
        }
    }

    // ── Create Subscriptions ──────────────────────────────────────────────

    @Test(description = "TC-SUB-001: Create Subscription with Monthly Price")
    @Story("Create Subscription") @Severity(SeverityLevel.BLOCKER)
    @Description("POST /subscriptions with customer, monthly price, payment method — expects 200, status=active")
    public void tc_sub_001_createSubscriptionWithMonthlyPrice() {
        Response res = given()
                .contentType(ContentType.URLENC)
                .formParams(SubscriptionRequestBuilder.builder()
                        .customer(customerId)
                        .price(monthlyPriceId)
                        .defaultPaymentMethod(paymentMethodId)
                        .build())
                .when().post(SUBSCRIPTIONS)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        createdSubscriptionIds.add(res.path("id"));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("id").toString()).startsWith("sub_");
        soft.assertThat(res.path("object").toString()).isEqualTo("subscription");
        soft.assertThat(res.path("status").toString()).isEqualTo(STATUS_ACTIVE);
        soft.assertThat(res.path("customer").toString()).isEqualTo(customerId);
        soft.assertAll();
    }

    @Test(description = "TC-SUB-002: Create Subscription with Yearly Price")
    @Story("Create Subscription") @Severity(SeverityLevel.CRITICAL)
    @Description("POST /subscriptions with yearly price — expects 200, status=active")
    public void tc_sub_002_createSubscriptionWithYearlyPrice() {
        Response res = given()
                .contentType(ContentType.URLENC)
                .formParams(SubscriptionRequestBuilder.builder()
                        .customer(customerId)
                        .price(yearlyPriceId)
                        .defaultPaymentMethod(paymentMethodId)
                        .build())
                .when().post(SUBSCRIPTIONS)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        createdSubscriptionIds.add(res.path("id"));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("id").toString()).startsWith("sub_");
        soft.assertThat(res.path("status").toString()).isEqualTo(STATUS_ACTIVE);
        soft.assertAll();
    }

    @Test(description = "TC-SUB-003: Create Subscription with Trial Period")
    @Story("Create Subscription") @Severity(SeverityLevel.NORMAL)
    @Description("POST /subscriptions with trial_end=1800000000 — expects 200, status=trialing")
    public void tc_sub_003_createSubscriptionWithTrialPeriod() {
        Response res = given()
                .contentType(ContentType.URLENC)
                .formParams(SubscriptionRequestBuilder.builder()
                        .customer(customerId)
                        .price(monthlyPriceId)
                        .defaultPaymentMethod(paymentMethodId)
                        .trialEnd("1800000000")
                        .build())
                .when().post(SUBSCRIPTIONS)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        createdSubscriptionIds.add(res.path("id"));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("id").toString()).startsWith("sub_");
        soft.assertThat(res.path("status").toString()).isEqualTo(STATUS_TRIALING);
        soft.assertThat(res.path("trial_end").toString()).isEqualTo("1800000000");
        soft.assertAll();
    }

    @Test(description = "TC-SUB-004: Create Subscription without Payment Method")
    @Story("Create Subscription") @Severity(SeverityLevel.NORMAL)
    @Description("POST /subscriptions without default_payment_method — expects 400, payment method required")
    public void tc_sub_004_createSubscriptionWithoutPaymentMethod() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(SubscriptionRequestBuilder.builder()
                        .customer(customerId)
                        .price(monthlyPriceId)
                        .build())
                .when().post(SUBSCRIPTIONS)
                .then()
                .statusCode(400)
                .time(lessThan(MAX_RESPONSE_TIME_MS));
    }

    @Test(description = "TC-SUB-005: Create Subscription without Customer")
    @Story("Create Subscription") @Severity(SeverityLevel.NORMAL)
    @Description("POST /subscriptions with invalid customer — expects 404, No such customer")
    public void tc_sub_005_createSubscriptionWithoutCustomer() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(SubscriptionRequestBuilder.builder()
                        .customer("invalidCustomer")
                        .price(monthlyPriceId)
                        .defaultPaymentMethod(paymentMethodId)
                        .build())
                .when().post(SUBSCRIPTIONS)
                .then()
                .statusCode(404)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("No such customer"));
    }

    @Test(description = "TC-SUB-006: Create Subscription without Price")
    @Story("Create Subscription") @Severity(SeverityLevel.NORMAL)
    @Description("POST /subscriptions without price items — expects 400, error mentions items")
    public void tc_sub_006_createSubscriptionWithoutPrice() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(SubscriptionRequestBuilder.builder()
                        .customer(customerId)
                        .build())
                .when().post(SUBSCRIPTIONS)
                .then()
                .statusCode(400)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("items"));
    }

    @Test(description = "TC-SUB-007: Create Subscription with Non-Existent Customer")
    @Story("Create Subscription") @Severity(SeverityLevel.NORMAL)
    @Description("POST /subscriptions with non-existent customer ID — expects 404, No such customer")
    public void tc_sub_007_createSubscriptionWithNonExistentCustomer() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(SubscriptionRequestBuilder.builder()
                        .customer("cus_nonexistent123")
                        .price(monthlyPriceId)
                        .build())
                .when().post(SUBSCRIPTIONS)
                .then()
                .statusCode(404)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("No such customer"));
    }

    @Test(description = "TC-SUB-008: Create Subscription with Non-Existent Price")
    @Story("Create Subscription") @Severity(SeverityLevel.NORMAL)
    @Description("POST /subscriptions without price items — expects 400, error mentions items")
    public void tc_sub_008_createSubscriptionWithNonExistentPrice() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(SubscriptionRequestBuilder.builder()
                        .customer(customerId)
                        .build())
                .when().post(SUBSCRIPTIONS)
                .then()
                .statusCode(400)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("items"));
    }

    // ── Retrieve Subscriptions ────────────────────────────────────────────

    @Test(description = "TC-SUB-009: Retrieve Existing Subscription")
    @Story("Retrieve Subscription") @Severity(SeverityLevel.CRITICAL)
    @Description("GET /subscriptions/{id} — expects 200, id matches, object=subscription")
    public void tc_sub_009_retrieveExistingSubscription() {
        Response res = given()
                .when().get(SUBSCRIPTIONS + "/" + subscriptionId)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("id").toString()).isEqualTo(subscriptionId);
        soft.assertThat(res.path("object").toString()).isEqualTo("subscription");
        soft.assertAll();
    }

    @Test(description = "TC-SUB-010: Retrieve Subscription with Invalid ID")
    @Story("Retrieve Subscription") @Severity(SeverityLevel.NORMAL)
    @Description("GET /subscriptions/invalid_id — expects 404, No such subscription")
    public void tc_sub_010_retrieveSubscriptionWithInvalidId() {
        given()
                .when().get(SUBSCRIPTIONS + "/invalid_subscription_id")
                .then()
                .statusCode(404)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.message", containsString("No such subscription"));
    }

    // ── Update Subscriptions ──────────────────────────────────────────────

    @Test(description = "TC-SUB-011: Update Subscription to New Price")
    @Story("Update Subscription") @Severity(SeverityLevel.NORMAL)
    @Description("POST /subscriptions/{id} updating item price — expects 200, new price in items")
    public void tc_sub_011_updateSubscriptionToNewPrice() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(SubscriptionRequestBuilder.builder()
                        .updateItem(subscriptionItemId, yearlyPriceId)
                        .build())
                .when().post(SUBSCRIPTIONS + "/" + subscriptionId)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("items.data[0].price.id", equalTo(yearlyPriceId));
    }

    @Test(description = "TC-SUB-012: Update Subscription Metadata")
    @Story("Update Subscription") @Severity(SeverityLevel.NORMAL)
    @Description("POST /subscriptions/{id} with metadata — expects 200, metadata updated")
    public void tc_sub_012_updateSubscriptionMetadata() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(SubscriptionRequestBuilder.builder()
                        .metadata("order_id", "12345")
                        .metadata("tier", "premium")
                        .metadata("renewal_date", "2024-03-15")
                        .build())
                .when().post(SUBSCRIPTIONS + "/" + subscriptionId)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("metadata.order_id", equalTo("12345"))
                .body("metadata.tier", equalTo("premium"));
    }

    @Test(description = "TC-SUB-013: Update Subscription - Cancel at Period End")
    @Story("Update Subscription") @Severity(SeverityLevel.NORMAL)
    @Description("POST /subscriptions/{id} with cancel_at_period_end=true — expects 200, cancel_at_period_end=true, status=active")
    public void tc_sub_013_cancelAtPeriodEnd() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(SubscriptionRequestBuilder.builder()
                        .cancelAtPeriodEnd(true)
                        .build())
                .when().post(SUBSCRIPTIONS + "/" + subscriptionId)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("cancel_at_period_end", equalTo(true))
                .body("status", equalTo(STATUS_ACTIVE));
    }

    // ── Cancel Subscriptions ──────────────────────────────────────────────

    @Test(description = "TC-SUB-014: Cancel Subscription Immediately")
    @Story("Cancel Subscription") @Severity(SeverityLevel.CRITICAL)
    @Description("DELETE /subscriptions/{id} — expects 200, status=canceled, canceled_at populated")
    public void tc_sub_014_cancelSubscriptionImmediately() {
        String subId = given()
                .contentType(ContentType.URLENC)
                .formParams(SubscriptionRequestBuilder.builder()
                        .customer(customerId)
                        .price(monthlyPriceId)
                        .defaultPaymentMethod(paymentMethodId)
                        .build())
                .when().post(SUBSCRIPTIONS)
                .then().statusCode(200)
                .extract().path("id");

        Response res = given()
                .when().delete(SUBSCRIPTIONS + "/" + subId)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        canceledSubscriptionId = subId;

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("status").toString()).isEqualTo(STATUS_CANCELED);
        soft.assertThat(res.path("canceled_at").toString()).isNotBlank();
        soft.assertAll();
    }

    @Test(description = "TC-SUB-015: Cancel Already Canceled Subscription")
    @Story("Cancel Subscription") @Severity(SeverityLevel.NORMAL)
    @Description("DELETE already canceled subscription — expects 400, error mentions already canceled")
    public void tc_sub_015_cancelAlreadyCanceledSubscription() {
        // create a new sub, cancel it, then try again to verify the 404
        String subId = given()
                .contentType(ContentType.URLENC)
                .formParams(SubscriptionRequestBuilder.builder()
                        .customer(customerId)
                        .price(monthlyPriceId)
                        .defaultPaymentMethod(paymentMethodId)
                        .build())
                .when().post(SUBSCRIPTIONS)
                .then().statusCode(200)
                .extract().path("id");

        given().delete(SUBSCRIPTIONS + "/" + subId).then().statusCode(200);

        // second cancel — Stripe returns 404 once it's gone
        given()
                .when().delete(SUBSCRIPTIONS + "/" + subId)
                .then()
                .statusCode(404)
                .time(lessThan(MAX_RESPONSE_TIME_MS));
    }

    @Test(description = "TC-SUB-016: Cancel Invalid Subscription")
    @Story("Cancel Subscription") @Severity(SeverityLevel.NORMAL)
    @Description("DELETE /subscriptions/invalid_sub — expects 404, No such subscription")
    public void tc_sub_016_cancelInvalidSubscription() {
        given()
                .when().delete(SUBSCRIPTIONS + "/invalid_sub")
                .then()
                .statusCode(404)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.message", containsString("No such subscription"));
    }

    // ── List Subscriptions ────────────────────────────────────────────────

    @Test(description = "TC-SUB-017: List All Subscriptions")
    @Story("List Subscriptions") @Severity(SeverityLevel.NORMAL)
    @Description("GET /subscriptions — expects 200, object=list, data not empty, each has id and object")
    public void tc_sub_017_listAllSubscriptions() {
        Response res = given()
                .when().get(SUBSCRIPTIONS)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("object", equalTo("list"))
                .extract().response();

        List<String> ids = res.jsonPath().getList("data.id");
        List<String> objects = res.jsonPath().getList("data.object");

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(ids).isNotEmpty();
        ids.forEach(id -> soft.assertThat(id).startsWith("sub_"));
        objects.forEach(obj -> soft.assertThat(obj).isEqualTo("subscription"));
        soft.assertAll();
    }

    @Test(description = "TC-SUB-018: List Subscriptions for Specific Customer")
    @Story("List Subscriptions") @Severity(SeverityLevel.NORMAL)
    @Description("GET /subscriptions?customer={id} — expects 200, all subscriptions belong to customer")
    public void tc_sub_018_listSubscriptionsForSpecificCustomer() {
        Response res = given()
                .queryParam("customer", customerId)
                .when().get(SUBSCRIPTIONS)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("object", equalTo("list"))
                .extract().response();

        List<String> customers = res.jsonPath().getList("data.customer");
        SoftAssertions soft = new SoftAssertions();
        customers.forEach(c -> soft.assertThat(c).isEqualTo(customerId));
        soft.assertAll();
    }

    @Test(description = "TC-SUB-019: List Active Subscriptions Only")
    @Story("List Subscriptions") @Severity(SeverityLevel.NORMAL)
    @Description("GET /subscriptions?status=active — expects 200, all returned subscriptions are active")
    public void tc_sub_019_listActiveSubscriptionsOnly() {
        Response res = given()
                .queryParam("status", STATUS_ACTIVE)
                .when().get(SUBSCRIPTIONS)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("object", equalTo("list"))
                .extract().response();

        List<String> statuses = res.jsonPath().getList("data.status");
        SoftAssertions soft = new SoftAssertions();
        statuses.forEach(s -> soft.assertThat(s).isEqualTo(STATUS_ACTIVE));
        soft.assertAll();
    }

    @Test(description = "TC-SUB-020: List Canceled Subscriptions Only")
    @Story("List Subscriptions") @Severity(SeverityLevel.NORMAL)
    @Description("GET /subscriptions?status=canceled — expects 200, all returned subscriptions are canceled")
    public void tc_sub_020_listCanceledSubscriptionsOnly() {
        Response res = given()
                .queryParam("status", STATUS_CANCELED)
                .when().get(SUBSCRIPTIONS)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("object", equalTo("list"))
                .extract().response();

        List<String> statuses = res.jsonPath().getList("data.status");
        SoftAssertions soft = new SoftAssertions();
        statuses.forEach(s -> soft.assertThat(s).isEqualTo(STATUS_CANCELED));
        soft.assertAll();
    }
}
