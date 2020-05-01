package com.satw.demo.Blockchain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedList;

public class BlockJsonDeserializer implements JsonDeserializer<Block> {
    @Override
    public Block deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement transactionJSON = jsonObject.get("transactions");
        Gson gson = new GsonBuilder().registerTypeAdapter(Transaction.class, new TransactionJsonDeserializer()).create();

        LinkedList<Transaction> transactions = gson.fromJson(transactionJSON, new TypeToken<LinkedList<Transaction>>() {}.getType());
        String hash = jsonObject.get("hash").getAsString();
        String previousHash = jsonObject.get("previousHash").getAsString();
        String merkleRoot = jsonObject.get("merkleRoot").getAsString();
        int nonce = jsonObject.get("nonce").getAsInt();
        long timestamp = jsonObject.get("timestamp").getAsLong();

        Block block = new Block(hash, previousHash, merkleRoot, nonce, timestamp, transactions);
        return block;
    }
}