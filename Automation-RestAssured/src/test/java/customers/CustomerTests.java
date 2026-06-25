package customers;

import base.BaseTest;
import builders.CustomerRequestBuilder;
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
@Feature("Customers")
public class CustomerTests extends BaseTest {

    private String customerId;
    private String customerEmail;

    @BeforeClass
    public void createBaseCustomer() {
        customerEmail = "test_" + System.currentTimeMillis() + "@example.com";
        customerId = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder()
                        .email(customerEmail)
                        .name("Test Customer")
                        .build(), "id");
        // not tracking this one — @AfterMethod would kill it after the first test runs
    }

    @AfterClass
    public void deleteBaseCustomer() {
        if (customerId != null) {
            given().delete(CUSTOMERS + "/" + customerId);
        }
    }

    // ── Create Customers ──────────────────────────────────────────────────

    @Test(description = "TC-CUS-001: Create Customer with Email and Name")
    @Story("Create Customer") @Severity(SeverityLevel.CRITICAL)
    @Description("POST /customers with email and name — expects 200 and id starting with cus_")
    public void tc_cus_001_createCustomerWithEmailAndName() {
        String id = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder()
                        .email("cus001_" + System.currentTimeMillis() + "@example.com")
                        .name("John Doe")
                        .build(), "id");
        createdCustomerIds.add(id);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(id).startsWith("cus_");
        soft.assertAll();
    }

    @Test(description = "TC-CUS-002: Create Customer with Email Only")
    @Story("Create Customer") @Severity(SeverityLevel.NORMAL)
    @Description("POST /customers with email only — expects 200 and id starting with cus_")
    public void tc_cus_002_createCustomerWithEmailOnly() {
        String id = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder()
                        .email("cus002_" + System.currentTimeMillis() + "@example.com")
                        .build(), "id");
        createdCustomerIds.add(id);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(id).startsWith("cus_");
        soft.assertAll();
    }

    @Test(description = "TC-CUS-003: Create Customer with All Optional Fields")
    @Story("Create Customer") @Severity(SeverityLevel.NORMAL)
    @Description("POST /customers with email, name, phone, description, metadata — expects 200")
    public void tc_cus_003_createCustomerWithAllOptionalFields() {
        String id = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder()
                        .email("cus003_" + System.currentTimeMillis() + "@example.com")
                        .name("Jane Doe")
                        .phone("+1234567890")
                        .description("VIP Customer")
                        .metadata("order_id", "ORD-001")
                        .metadata("priority", "high")
                        .build(), "id");
        createdCustomerIds.add(id);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(id).startsWith("cus_");
        soft.assertAll();
    }

    @Test(description = "TC-CUS-004: Create Customer with Invalid Email")
    @Story("Create Customer") @Severity(SeverityLevel.NORMAL)
    @Description("POST /customers with invalid email format — expects 400 Bad Request")
    public void tc_cus_004_createCustomerWithInvalidEmail() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(CustomerRequestBuilder.builder()
                        .email("invalid-email")
                        .name("John Doe")
                        .build())
                .when().post(CUSTOMERS)
                .then()
                .statusCode(400);
    }

    @Test(description = "TC-CUS-005: Create Customer with Empty Body")
    @Story("Create Customer") @Severity(SeverityLevel.NORMAL)
    @Description("POST /customers with empty body — Stripe allows anonymous customers, expects 200")
    public void tc_cus_005_createCustomerWithEmptyBody() {
        String id = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder().build(), "id");
        createdCustomerIds.add(id);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(id).startsWith("cus_");
        soft.assertAll();
    }

    // ── Retrieve Customers ────────────────────────────────────────────────

    @Test(description = "TC-CUS-006: Retrieve Existing Customer")
    @Story("Retrieve Customer") @Severity(SeverityLevel.CRITICAL)
    @Description("GET /customers/{id} — expects 200, id, object=customer, correct email")
    public void tc_cus_006_retrieveExistingCustomer() {
        Response res = given()
                .when().get(CUSTOMERS + "/" + customerId)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("id").toString()).isEqualTo(customerId);
        soft.assertThat(res.path("object").toString()).isEqualTo("customer");
        soft.assertThat(res.path("email").toString()).isEqualTo(customerEmail);
        soft.assertAll();
    }

    @Test(description = "TC-CUS-007: Retrieve Customer with Invalid ID")
    @Story("Retrieve Customer") @Severity(SeverityLevel.NORMAL)
    @Description("GET /customers/invalid_id — expects 404 Not Found")
    public void tc_cus_007_retrieveCustomerWithInvalidId() {
        given()
                .when().get(CUSTOMERS + "/invalid_customer_id")
                .then()
                .statusCode(404)
                .time(lessThan(MAX_RESPONSE_TIME_MS));
    }

    @Test(description = "TC-CUS-008: Retrieve Customer with Empty ID")
    @Story("Retrieve Customer") @Severity(SeverityLevel.MINOR)
    @Description("GET /customers/ with trailing slash (empty ID) — expects 404 invalid_request_error")
    public void tc_cus_008_retrieveCustomerWithEmptyId() {
        given()
                .when().get(CUSTOMERS + "/")
                .then()
                .statusCode(404)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.type", equalTo("invalid_request_error"));
    }

    // ── Update Customers ──────────────────────────────────────────────────

    @Test(description = "TC-CUS-009: Update Customer Email")
    @Story("Update Customer") @Severity(SeverityLevel.NORMAL)
    @Description("POST /customers/{id} with new email — expects 200 and updated email in response")
    public void tc_cus_009_updateCustomerEmail() {
        String newEmail = "updated_" + System.currentTimeMillis() + "@example.com";
        given()
                .contentType(ContentType.URLENC)
                .formParams(CustomerRequestBuilder.builder().email(newEmail).build())
                .when().post(CUSTOMERS + "/" + customerId)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("email", equalTo(newEmail));
    }

    @Test(description = "TC-CUS-010: Update Customer Name")
    @Story("Update Customer") @Severity(SeverityLevel.NORMAL)
    @Description("POST /customers/{id} with new name — expects 200 and updated name in response")
    public void tc_cus_010_updateCustomerName() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(CustomerRequestBuilder.builder().name("Updated Name").build())
                .when().post(CUSTOMERS + "/" + customerId)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("name", equalTo("Updated Name"));
    }

    @Test(description = "TC-CUS-011: Update Customer Metadata")
    @Story("Update Customer") @Severity(SeverityLevel.NORMAL)
    @Description("POST /customers/{id} with metadata — expects 200 and all metadata keys updated")
    public void tc_cus_011_updateCustomerMetadata() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(CustomerRequestBuilder.builder()
                        .metadata("order_id", "12345")
                        .metadata("tier", "premium")
                        .metadata("last_purchase", "2024-01-15")
                        .build())
                .when().post(CUSTOMERS + "/" + customerId)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("metadata.order_id", equalTo("12345"))
                .body("metadata.tier", equalTo("premium"))
                .body("metadata.last_purchase", equalTo("2024-01-15"));
    }

    @Test(description = "TC-CUS-012: Update Customer with Invalid Email")
    @Story("Update Customer") @Severity(SeverityLevel.NORMAL)
    @Description("POST /customers/{id} with invalid email format — expects 400 Bad Request")
    public void tc_cus_012_updateCustomerWithInvalidEmail() {
        given()
                .contentType(ContentType.URLENC)
                .formParams(CustomerRequestBuilder.builder().email("not-an-email").build())
                .when().post(CUSTOMERS + "/" + customerId)
                .then()
                .statusCode(400);
    }

    // ── List Customers ────────────────────────────────────────────────────

    @Test(description = "TC-CUS-013: List All Customers")
    @Story("List Customers") @Severity(SeverityLevel.NORMAL)
    @Description("GET /customers — expects 200, object=list, data array not null")
    public void tc_cus_013_listAllCustomers() {
        given()
                .when().get(CUSTOMERS)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("object", equalTo("list"))
                .body("data", notNullValue());
    }

    @Test(description = "TC-CUS-014: List Customers with Limit")
    @Story("List Customers") @Severity(SeverityLevel.NORMAL)
    @Description("GET /customers?limit=2 — expects 200, data array size <= 2")
    public void tc_cus_014_listCustomersWithLimit() {
        given()
                .queryParam("limit", 2)
                .when().get(CUSTOMERS)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("data.size()", lessThanOrEqualTo(2));
    }

    @Test(description = "TC-CUS-015: List Customers by Email")
    @Story("List Customers") @Severity(SeverityLevel.NORMAL)
    @Description("GET /customers?email=... — expects 200, all results match the filtered email")
    public void tc_cus_015_listCustomersByEmail() {
        String searchEmail = "search_" + System.currentTimeMillis() + "@example.com";
        String id = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder().email(searchEmail).build(), "id");
        createdCustomerIds.add(id);

        Response res = given()
                .queryParam("email", searchEmail)
                .when().get(CUSTOMERS)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("object", equalTo("list"))
                .extract().response();

        List<String> emails = res.jsonPath().getList("data.email");
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(emails).isNotEmpty();
        emails.forEach(email -> soft.assertThat(email).isEqualTo(searchEmail));
        soft.assertAll();
    }

    // ── Delete Customers ──────────────────────────────────────────────────

    @Test(description = "TC-CUS-016: Delete Customer")
    @Story("Delete Customer") @Severity(SeverityLevel.CRITICAL)
    @Description("DELETE /customers/{id} — expects 200 and deleted=true in response")
    public void tc_cus_016_deleteCustomer() {
        String idToDelete = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder()
                        .email("delete_" + System.currentTimeMillis() + "@example.com")
                        .build(), "id");

        given()
                .when().delete(CUSTOMERS + "/" + idToDelete)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("deleted", equalTo(true));
    }

    @Test(description = "TC-CUS-017: Delete Already Deleted Customer")
    @Story("Delete Customer") @Severity(SeverityLevel.NORMAL)
    @Description("DELETE /customers/{id} twice — second delete expects 404 Not Found")
    public void tc_cus_017_deleteAlreadyDeletedCustomer() {
        String idToDelete = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder()
                        .email("delete2_" + System.currentTimeMillis() + "@example.com")
                        .build(), "id");

        given().delete(CUSTOMERS + "/" + idToDelete).then().statusCode(200);

        given()
                .when().delete(CUSTOMERS + "/" + idToDelete)
                .then()
                .statusCode(404)
                .time(lessThan(MAX_RESPONSE_TIME_MS));
    }

    @Test(description = "TC-CUS-018: Delete Non-Existent Customer")
    @Story("Delete Customer") @Severity(SeverityLevel.NORMAL)
    @Description("DELETE /customers/invalid_id — expects 404 Not Found")
    public void tc_cus_018_deleteNonExistentCustomer() {
        given()
                .when().delete(CUSTOMERS + "/invalid_customer_id")
                .then()
                .statusCode(404)
                .time(lessThan(MAX_RESPONSE_TIME_MS));
    }
}
