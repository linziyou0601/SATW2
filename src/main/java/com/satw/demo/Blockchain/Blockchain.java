package com.satw.demo.Blockchain;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import javax.annotation.PostConstruct;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.satw.demo.Controller.NotificationController;
import com.satw.demo.Dao.UserRepository;
import com.satw.demo.Model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Blockchain {

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationController notificationController ;

    @PostConstruct
    private void init(){
        INSTANCE = this; //Wiring Spring Boot Bean
    }

    // SINGLETON PATTERN
    private static Blockchain INSTANCE = new Blockchain();
    private Blockchain(){};
    public static Blockchain getInstance(){ return INSTANCE; }

    //------------------------------------Attributes------------------------------------
    public final int DIFFICULTY = 3;
    private LinkedList<Block> chain = new LinkedList<>();
    private HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    //------------------------------------Methods------------------------------------
    // Third Party
    public User getThirdParty(){ return userRepository.findFirstById(1); }
    public String getThirdPartyWalletAddress(){ return getThirdParty().getWalletAddress(); }

    // UTXOs
    public HashMap<String, TransactionOutput> getUTXOs(){ return UTXOs; }
    public void putUTXOs(String key, TransactionOutput transactionOutput){ UTXOs.put(key, transactionOutput); }
    public void removeUTXOs(String key){ UTXOs.remove(key); }
    
    // GET INFOMATION BY WALLET ADDRESS
    public int getBalance(String address){
        updateChain();
        int total = 0;
        for(TransactionOutput UTXO: UTXOs.values())
            if(UTXO.verifyOwner(address))
                total += UTXO.getAmount();
        return total;
    }
    public LinkedList<TransactionInput> getTxInputs(String address, int amount){
        LinkedList<TransactionInput> inputs = new LinkedList<>();
        int ownUTXOs = 0;
        //取得部分UTXO直到足夠支付本次交易的輸出
        for(TransactionOutput UTXO: UTXOs.values()){
            if(UTXO.verifyOwner(address)){
                inputs.add(new TransactionInput(UTXO.getHash()));
                ownUTXOs += UTXO.getAmount();
            }
            if(ownUTXOs >= amount) break;
        }
        return inputs;
    }
    public LinkedList<Transaction> getTransactions(String address){
        LinkedList<Transaction> txs = new LinkedList<>();
        for(Block block: chain)
            for(Transaction tx: block.getTransactions())
                if(tx.verifyOwner(address))
                    txs.add(tx);
        Collections.reverse(txs);
        return txs;
    }

    // BLOCKCHAIN
    public LinkedList<Block> getChain(){ return chain; }
    public void updateChain(){
        chain = readChain();
        if(verifyChain(chain)) packTransaction();
    }
    public void replaceChain(LinkedList<Block> newChain){
        chain = readChain();
        if(verifyChain(newChain) && newChain.size() > chain.size()){
            chain = newChain;
            writeChain(chain);
            removeVerifiedTransaction();
        }
        updateChain();
    }
    public void removeVerifiedTransaction(){
        LinkedList<Block> bc = chain;
        Collections.reverse(bc);
        for(Block block: bc){
            for(Transaction tx: block.getTransactions()){
                LinkedList<Transaction> unverifiedTransactions = readUnverifiedTransactions();
                if(unverifiedTransactions.size()>0){
                    int index = unverifiedTransactions.indexOf(tx);
                    Transaction upTx = unverifiedTransactions.get(index);
                    if(upTx!=null){
                        if(tx.getClassType().equals("Payment")){
                            notificationController.createNotify(((Payment)tx).getPayerAddress(), tx.getHash(), ((Payment)tx).getOrderId(), "Payment Paid", "Payment Paid", tx.getDetail());
                            notificationController.createNotify(((Payment)tx).getReceiverAddress(), tx.getHash(), ((Payment)tx).getOrderId(), "Payment Received", "Payment Received", tx.getDetail());
                        }
                        else if(tx.getClassType().equals("Deposit")){
                            notificationController.createNotify(((Deposit)tx).getReceiverAddress(), tx.getHash(), 0, "Deposit", "Deposit Coin", tx.getDetail());
                        }
                        else if(tx.getClassType().equals("Withdraw")){
                            notificationController.createNotify(((Withdraw)tx).getPayerAddress(), tx.getHash(), 0, "Withdraw", "Withdraw Money", tx.getDetail());
                        }
                    }
                    removeUnverifiedTransaction(tx);
                }
            }
        } 
    }
    public LinkedList<Block> readChain(){
        LinkedList<Block> bc = new LinkedList<>();
        try {
            String path = System.getProperty("user.home") + File.separator + "Blockchain";
            File file = new File(path, "blockchain.json");
            if (!file.exists()) file.createNewFile();
            FileReader fileReader = new FileReader(file.getAbsolutePath());
            
            Gson gson = new GsonBuilder().registerTypeAdapter(Transaction.class, new TransactionJsonDeserializer()).create();
            bc = gson.fromJson(fileReader, new TypeToken<LinkedList<Block>>(){}.getType());
            if(bc!=null) bc.sort(Comparator.comparingLong(Block::getTimestamp));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(bc==null) bc = new LinkedList<>();
        return bc;
    }
    public void writeChain(LinkedList<Block> bc){
        Gson gson = new Gson();
        try {
            String path = System.getProperty("user.home") + File.separator + "Blockchain";
            File file = new File(path, "blockchain.json");
            if (!file.exists()) file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());

            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(gson.toJson(bc));
            bw.close();
        } catch (IOException e) {
			e.printStackTrace();
		}
    }

    // UNVERIFIED TRANSACTIONS
    public void addUnverifiedTransaction(Transaction newTransacion) {
        LinkedList<Transaction> unverifiedTransactions = readUnverifiedTransactions();
        unverifiedTransactions.add(newTransacion);
        writeUnverifiedTransactions(unverifiedTransactions);
    }
    public void removeUnverifiedTransaction(Transaction transaction){
        LinkedList<Transaction> unverifiedTransactions = readUnverifiedTransactions();
        int index = unverifiedTransactions.indexOf(transaction);
        unverifiedTransactions.remove(index);
        writeUnverifiedTransactions(unverifiedTransactions);
    }
    public LinkedList<Transaction> readUnverifiedTransactions(){
        LinkedList<Transaction> ut = new LinkedList<>();
        try {
            String path = System.getProperty("user.home") + File.separator + "Blockchain";
            File file = new File(path, "unverifiedTransactions.json");
            if (!file.exists()) file.createNewFile();
            FileReader fileReader = new FileReader(file.getAbsolutePath());
            
            Gson gson = new GsonBuilder().registerTypeAdapter(Transaction.class, new TransactionJsonDeserializer()).create();
            ut = gson.fromJson(fileReader, new TypeToken<LinkedList<Transaction>>(){}.getType());
            if(ut!=null) ut.sort(Comparator.comparingLong(Transaction::getTimestamp));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(ut==null) ut = new LinkedList<>();
        return ut;
    }
    public void writeUnverifiedTransactions(LinkedList<Transaction> ut){
        Gson gson = new Gson();
        try {
            String path = System.getProperty("user.home") + File.separator + "Blockchain";
            File file = new File(path, "unverifiedTransactions.json");
            if (!file.exists()) file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());

            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(gson.toJson(ut));
            bw.close();
        } catch (IOException e) {
			e.printStackTrace();
		}
    }

    // PACK TRANSACTIONS
    public String lastBlockHash(){ return chain.size()>0? chain.getLast().getHash(): "0"; }
    public void packTransaction() {
        LinkedList<Transaction> unverifiedTransactions = readUnverifiedTransactions();
        while(unverifiedTransactions!=null && unverifiedTransactions.size() > 0){
            Block newBlock = new Block(lastBlockHash());
            newBlock.addTransaction(unverifiedTransactions.pop());
            if(newBlock.getTransactions().size()>=3 || unverifiedTransactions.size()==0)
                addBlock(newBlock);
        }
    }
    public void addBlock(Block newBlock) {
        newBlock.mineBlock(DIFFICULTY);
        LinkedList<Block> newChain = chain;
        newChain.add(newBlock);
        replaceChain(newChain);
    }


    // ------------------VERIFY------------------
    public boolean verifyChain(LinkedList<Block> chain) {
        if(chain!=null) chain.sort(Comparator.comparingLong(Block::getTimestamp));
        Block currentBlock, previousBlock;
        String hashTarget = String.join("", Collections.nCopies(DIFFICULTY, "0"));
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
        int sequence = 0;

        //驗證所有區塊Hash值
        for(int i=0; i < chain.size(); i++) {
            currentBlock = chain.get(i);
            previousBlock = i>0? chain.get(i-1): null;

            //驗證區塊Hash值
            if(!currentBlock.getHash().equals(currentBlock.calculateHash())){
                System.out.println("[x] 區塊(" + currentBlock.getHash() + ") 的Hash值有誤！");  //prompt
                return false;
            }
            if(previousBlock!=null && !previousBlock.getHash().equals(currentBlock.getPreviousHash())){
                System.out.println("[x] 區塊(" + currentBlock.getHash() + ") 的PreviousHash值有誤！");  //prompt
                return false;
            }
            if(!currentBlock.getHash().substring(0, DIFFICULTY).equals(hashTarget)){
                System.out.println("[x] 區塊(" + currentBlock.getHash() + ") 的Hash前綴0數目有誤！");  //prompt
                return false;
            }

            //驗證區塊中所有交易 （按鏈->區塊->交易 順序驗，跑一次整個鏈的交易流程）
            TransactionOutput tempOutput;
            for(int t=0; t < currentBlock.getTransactions().size(); t++) {
                Transaction currentTransaction = currentBlock.getTransactions().get(t);
                if(currentTransaction.getSequence()>=sequence)
                    sequence = currentTransaction.getSequence()+1;

                if(!currentTransaction.verifySignature()) {
                    System.out.println("[x] 交易(" + currentTransaction.getHash() + ") 的簽章不合法！");   //prompt
                    return false;
                }

                if(currentTransaction.getClassType().equals("Payment")) {
                    if (currentTransaction.getInputsAmount() != currentTransaction.getOutputsAmount()) {
                        System.out.println("[x] 交易(" + currentTransaction.getHash() + ") 的輸入與輸出金額不相等！"); //prompt
                        return false;
                    }
                }

                if(!(currentTransaction.getClassType().equals("Deposit"))) {
                    for(TransactionInput input : currentTransaction.getInputs()) {
                        tempOutput = tempUTXOs.get(input.getSourceOutputHash());
                        if (tempOutput == null) {
                            System.out.println("[x] 交易(" + currentTransaction.getHash() + ") 的金流來源遺失！"); //prompt
                            return false;
                        }
                        if (input.getAmount() != tempOutput.getAmount()) {
                            System.out.println("[x] 交易(" + currentTransaction.getHash() + ") 的金流有問題！");   //prompt
                            return false;
                        }
                        tempUTXOs.remove(input.getSourceOutputHash());
                    }
                }

                if(currentTransaction.getOutputs()!=null) {
                    //此交易所有輸出的UTXO再放入tempUTXOs
                    for(TransactionOutput output: currentTransaction.getOutputs())
                        tempUTXOs.put(output.getHash(), output);
                }
            }
        }
        System.out.println("[v] 區塊鏈驗證成功！"); //prompt
        UTXOs = tempUTXOs;
        Transaction.nonce = sequence;
        return true;
    }
}