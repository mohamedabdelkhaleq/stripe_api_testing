package E2E;

import base.BaseTest;
import org.testng.annotations.Test;
import services.CustomerService;
import services.PaymentIntentService;
import services.PaymentMethodService;
import static constants.StripeConstants.STATUS_FAILED;

public class DeclinedCardFlow extends BaseTest {
    CustomerService customerService =
            new CustomerService();

    PaymentMethodService paymentMethodService =
            new PaymentMethodService();

    PaymentIntentService paymentIntentService =
            new PaymentIntentService();
    String amount = "2000";
    @Test
    public void DeclinedCardFlowTest(){

        String customerId =
                customerService.createCustomer();

        String paymentMethodId =
                paymentMethodService
                        .createInvalid();

        paymentMethodService.attach(
                customerId,
                paymentMethodId);

        String paymentIntentId =
                paymentIntentService
                        .create(customerId,amount);


        String chargeId =
                paymentIntentService.confirm(
                        paymentIntentId,
                        paymentMethodId,STATUS_FAILED);

        paymentIntentService.verifyCharge(chargeId, amount,STATUS_FAILED);

    }

    }
