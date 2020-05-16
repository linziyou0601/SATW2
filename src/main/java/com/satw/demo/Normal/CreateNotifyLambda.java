package com.satw.demo.Normal;

@FunctionalInterface
public interface CreateNotifyLambda<Str1, Str2, Int1, Str3, Str4, Str5> {
    public void execute(Str1 userWalletAddress, Str2 txHash, Int1 orderId, Str3 type, Str4 title, Str5 description);
}