package prices;

import base.BaseTest;
import builders.PriceRequestBuilder;
import builders.ProductRequestBuilder;
import io.qameta.allure.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static constants.StripeConstants.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Stripe API")
@Feature("Prices")
public class PriceTests extends BaseTest {

    private String productId;
    private String priceId;
    private final List<String> createdPriceIds = new ArrayList<>();

    @BeforeClass
    public void setup() {
        // Create a base product to attach prices to
        productId = createAndExtract(PRODUCTS,
                ProductRequestBuilder.builder()
                        .name("Price Test Product")
                        .build(), "id");

        // Create a base price for retrieve/update/deactivate tests
        priceId = createAndExtract(PRICES,
                PriceRequestBuilder.builder()
                        .unitAmount(1000)
                        .currency("usd")
                        .product(productId)
                        .build(), "id");
        createdPriceIds.add(priceId);
    }

    @AfterClass
    public void cleanup() {
        // Prices cannot be deleted in Stripe — deactivate them
        createdPriceIds.forEach(id ->
                given().contentType(ContentType.URLENC)
                        .formParam("active", "false")
                        .post(PRICES + "/" + id));

        // Delete the base product
        if (productId != null) {
            given().delete(PRODUCTS + "/" + productId);
        }
    }

    // ── Create Prices ─────────────────────────────────────────────────────

