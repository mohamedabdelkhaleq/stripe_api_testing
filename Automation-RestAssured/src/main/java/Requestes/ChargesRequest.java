package Requestes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChargesRequest {
    private String id;
    private  String amount;
    private  String currency;
    private  String status;
    private  boolean paid;

    public String getId() {
        return id;
    }
    public boolean isPaid() {
        return paid;
    }

    public String getStatus() {
        return status;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }


}
