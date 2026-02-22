package com.jpmc.midascore.foundation; //This is a simple POJO class that represents the balance of a user. It has a single field amount that stores the balance amount. It also has a default constructor, a parameterized constructor, getters and setters for the amount field, and a toString method for debugging purposes. The @JsonIgnoreProperties annotation is used to ignore any unknown properties when deserializing JSON data into this class.

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Balance {
    private float amount;

    public Balance() {
    }

    public Balance(float amount) {
        this.amount = amount;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Balance {amount=" + amount + "}";
    }
}