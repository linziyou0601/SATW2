package com.satw.demo.Normal;

@FunctionalInterface
public interface CreateNotifyLambda<U, V, W, X, Y, Z> {
    public void execute(U userWalletAddress, V txHash, W orderId, X type, Y title, Z description);
}