    @Test(description = "TC-PRC-001: Create One-Time Price")
    @Story("Create Price") @Severity(SeverityLevel.CRITICAL)
    @Description("POST /prices with unit_amount, currency, product — expects 200, type=one_time")
    public void tc_prc_001_createOneTimePrice() {
        Response res = given()
                .contentType(ContentType.URLENC)
                .formParams(PriceRequestBuilder.builder()
                        .currency("usd")
                        .unitAmount(1000)
                        .product(productId)
                        .build())
                .when().post(PRICES)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        createdPriceIds.add(res.path("id"));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("id").toString()).startsWith("price_");
        soft.assertThat(res.path("object").toString()).isEqualTo("price");
        soft.assertThat(res.path("type").toString()).isEqualTo("one_time");
        soft.assertThat(res.path("currency").toString()).isEqualTo("usd");
        soft.assertThat(res.path("unit_amount").toString()).isEqualTo("1000");
        soft.assertAll();
    }

    @Test(description = "TC-PRC-002: Create Monthly Recurring Price")
    @Story("Create Price") @Severity(SeverityLevel.CRITICAL)
    @Description("POST /prices with recurring[interval]=month — expects 200, type=recurring, interval=month")
    public void tc_prc_002_createMonthlyRecurringPrice() {
        Response res = given()
                .contentType(ContentType.URLENC)
                .formParams(PriceRequestBuilder.builder()
                        .currency("usd")
                        .unitAmount(1000)
                        .product(productId)
                        .recurringInterval("month")
                        .build())
                .when().post(PRICES)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        createdPriceIds.add(res.path("id"));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("type").toString()).isEqualTo("recurring");
        soft.assertThat(res.path("recurring.interval").toString()).isEqualTo("month");
        soft.assertThat(res.path("recurring.interval_count").toString()).isEqualTo("1");
        soft.assertThat(res.path("unit_amount").toString()).isEqualTo("1000");
        soft.assertAll();
    }

    @Test(description = "TC-PRC-003: Create Yearly Recurring Price")
    @Story("Create Price") @Severity(SeverityLevel.NORMAL)
    @Description("POST /prices with recurring[interval]=year — expects 200, type=recurring, interval=year")
    public void tc_prc_003_createYearlyRecurringPrice() {
        Response res = given()
                .contentType(ContentType.URLENC)
                .formParams(PriceRequestBuilder.builder()
                        .currency("usd")
                        .unitAmount(1000)
                        .product(productId)
                        .recurringInterval("year")
                        .build())
                .when().post(PRICES)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        createdPriceIds.add(res.path("id"));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("type").toString()).isEqualTo("recurring");
        soft.assertThat(res.path("recurring.interval").toString()).isEqualTo("year");
        soft.assertThat(res.path("recurring.interval_count").toString()).isEqualTo("1");
        soft.assertThat(res.path("unit_amount").toString()).isEqualTo("1000");
        soft.assertAll();
    }

    @Test(description = "TC-PRC-004: Create Price Without Product ID")
    @Story("Create Price") @Severity(SeverityLevel.NORMAL)
    @Description("POST /prices without product — expects 400, error mentions product")
    public void tc_prc_004_createPriceWithoutProductId() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(PriceRequestBuilder.builder()
                        .currency("usd")
                        .unitAmount(1000)
                        .recurringInterval("year")
                        .build())
                .when().post(PRICES)
                .then()
                .statusCode(400)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("product"));
    }

    @Test(description = "TC-PRC-005: Create Price with Zero Amount")
    @Story("Create Price") @Severity(SeverityLevel.NORMAL)
    @Description("POST /prices with unit_amount=0 — expects 200, unit_amount=0 (free price)")
    public void tc_prc_005_createPriceWithZeroAmount() {
        Response res = given()
                .contentType(ContentType.URLENC)
                .formParams(PriceRequestBuilder.builder()
                        .currency("usd")
                        .unitAmount(0)
                        .product(productId)
                        .build())
                .when().post(PRICES)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        createdPriceIds.add(res.path("id"));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("unit_amount").toString()).isEqualTo("0");
        soft.assertThat(res.path("object").toString()).isEqualTo("price");
        soft.assertAll();
    }

    @Test(description = "TC-PRC-006: Create Price with Invalid Currency")
    @Story("Create Price") @Severity(SeverityLevel.NORMAL)
    @Description("POST /prices with currency=xyz — expects 400, error mentions currency")
    public void tc_prc_006_createPriceWithInvalidCurrency() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(PriceRequestBuilder.builder()
                        .currency("xyz")
                        .unitAmount(1000)
                        .product(productId)
                        .build())
                .when().post(PRICES)
                .then()
                .statusCode(400)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.message", containsString("currency"));
    }

    // ── Retrieve Prices ───────────────────────────────────────────────────

    @Test(description = "TC-PRC-007: Retrieve Existing Price with Valid Id")
    @Story("Retrieve Price") @Severity(SeverityLevel.CRITICAL)
    @Description("GET /prices/{id} — expects 200, id matches, object=price, has required fields")
    public void tc_prc_007_retrieveExistingPrice() {
        Response res = given()
                .when().get(PRICES + "/" + priceId)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("id").toString()).isEqualTo(priceId);
        soft.assertThat(res.path("object").toString()).isEqualTo("price");
        soft.assertThat(res.path("currency").toString()).isNotBlank();
        soft.assertThat(res.path("unit_amount").toString()).isNotBlank();
        soft.assertAll();
    }

    @Test(description = "TC-PRC-008: Retrieve Price with Invalid Id")
    @Story("Retrieve Price") @Severity(SeverityLevel.NORMAL)
    @Description("GET /prices/invalidPrice — expects 404, error mentions No such price")
    public void tc_prc_008_retrievePriceWithInvalidId() {
        given()
                .when().get(PRICES + "/invalidPrice")
                .then()
                .statusCode(404)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("No such price"));
    }

    // ── Update Prices ─────────────────────────────────────────────────────

    @Test(description = "TC-PRC-009: Update Price Metadata")
    @Story("Update Price") @Severity(SeverityLevel.NORMAL)
    @Description("POST /prices/{id} with metadata — expects 200 and all metadata keys updated")
    public void tc_prc_009_updatePriceMetadata() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(PriceRequestBuilder.builder()
                        .metadata("order_id", "12345")
                        .metadata("tier", "premium")
                        .metadata("env", "testing")
                        .build())
                .when().post(PRICES + "/" + priceId)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("metadata.order_id", equalTo("12345"))
                .body("metadata.tier", equalTo("premium"))
                .body("metadata.env", equalTo("testing"));
    }

    @Test(description = "TC-PRC-010: Deactivate Price (active=false)")
    @Story("Update Price") @Severity(SeverityLevel.NORMAL)
    @Description("POST /prices/{id} with active=false — expects 200 and active=false")
    public void tc_prc_010_deactivatePrice() {
        String id = createAndExtract(PRICES,
                PriceRequestBuilder.builder()
                        .unitAmount(500)
                        .currency("usd")
                        .product(productId)
                        .build(), "id");
        createdPriceIds.add(id);

        given()
                .contentType(ContentType.URLENC)
                .formParams(PriceRequestBuilder.builder().active(false).build())
                .when().post(PRICES + "/" + id)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("active", equalTo(false));
    }

    // ── List Prices ───────────────────────────────────────────────────────

    @Test(description = "TC-PRC-011: List All Prices")
    @Story("List Prices") @Severity(SeverityLevel.NORMAL)
    @Description("GET /prices — expects 200, object=list, data not empty, each price has id and object")
    public void tc_prc_011_listAllPrices() {
        Response res = given()
                .when().get(PRICES)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("object", equalTo("list"))
                .extract().response();

        List<String> ids = res.jsonPath().getList("data.id");
        List<String> objects = res.jsonPath().getList("data.object");

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(ids).isNotEmpty();
        ids.forEach(id -> soft.assertThat(id).startsWith("price_"));
        objects.forEach(obj -> soft.assertThat(obj).isEqualTo("price"));
        soft.assertAll();
    }

    @Test(description = "TC-PRC-012: List Prices for Specific Product")
    @Story("List Prices") @Severity(SeverityLevel.NORMAL)
    @Description("GET /prices?product={id} — expects 200, all prices belong to the specified product")
    public void tc_prc_012_listPricesForSpecificProduct() {
        Response res = given()
                .queryParam("product", productId)
                .when().get(PRICES)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("object", equalTo("list"))
                .extract().response();

        List<String> products = res.jsonPath().getList("data.product");
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(products).isNotEmpty();
        products.forEach(p -> soft.assertThat(p).isEqualTo(productId));
        soft.assertAll();
    }
}
