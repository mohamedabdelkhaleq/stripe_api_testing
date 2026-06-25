package products;

import base.BaseTest;
import builders.ProductRequestBuilder;
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
@Feature("Products")
public class ProductTests extends BaseTest {

    private String productId;
    private String productName;

    @BeforeClass
    public void createBaseProduct() {
        productName = "Test Product";
        productId = createAndExtract(PRODUCTS,
                ProductRequestBuilder.builder()
                        .name(productName)
                        .description("Base product for tests")
                        .build(), "id");
        // not tracking this — @AfterMethod would archive it after the first test
    }

    @AfterClass
    public void deleteBaseProduct() {
        if (productId != null) {
            given().delete(PRODUCTS + "/" + productId);
        }
    }

    // ── Create Products ───────────────────────────────────────────────────

    @Test(description = "TC-PR-001: Create Product with Valid Name")
    @Story("Create Product") @Severity(SeverityLevel.CRITICAL)
    @Description("POST /products with valid name — expects 200, id starts with prod_, active=true")
    public void tc_pr_001_createProductWithValidName() {
        Response res = given()
                .contentType(ContentType.URLENC)
                .formParams(ProductRequestBuilder.builder()
                        .name("Test Product")
                        .build())
                .when().post(PRODUCTS)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        createdProductIds.add(res.path("id"));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("id").toString()).startsWith("prod_");
        soft.assertThat(res.path("object").toString()).isEqualTo("product");
        soft.assertThat(res.path("name").toString()).isEqualTo("Test Product");
        soft.assertThat((Boolean) res.path("active")).isTrue();
        soft.assertAll();
    }

    @Test(description = "TC-PR-002: Create Product with Name and Description")
    @Story("Create Product") @Severity(SeverityLevel.NORMAL)
    @Description("POST /products with name and description — expects 200 and description saved")
    public void tc_pr_002_createProductWithNameAndDescription() {
        Response res = given()
                .contentType(ContentType.URLENC)
                .formParams(ProductRequestBuilder.builder()
                        .name("Test Product")
                        .description("This is a test description.")
                        .build())
                .when().post(PRODUCTS)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        createdProductIds.add(res.path("id"));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("id").toString()).startsWith("prod_");
        soft.assertThat(res.path("object").toString()).isEqualTo("product");
        soft.assertThat(res.path("description").toString()).isEqualTo("This is a test description.");
        soft.assertThat((Boolean) res.path("active")).isTrue();
        soft.assertAll();
    }

    @Test(description = "TC-PR-003: Create Product with Metadata")
    @Story("Create Product") @Severity(SeverityLevel.NORMAL)
    @Description("POST /products with metadata[color]=red — expects 200 and metadata saved")
    public void tc_pr_003_createProductWithMetadata() {
        Response res = given()
                .contentType(ContentType.URLENC)
                .formParams(ProductRequestBuilder.builder()
                        .name("Test Product")
                        .metadata("color", "red")
                        .build())
                .when().post(PRODUCTS)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        createdProductIds.add(res.path("id"));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("id").toString()).startsWith("prod_");
        soft.assertThat(res.path("metadata.color").toString()).isEqualTo("red");
        soft.assertAll();
    }

    @Test(description = "TC-PR-004: Create Product with Missing Name")
    @Story("Create Product") @Severity(SeverityLevel.NORMAL)
    @Description("POST /products with empty name — expects 400, error mentions name required")
    public void tc_pr_004_createProductWithMissingName() {
        given()
                .contentType(ContentType.URLENC)
                .formParam("name", "")
                .when().post(PRODUCTS)
                .then()
                .statusCode(400)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("name"));
    }

    // ── Retrieve Products ─────────────────────────────────────────────────

    @Test(description = "TC-PR-005: Get a Product with Valid ID")
    @Story("Retrieve Product") @Severity(SeverityLevel.CRITICAL)
    @Description("GET /products/{id} — expects 200, id matches, object=product, active=true")
    public void tc_pr_005_getProductWithValidId() {
        Response res = given()
                .when().get(PRODUCTS + "/" + productId)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("id").toString()).isEqualTo(productId);
        soft.assertThat(res.path("object").toString()).isEqualTo("product");
        soft.assertThat((Boolean) res.path("active")).isTrue();
        soft.assertAll();
    }

    @Test(description = "TC-PR-006: Get Product with Invalid ID")
    @Story("Retrieve Product") @Severity(SeverityLevel.NORMAL)
    @Description("GET /products/invalid_id — expects 404, error mentions No such product")
    public void tc_pr_006_getProductWithInvalidId() {
        given()
                .when().get(PRODUCTS + "/prod_invalid123")
                .then()
                .statusCode(404)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("No such product"));
    }

    // ── Update Products ───────────────────────────────────────────────────

    @Test(description = "TC-PR-007: Update Name of Product")
    @Story("Update Product") @Severity(SeverityLevel.NORMAL)
    @Description("POST /products/{id} with new name — expects 200 and updated name")
    public void tc_pr_007_updateProductName() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(ProductRequestBuilder.builder()
                        .name("Updated Product Name")
                        .build())
                .when().post(PRODUCTS + "/" + productId)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("id", matchesPattern("^prod_.*"))
                .body("object", equalTo("product"))
                .body("name", equalTo("Updated Product Name"))
                .body("active", equalTo(true));
    }

    @Test(description = "TC-PR-008: Update Description of Product")
    @Story("Update Product") @Severity(SeverityLevel.NORMAL)
    @Description("POST /products/{id} with new description — expects 200 and updated description")
    public void tc_pr_008_updateProductDescription() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(ProductRequestBuilder.builder()
                        .description("Updated Description")
                        .build())
                .when().post(PRODUCTS + "/" + productId)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("description", equalTo("Updated Description"));
    }

    @Test(description = "TC-PR-009: Deactivate Product")
    @Story("Update Product") @Severity(SeverityLevel.NORMAL)
    @Description("POST /products/{id} with active=false — expects 200 and active=false")
    public void tc_pr_009_deactivateProduct() {
        String id = createAndExtract(PRODUCTS,
                ProductRequestBuilder.builder().name("Product To Deactivate").build(), "id");
        createdProductIds.add(id);

        given()
                .contentType(ContentType.URLENC)
                .formParams(ProductRequestBuilder.builder().active(false).build())
                .when().post(PRODUCTS + "/" + id)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("id", matchesPattern("^prod_.*"))
                .body("object", equalTo("product"))
                .body("active", equalTo(false));
    }

    // ── List Products ─────────────────────────────────────────────────────

    @Test(description = "TC-PR-010: List all Products")
    @Story("List Products") @Severity(SeverityLevel.NORMAL)
    @Description("GET /products — expects 200, object=list, data array not empty")
    public void tc_pr_010_listAllProducts() {
        Response res = given()
                .when().get(PRODUCTS)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("object", equalTo("list"))
                .body("data", notNullValue())
                .extract().response();

        List<Object> data = res.jsonPath().getList("data");
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(data).isNotEmpty();
        soft.assertAll();
    }

    @Test(description = "TC-PR-011: List all Inactive Products")
    @Story("List Products") @Severity(SeverityLevel.NORMAL)
    @Description("GET /products?active=false — expects 200, all returned products are inactive")
    public void tc_pr_011_listAllInactiveProducts() {
        Response res = given()
                .queryParam("active", "false")
                .when().get(PRODUCTS)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("object", equalTo("list"))
                .extract().response();

        List<Boolean> activeFlags = res.jsonPath().getList("data.active");
        SoftAssertions soft = new SoftAssertions();
        activeFlags.forEach(flag -> soft.assertThat(flag).isFalse());
        soft.assertAll();
    }

    // ── Delete Products ───────────────────────────────────────────────────

    @Test(description = "TC-PR-012: Delete Existing Product")
    @Story("Delete Product") @Severity(SeverityLevel.CRITICAL)
    @Description("DELETE /products/{id} — expects 200, deleted=true, id matches")
    public void tc_pr_012_deleteExistingProduct() {
        String id = createAndExtract(PRODUCTS,
                ProductRequestBuilder.builder().name("Product To Delete").build(), "id");

        given()
                .when().delete(PRODUCTS + "/" + id)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("deleted", equalTo(true))
                .body("id", equalTo(id));
    }

    @Test(description = "TC-PR-013: Delete Product with Active Prices")
    @Story("Delete Product") @Severity(SeverityLevel.NORMAL)
    @Description("DELETE /products/{id} that has active prices — expects 400, error mentions price")
    public void tc_pr_013_deleteProductWithActivePrices() {
        // Create product then attach an active price to it
        String id = createAndExtract(PRODUCTS,
                ProductRequestBuilder.builder().name("Product With Price").build(), "id");

        // Create an active price linked to this product
        given()
                .contentType(ContentType.URLENC)
                .formParam("unit_amount", "1000")
                .formParam("currency", "usd")
                .formParam("product", id)
                .when().post(PRICES);

        // Attempting to delete a product with active prices should return 400
        given()
                .when().delete(PRODUCTS + "/" + id)
                .then()
                .statusCode(400)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("price"));
    }

    @Test(description = "TC-PR-014: Delete Non-Existent Product")
    @Story("Delete Product") @Severity(SeverityLevel.NORMAL)
    @Description("DELETE /products/invalidprod — expects 404, error mentions No such product")
    public void tc_pr_014_deleteNonExistentProduct() {
        given()
                .when().delete(PRODUCTS + "/invalidprod")
                .then()
                .statusCode(404)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("No such product"));
    }
}
