package E2E;

import base.BaseTest;
import io.qameta.allure.*;
import org.junit.Assert;
import org.testng.annotations.Test;
import services.CustomerService;
import services.PaymentIntentService;
import services.PaymentMethodService;

import static constants.StripeConstants.STATUS_CARD_DECLINE;
import static constants.StripeConstants.STATUS_FAILED;
@Epic("Stripe API Automation")
@Feature("End-to-End Flows")
@Story("Declined Card Flow")
public class DeclinedCardFlow extends BaseTest {
    CustomerService customerService =
            new CustomerService();

    PaymentMethodService paymentMethodService =
            new PaymentMethodService();

    PaymentIntentService paymentIntentService =
            new PaymentIntentService();
    String amount = "2000";
    @Test(description = "E2E-003: Payment with declined card")
    @Severity(SeverityLevel.BLOCKER)
    @Description("""
            Verify the complete payment flow when using a declined card.
            Expected:
            - PaymentIntent status = failed
            - Charge status = failed
            - Failure code = card_declined
            """)
    public void DeclinedCardFlowTest(){


        // Step 1: Create customer
        String customerId =
                customerService.createCustomer();

        Assert.assertNotNull(customerId);

        // Step 2: Create declined payment method
        String paymentMethodId =
                paymentMethodService.createInvalid();

        Assert.assertNotNull(paymentMethodId);

        // Step 3: Attach payment method
        paymentMethodService.attachInvalid(
                customerId,
                paymentMethodId);

        // Step 4: Create PaymentIntent
        String paymentIntentId =
                paymentIntentService.create(
                        customerId,
                        amount);

        Assert.assertNotNull(paymentIntentId);

        // Step 5: Confirm PaymentIntent
        String chargeId =
                paymentIntentService.confirmInvalid(
                        paymentIntentId,
                        paymentMethodId,
                        STATUS_FAILED
                );

//        Assert.assertNotNull(chargeId);
        // Step 6: Verify Charge
        paymentIntentService.verifyCharge(
                chargeId,
                amount,
                STATUS_FAILED
        );

        // Step 7: Verify failure code
        String failureCode =
                paymentIntentService.getFailureCode(
                        chargeId);

        Assert.assertEquals(
                failureCode,
                STATUS_CARD_DECLINE
        );
    }

    }
