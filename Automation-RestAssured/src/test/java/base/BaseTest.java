package base;

import config.StripeConfig;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static constants.StripeConstants.*;
import static io.restassured.RestAssured.given;


public class BaseTest {

    @BeforeSuite
    public void globalSetup() {
        RestAssured.baseURI = StripeConfig.baseUrl();
        RestAssured.authentication =
                RestAssured.preemptive().basic(StripeConfig.apiKey(), "");
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    }
    private RequestSpecification setupRequest() {
        return given().contentType(ContentType.URLENC);
    }


    // Each test stores IDs of resources it created
    protected static List<String> createdCustomerIds = new ArrayList<>();
    protected static List<String> createdProductIds = new ArrayList<>();
    protected static List<String> createdSubscriptionIds = new ArrayList<>();
    protected static List<String> createdPaymentMethodsIds = new ArrayList<>();


    @AfterSuite
    public void cleanup() {

        // Delete all customers created in this test
        try {
            createdCustomerIds.forEach(id ->
                    given().delete(CUSTOMERS + "/" + id).then().statusCode(200));
        } finally {
            createdCustomerIds.clear();
        }

        // Cancel all subscriptions created in this test
        try {
            createdSubscriptionIds.forEach(id ->
                    given().delete(SUBSCRIPTIONS + "/" + id));
        } finally {
            createdSubscriptionIds.clear();
        }

        // products can't be deleted if they have prices, so we just archive them
        try {
            createdProductIds.forEach(id ->
                    setupRequest()
                    .formParam("active", "false")
                            .post(PRODUCTS + "/" + id));
        } finally {
            createdProductIds.clear();
        }
    }

    protected String createAndExtract(String endpoint,
                                      Map<String, String> body,
                                      String jsonPath) {
        return  setupRequest()
                .formParams(body)
                .when()
                .post(endpoint)
                .then()
                .extract().path(jsonPath);
    }
    protected Response createAndExtractResponse(String endpoint,
                                      Map<String, String> body) {
        return  setupRequest()
                .formParams(body)
                .when()
                .post(endpoint)
                .then()
                .log().all()
                .extract().response();
    }
    protected Response postActionOnId(String endpoint, Map<String, String> body , String id, String action) {
        return  setupRequest()
                .formParams(body)
                .when()
                .post(String.format("%s/%s/%s",endpoint,id,action))
                .then()
                .log().all()
                .extract().response();
    }
    protected Response postActionOnId(String endpoint, String id, String action) {
        return  setupRequest()
                .when()
                .post(endpoint + "/" + id + "/" + action)
                .then()
                .log().all()
                .extract().response();
    }
    protected Response extractErrorResponse(String endpoint,
                                            Map<String, String> body) {
        return  setupRequest()
                .formParams(body)
                .when()
                .post(endpoint)
                .then()
                .log().all()
                .statusCode(400)
                .extract().response();
    }
    protected Response getResponse(String endpoint) {
        return  setupRequest()
                .when()
                .get(endpoint)
                .then()
                .log().all()
                .extract().response();
    }
    protected Response getResponse(String endpoint,String id) {
        return setupRequest()
                .when()
                .get(endpoint.concat("/" + id))
                .then()
                .log().all()
                .extract().response();
    }
    protected Response InquireById(String endpoint, String id) {
        return  setupRequest()
                .when()
                .get(endpoint + "?" + id)
                .then()
                .log().all()
                .extract().response();

    }




}
