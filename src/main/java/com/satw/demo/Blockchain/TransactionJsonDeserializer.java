package com.satw.demo.Blockchain;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;

public class TransactionJsonDeserializer implements JsonDeserializer<Transaction> {
    @Override
    public Transaction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        String type = json.getAsJsonObject().get("classType").getAsString();
        switch(type) {
            case "Payment":
                return context.deserialize(json, Payment.class);
            case "Deposit":
                return context.deserialize(json, Deposit.class);
            default:
                return context.deserialize(json, Withdraw.class);
        }
    }
}