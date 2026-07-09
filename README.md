# Stripe API Testing

API test suite for the [Stripe API](https://stripe.com/docs/api), combining **manual/exploratory testing in Postman** with a **Java + REST Assured automation framework**. Covers Customers, Payment Methods, Payment Intents, Charges, Refunds, Subscriptions, Invoices, Products, Prices, and Authentication, plus full end-to-end payment and subscription flows.

Latest automated run: **94 test cases, 100% pass rate** (Allure report).

## Tech Stack

| Layer | Tools |
|---|---|
| Automation | Java 23, REST Assured 6.0, TestNG 7.12 |
| Assertions | AssertJ (soft assertions), Hamcrest |
| Reporting | Allure 2.27 |
| Build | Maven (Surefire + Allure Maven plugin) |
| Logging | Log4j2 |
| Manual/Exploratory | Postman |

## Project Structure

```
stripe_api_testing/
├── Automation-RestAssured/
│   ├── src/main/java/
│   │   ├── config/            # StripeConfig – loads base URL & API keys from config.properties
│   │   ├── constants/         # StripeConstants – endpoints, statuses, test card/PM tokens
│   │   ├── Requestes/         # Request POJOs (ProductRequest, PaymentIntentRequest, etc.)
│   │   └── builders/          # Fluent builders for request payloads
│   ├── src/test/java/
│   │   ├── base/              # BaseTest – RestAssured setup, shared HTTP helpers, suite cleanup
│   │   ├── services/          # Service layer – reusable business flows per resource (CustomerService, PaymentIntentService, ...)
│   │   ├── auth/               # Authentication tests
│   │   ├── customers/         # Customers, Charges, Invoices, Payment Methods, Payment Intents, Refunds
│   │   ├── products/          # Product tests
│   │   ├── prices/            # Price tests
│   │   ├── subscriptions/     # Subscription tests
│   │   ├── E2E/               # Cross-resource end-to-end flows
│   │   └── dataReader/        # JSON test-data reader
│   └── src/test/resources/
│       ├── config.properties  # base.url / api.key / publishable.key (test-mode keys, gitignored)
│       ├── testng.xml         # Suite definition
│       └── test-data/         # JSON-driven test data (charges, payment intents, payment methods, refunds)
└── postman/
    └── collections/
        └── stripe_collection.json   # 155 requests across 11 folders (manual/exploratory coverage)
```

## Design Patterns

- **Builder Pattern** – `ProductBuilder`, `PriceRequestBuilder`, `PaymentIntentRequestBuilder`, `SubscriptionRequestBuilder`, etc. construct request payloads fluently and keep test code declarative.
- **Service Layer** – Each Stripe resource (`CustomerService`, `PaymentIntentService`, `RefundService`, `SubscriptionService`, ...) wraps the raw HTTP calls and built-in assertions, so tests and E2E flows call `service.create(...)` instead of repeating request/response boilerplate.
- **Base Test Layer** – `BaseTest` centralizes RestAssured configuration (base URI, preemptive auth, request/response logging), generic HTTP helpers (`createAndExtract`, `getResponse`, `deleteById`, ...), and `@AfterSuite` cleanup that deletes/archives resources created during the run.
- **Data-Driven Tests** – `JsonReader` feeds TestNG data providers from JSON files in `test-data/` for parameterized scenarios (charges, payment methods, payment intents, refunds).
- **E2E Composition** – E2E tests (`FullPaymentLifecycleTest`, `FullSubscriptionLifecycle`, `DeclinedCardFlow`) compose multiple services together to validate real multi-step flows (e.g., create product → create customer → attach payment method → create & confirm payment intent → verify charge → refund).

## Test Coverage

| Suite | Focus |
|---|---|
| Authentication | Valid/invalid API key handling |
| Products | CRUD, validation |
| Prices | CRUD, recurring pricing, validation |
| Customers | CRUD, validation |
| Payment Methods | Creation, attach/detach, card variants |
| Payment Intents | Creation, confirmation, status transitions |
| Charges | Creation, validation |
| Refunds | Full/partial refunds |
| Invoices | Invoice lifecycle |
| E2E Flows | Full payment lifecycle, subscription lifecycle, declined-card flow, auth boundary flow |

## Setup

1. **Prerequisites**: JDK 23, Maven, a [Stripe test-mode](https://dashboard.stripe.com/test/apikeys) secret key.
2. Configure credentials in `Automation-RestAssured/src/test/resources/config.properties`:
   ```properties
   base.url=https://api.stripe.com/v1
   api.key=sk_test_your_key_here
   publishable.key=pk_test_your_key_here
   ```
   This file is gitignored — never commit real keys.

## Running Tests

From `Automation-RestAssured/`:

```bash
# Run the full suite (defined in src/test/resources/testng.xml)
mvn clean test

# Generate and open the Allure report
mvn allure:report
mvn allure:serve
```

## Reporting

Test execution results are published with **Allure**, generated into `allure-results/` and rendered as an interactive HTML report showing suite/feature breakdowns, pass/fail trends, and per-test steps and logs (via RestAssured's request/response logging filters).

## Postman Collection

`postman/collections/stripe_collection.json` mirrors the automated coverage for manual/exploratory testing and quick debugging, organized into 11 folders (Customers, Payment Methods, Payment Intents, Charges, Products, Authentication, Prices, Subscriptions, Refunds, Invoices & Events, E2E Flows).

## Notes

- Test-suite cleanup (`@AfterSuite`) deletes customers/subscriptions and archives products created during the run to keep the Stripe test account tidy.
- `config.properties` ships with placeholder/test keys for local setup only — replace with your own Stripe test-mode keys before running.
