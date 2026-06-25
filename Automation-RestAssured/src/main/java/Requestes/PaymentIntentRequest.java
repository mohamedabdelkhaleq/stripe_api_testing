package Requestes;

import java.util.HashMap;
import java.util.Map;

public class PaymentIntentRequest   {
    private String amount;
    private String currency;
    private String payment_Method;
    private String description;
    private boolean confirm;
    private String returnUrl;
    private String customer;

    public PaymentIntentRequest(String amount, String currency, String payment_Method,String description, boolean confirm, String returnUrl, String customer) {
        this.amount = amount;
        this.currency = currency;
        this.payment_Method = payment_Method;
        this.description = description;
        this.confirm = confirm;
        this.returnUrl = returnUrl;
        this.customer = customer;
    }
    //getters only


    public String getAmount() {
        return amount;
    }
    public String getCurrency() {
        return currency;
    }
    public String getPayment_Method() {
        return payment_Method;
    }
    public String getDescription() {
        return description;
    }
    public boolean isConfirm() {
        return confirm;
    }
    public String getCustomer() {
        return customer;
    }
    public String getReturnUrl() {
        return returnUrl;
    }
    public Map<String, String> create() {
        Map<String, String> params = new HashMap<String, String>();
        if (amount != null) {params.put("amount", amount);}
        if (currency != null) {params.put("currency", currency);}
        if (payment_Method != null) {params.put("payment_method", payment_Method);}
        if (description != null) {params.put("description", description);}
        if (confirm) {params.put("confirm",String.valueOf(confirm));}
        if (returnUrl != null) {params.put("return_url", returnUrl);}
        if (customer != null) {params.put("customer", customer);}
        return params;
    }
}
