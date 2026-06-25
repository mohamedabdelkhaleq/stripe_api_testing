package auth;

import base.BaseTest;
import config.StripeConfig;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import static constants.StripeConstants.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Stripe API")
@Feature("Authentication")
public class AuthTests extends BaseTest {

    @Test(description = "TC-AUTH-001: GET /v1/account - Valid Key")
    @Story("Authentication") @Severity(SeverityLevel.BLOCKER)
    @Description("GET /account with valid sk_test_ key — expects 200, id starts with acct_, object=account")
    public void tc_auth_001_validKeyReturns200() {
        Response res = given()
                .when().get(ACCOUNT)
                .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .extract().response();

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(res.path("id").toString()).startsWith("acct_");
        soft.assertThat(res.path("object").toString()).isEqualTo("account");
        soft.assertAll();
    }

    @Test(description = "TC-AUTH-002: GET /v1/account - Invalid Key")
    @Story("Authentication") @Severity(SeverityLevel.CRITICAL)
    @Description("GET /account with invalid key — expects 401, error.type=invalid_request_error, message mentions Invalid API Key")
    public void tc_auth_002_invalidKeyReturns401() {
        given()
                .auth().preemptive().basic("invalidkey123", "")
                .when().get(ACCOUNT)
                .then()
                .statusCode(401)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("Invalid API Key provided"));
    }

    @Test(description = "TC-AUTH-003: GET /v1/account - No API Key")
    @Story("Authentication") @Severity(SeverityLevel.CRITICAL)
    @Description("GET /account with no auth — expects 401, message mentions You did not provide an API key")
    public void tc_auth_003_noApiKeyReturns401() {
        given()
                .auth().none()
                .when().get(ACCOUNT)
                .then()
                .statusCode(401)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("You did not provide an API key"));
    }

    @Test(description = "TC-AUTH-004: GET /v1/account - Empty Key")
    @Story("Authentication") @Severity(SeverityLevel.NORMAL)
    @Description("GET /account with empty Bearer token — expects 401, message mentions You did not provide an API key")
    public void tc_auth_004_emptyKeyReturns401() {
        given()
                .auth().preemptive().basic("", "")
                .when().get(ACCOUNT)
                .then()
                .statusCode(401)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("You did not provide an API key"));
    }

    @Test(description = "TC-AUTH-005: GET /v1/account - Publishable Key")
    @Story("Authentication") @Severity(SeverityLevel.NORMAL)
    @Description("GET /account with pk_test_ key — expects 403, error.code=secret_key_required")
    public void tc_auth_005_publishableKeyReturns403() {
        given()
                .auth().preemptive().basic(StripeConfig.publishableKey(), "")
                .when().get(ACCOUNT)
                .then()
                .statusCode(403)
                .time(lessThan(MAX_RESPONSE_TIME_MS))
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.code", equalTo("secret_key_required"));
    }
}
