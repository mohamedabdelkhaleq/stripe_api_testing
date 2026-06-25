package E2E;

import base.BaseTest;
import org.testng.annotations.Test;
import services.*;

import static constants.StripeConstants.*;

public class FullPaymentLifecycleTest extends BaseTest {
    ProductAndPriceService  productService =
            new ProductAndPriceService();

    CustomerService customerService =
            new CustomerService();

    PaymentMethodService paymentMethodService =
            new PaymentMethodService();

    PaymentIntentService paymentIntentService =
            new PaymentIntentService();

    RefundService refundService =
            new RefundService();

    String amount = "5000";
    String refundAmount = "2500";
    @Test
    public void shouldCompleteFullPaymentLifecycle() {

        productService.createProduct("E2E Product");
        productService.createPrice();

        String customerId =
                customerService.createCustomer();

        String paymentMethodId =
                paymentMethodService
                        .createValid();

        paymentMethodService.attach(
                customerId,
                paymentMethodId);

        String paymentIntentId =
                paymentIntentService
                        .create(customerId,amount);


        String chargeId =
                paymentIntentService.confirm(
                        paymentIntentId,
                        paymentMethodId,STATUS_SUCCEEDED);

        paymentIntentService.verifyCharge(chargeId, amount, STATUS_SUCCEEDED);
        refundService.refund(
                refundAmount,
                chargeId);
    }
}

