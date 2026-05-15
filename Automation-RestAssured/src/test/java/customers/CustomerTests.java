package customers;

import base.BaseTest;
import builders.CustomerRequestBuilder;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static constants.StripeConstants.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

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
        createdCustomerIds.add(customerId);
    }

    // ── Create Customers ──────────────────────────────────────────────────

    @Test(description = "TC-CUS-001: Create Customer with Email and Name")
    public void createCustomerWithEmailAndName() {
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
    public void createCustomerWithEmailOnly() {
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
    public void createCustomerWithAllOptionalFields() {
        String id = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder()
                        .email("cus003_" + System.currentTimeMillis() + "@example.com")
                        .name("Jane Doe")
                        .phone("+1234567890")
                        .description("VIP Customer")
                        .metadataOrderId("ORD-001")
                        .metadataPriority("high")
                        .build(), "id");
        createdCustomerIds.add(id);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(id).startsWith("cus_");
        soft.assertAll();
    }

    @Test(description = "TC-CUS-004: Create Customer with Invalid Email")
    public void createCustomerWithInvalidEmail() {
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParams(CustomerRequestBuilder.builder()
                        .email("invalid-email")
                        .name("John Doe")
                        .build())
                .when()
                .post(CUSTOMERS)
                .then()
                .statusCode(400);
    }

    @Test(description = "TC-CUS-005: Create Customer with Empty Body")
    public void createCustomerWithEmptyBody() {
        String id = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder()
                        .build(), "id");
        createdCustomerIds.add(id);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(id).startsWith("cus_");
        soft.assertAll();
    }

    // ── Retrieve Customers ────────────────────────────────────────────────

    @Test(description = "TC-CUS-006: Retrieve Existing Customer")
    public void retrieveExistingCustomer() {
        Response res = given()
                .when()
                .get(CUSTOMERS + "/" + customerId)
                .then()
                .statusCode(200)
                .extract().response();

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("id").toString()).isEqualTo(customerId);
        soft.assertThat(res.path("object").toString()).isEqualTo("customer");
        soft.assertThat(res.path("email").toString()).isEqualTo(customerEmail);
        soft.assertAll();
    }

    @Test(description = "TC-CUS-007: Retrieve Customer with Invalid ID")
    public void retrieveCustomerWithInvalidId() {
        given()
                .when()
                .get(CUSTOMERS + "/invalid_customer_id")
                .then()
                .statusCode(404);
    }

    @Test(description = "TC-CUS-008: Retrieve Customer with Empty ID")
    public void retrieveCustomerWithEmptyId() {
        given()
                .when()
                .get(CUSTOMERS + "/")
                .then()
                .statusCode(200);
    }

    // ── Update Customers ──────────────────────────────────────────────────

    @Test(description = "TC-CUS-009: Update Customer Email")
    public void updateCustomerEmail() {
        String newEmail = "updated_" + System.currentTimeMillis() + "@example.com";
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParams(CustomerRequestBuilder.builder()
                        .email(newEmail)
                        .build())
                .when()
                .post(CUSTOMERS + "/" + customerId)
                .then()
                .statusCode(200)
                .body("email", equalTo(newEmail));
    }

    @Test(description = "TC-CUS-010: Update Customer Name")
    public void updateCustomerName() {
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParams(CustomerRequestBuilder.builder()
                        .name("Updated Name")
                        .build())
                .when()
                .post(CUSTOMERS + "/" + customerId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Name"));
    }

    @Test(description = "TC-CUS-011: Update Customer Metadata")
    public void updateCustomerMetadata() {
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParams(CustomerRequestBuilder.builder()
                        .metadataOrderId("ORD-999")
                        .metadataPriority("low")
                        .build())
                .when()
                .post(CUSTOMERS + "/" + customerId)
                .then()
                .statusCode(200)
                .body("metadata.order_id", equalTo("ORD-999"));
    }

    @Test(description = "TC-CUS-012: Update Customer with Invalid Email")
    public void updateCustomerWithInvalidEmail() {
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParams(CustomerRequestBuilder.builder()
                        .email("not-an-email")
                        .build())
                .when()
                .post(CUSTOMERS + "/" + customerId)
                .then()
                .statusCode(400);
    }

    // ── List Customers ────────────────────────────────────────────────────

    @Test(description = "TC-CUS-013: List All Customers")
    public void listAllCustomers() {
        given()
                .when()
                .get(CUSTOMERS)
                .then()
                .statusCode(200)
                .body("object", equalTo("list"))
                .body("data", notNullValue());
    }

    @Test(description = "TC-CUS-014: List Customers with Limit")
    public void listCustomersWithLimit() {
        given()
                .queryParam("limit", 2)
                .when()
                .get(CUSTOMERS)
                .then()
                .statusCode(200)
                .body("data.size()", lessThanOrEqualTo(2));
    }

    @Test(description = "TC-CUS-015: List Customers by Email")
    public void listCustomersByEmail() {
        given()
                .queryParam("email", customerEmail)
                .when()
                .get(CUSTOMERS)
                .then()
                .statusCode(200)
                .body("data[0].email", equalTo(customerEmail));
    }

    // ── Delete Customers ──────────────────────────────────────────────────

    @Test(description = "TC-CUS-016: Delete Customer")
    public void deleteCustomer() {
        String idToDelete = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder()
                        .email("delete_" + System.currentTimeMillis() + "@example.com")
                        .build(), "id");

        given()
                .when()
                .delete(CUSTOMERS + "/" + idToDelete)
                .then()
                .statusCode(200)
                .body("deleted", equalTo(true));
    }

    @Test(description = "TC-CUS-017: Delete Already Deleted Customer")
    public void deleteAlreadyDeletedCustomer() {
        String idToDelete = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder()
                        .email("delete2_" + System.currentTimeMillis() + "@example.com")
                        .build(), "id");

        // Delete once
        given().delete(CUSTOMERS + "/" + idToDelete).then().statusCode(200);

        // Delete again
        given()
                .when()
                .delete(CUSTOMERS + "/" + idToDelete)
                .then()
                .statusCode(404);
    }

    @Test(description = "TC-CUS-018: Delete Non-Existent Customer")
    public void deleteNonExistentCustomer() {
        given()
                .when()
                .delete(CUSTOMERS + "/invalid_customer_id")
                .then()
                .statusCode(404);
    }
}