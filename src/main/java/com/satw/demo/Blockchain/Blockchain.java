package com.satw.demo.Blockchain;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import javax.annotation.PostConstruct;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.satw.demo.Controller.NotificationController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class Blockchain {

    @Autowired
    NotificationController notificationController;

    public int DIFFICULTY = 3;
    private LinkedList<Block> chain = new LinkedList<>();
    private HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    public static Blockchain blockchain;
    @PostConstruct
    public void init() {    
        blockchain = this;
    } 

    // UTXOs
    public static HashMap<String, TransactionOutput> getUTXOs(){ return blockchain.UTXOs; }
    public static void putUTXOs(String key, TransactionOutput transactionOutput){ blockchain.UTXOs.put(key, transactionOutput); }
    public static void removeUTXOs(String key){ blockchain.UTXOs.remove(key); }

    // UNVERIFIED TRANSACTIONS
    public static void addUnveriedTransaction(Transaction newTransacion) {
        LinkedList<Transaction> unverifiedTransactions = readUnverifiedTransactions();
        unverifiedTransactions.add(newTransacion);
        writeUnverifiedTransactions(unverifiedTransactions);
    }
    public static void removeUnverifiedTransaction(Transaction transaction){
        LinkedList<Transaction> unverifiedTransactions = readUnverifiedTransactions();
        int index = unverifiedTransactions.indexOf(transaction);
        unverifiedTransactions.remove(index);
        writeUnverifiedTransactions(unverifiedTransactions);
    }
    public static LinkedList<Transaction> readUnverifiedTransactions(){
        LinkedList<Transaction> ut = new LinkedList<>();
        try {
            String path = System.getProperty("user.home") + File.separator + "Blockchain";
            File file = new File(path, "unverifiedTransactions.json");
            if (!file.exists()) file.createNewFile();
            FileReader fileReader = new FileReader(file.getAbsoluteFile());
            Gson gson = new GsonBuilder().registerTypeAdapter(Transaction.class, new TransactionJsonDeserializer()).create();
            ut = gson.fromJson(fileReader, new TypeToken<LinkedList<Transaction>>(){}.getType());

            /*Resource resource = new ClassPathResource("unverifiedTransactions.json");
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            Gson gson = new GsonBuilder().registerTypeAdapter(Transaction.class, new TransactionJsonDeserializer()).create();
            ut = gson.fromJson(reader, new TypeToken<LinkedList<Transaction>>(){}.getType());*/
            /*Resource resource = new ClassPathResource("unverifiedTransactions.json");
            Gson gson = new GsonBuilder().registerTypeAdapter(Transaction.class, new TransactionJsonDeserializer()).create();
            ut = gson.fromJson(new FileReader(resource.getFile()), new TypeToken<LinkedList<Transaction>>(){}.getType());*/
            if(ut!=null) ut.sort(Comparator.comparingLong(Transaction::getTimestamp));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(ut==null) ut = new LinkedList<>();
        return ut;
    }
    public static void writeUnverifiedTransactions(LinkedList<Transaction> ut){
        Gson gson = new Gson();
        try {
            String path = System.getProperty("user.home") + File.separator + "Blockchain";
            File file = new File(path, "unverifiedTransactions.json");
            if (!file.exists()) file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(gson.toJson(ut));
            bw.close();
            /*Resource resource = new ClassPathResource("unverifiedTransactions.json");
            Path path = Paths.get(resource.getURI());
            OutputStream out = new FileOutputStream(path.toAbsolutePath().toString());
            Writer writer = new OutputStreamWriter(out, "UTF-8");
            writer.write(gson.toJson(ut));
            writer.close();*/
            /*Resource resource = new ClassPathResource("unverifiedTransactions.json");
            Writer unverifiedTransactionsWriter = new FileWriter(resource.getFile());
            gson.toJson(gson.toJson(ut), unverifiedTransactionsWriter);
			unverifiedTransactionsWriter.close();*/
        } catch (IOException e) {
			e.printStackTrace();
		}
    }

    // BLOCKCHAIN
    public static LinkedList<Block> getChain(){ return blockchain.chain; }
    public static void updateChain(){
        blockchain.chain = readChain();
        verifyChain(blockchain.chain);
        packTransaction();
    }
    public static void replaceChain(LinkedList<Block> newChain){
        blockchain.chain = readChain();
        if(verifyChain(newChain) && newChain.size() > blockchain.chain.size()){
            blockchain.chain = newChain;
            writeChain(blockchain.chain);
            removeVerifiedTransaction();
        }
        verifyChain(blockchain.chain);
    }
    public static void removeVerifiedTransaction(){
        LinkedList<Block> bc = blockchain.chain;
        Collections.reverse(bc);
        for(Block block: bc){
            for(Transaction tx: block.getTransactions()){
                LinkedList<Transaction> unverifiedTransactions = readUnverifiedTransactions();
                if(unverifiedTransactions.size()>0){
                    int index = unverifiedTransactions.indexOf(tx);
                    Transaction upTx = unverifiedTransactions.get(index);
                    if(upTx!=null){
                        if(tx.getClassType().equals("Payment")){
                            blockchain.notificationController.createNotify(((Payment)tx).getPayerId(), ((Payment)tx).getHash(), ((Payment)tx).getOrderId(), "Payment Paid", "Payment Paid", ((Payment)tx).getDetail());
                            blockchain.notificationController.createNotify(((Payment)tx).getReceiverId(), ((Payment)tx).getHash(), ((Payment)tx).getOrderId(), "Payment Received", "Payment Received", ((Payment)tx).getDetail());
                        }
                        else if(tx.getClassType().equals("Deposit")){
                            blockchain.notificationController.createNotify(((Deposit)tx).getReceiverId(), ((Deposit)tx).getHash(), 0, "Deposit", "Deposit Coin", ((Deposit)tx).getDetail());
                        }
                        else if(tx.getClassType().equals("Withdraw")){
                            blockchain.notificationController.createNotify(((Withdraw)tx).getPayerId(), ((Withdraw)tx).getHash(), 0, "Withdraw", "Withdraw Money", ((Withdraw)tx).getDetail());
                        }
                    }
                    removeUnverifiedTransaction(tx);
                }
            }
        } 
    }
    public static LinkedList<Block> readChain(){
        LinkedList<Block> bc = new LinkedList<>();
        try {
            String path = System.getProperty("user.home") + File.separator + "Blockchain";
            File file = new File(path, "blockchain.json");
            if (!file.exists()) file.createNewFile();
            FileReader fileReader = new FileReader(file.getAbsoluteFile());
            Gson gson = new GsonBuilder().registerTypeAdapter(Transaction.class, new TransactionJsonDeserializer()).create();
            bc = gson.fromJson(fileReader, new TypeToken<LinkedList<Transaction>>(){}.getType());
            /*Resource resource = new ClassPathResource("blockchain.json");
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            Gson gson = new GsonBuilder().registerTypeAdapter(Block.class, new BlockJsonDeserializer()).create();
            bc = gson.fromJson(reader, new TypeToken<LinkedList<Block>>(){}.getType());*/
            /*Resource resource = new ClassPathResource("blockchain.json");
            Gson gson = new GsonBuilder().registerTypeAdapter(Block.class, new BlockJsonDeserializer()).create();
            bc = gson.fromJson(new FileReader(resource.getFile()), new TypeToken<LinkedList<Block>>(){}.getType());*/
            if(bc!=null) bc.sort(Comparator.comparingLong(Block::getTimestamp));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(bc==null) bc = new LinkedList<>();
        return bc;
    }
    public static void writeChain(LinkedList<Block> bc){
        Gson gson = new Gson();
        try {
            String path = System.getProperty("user.home") + File.separator + "Blockchain";
            File file = new File(path, "blockchain.json");
            if (!file.exists()) file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(gson.toJson(bc));
            bw.close();
            
            /*Resource resource = new ClassPathResource("blockchain.json");
            File file = resource.getFile();
            if (!file.exists()) file.createNewFile();

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(gson.toJson(bc));
            bw.close();*/
            
            /*Resource resource = new ClassPathResource("blockchain.json");
            Writer blockchainWriter = new FileWriter(resource.getFile());
            gson.toJson(gson.toJson(bc), blockchainWriter);
			blockchainWriter.close();*/
        } catch (IOException e) {
			e.printStackTrace();
		}
    }

    // PACK TRANSACTIONS
    public static String lastBlockHash(){ return blockchain.chain.size()>0? blockchain.chain.getLast().getHash(): "0"; }
    public static void packTransaction() {
        LinkedList<Transaction> unverifiedTransactions = readUnverifiedTransactions();
        while(unverifiedTransactions!=null && unverifiedTransactions.size() > 0){
            Block newBlock = new Block(lastBlockHash());
            newBlock.addTransaction(unverifiedTransactions.pop());
            if(newBlock.getTransactions().size()>=3 || unverifiedTransactions.size()==0)
                addBlock(newBlock);
        }
    }
    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(blockchain.DIFFICULTY);
        LinkedList<Block> newChain = blockchain.chain;
        newChain.add(newBlock);
        replaceChain(newChain);
    }

    // VERIFY
    public static boolean verifyChain(LinkedList<Block> chain) {
        if(chain!=null) chain.sort(Comparator.comparingLong(Block::getTimestamp));
        Block currentBlock, previousBlock;
        String hashTarget = String.join("", Collections.nCopies(blockchain.DIFFICULTY, "0"));
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
            if(!currentBlock.getHash().substring(0, blockchain.DIFFICULTY).equals(hashTarget)){
                System.out.println("[x] 區塊(" + currentBlock.getHash() + ") 的Hash前綴0數目有誤！");  //prompt
                return false;
            }

            //驗證區塊中所有交易 （按鏈->區塊->交易 順序驗，跑一次整個鏈的交易流程）
            TransactionOutput tempOutput;
            for(int t=0; t < currentBlock.getTransactions().size(); t++) {
                Transaction currentTransaction = currentBlock.getTransactions().get(t);
                if(currentTransaction.getSequence()>=sequence)
                    sequence = currentTransaction.getSequence()+1;

                if(!currentTransaction.verifiySignature()) {
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
        blockchain.UTXOs = tempUTXOs;
        Transaction.nonce = sequence;
        return true;
    }
}