package com.example.jetpackcompose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransactionHistory {
    private static final TransactionHistory instance = new TransactionHistory();
    public static TransactionHistory getInstance() {
        return instance;
    }


    public List<Transaction> transactionList = new ArrayList<Transaction>(
            Arrays.asList(new Transaction("Jan 12, 2023", "Alex Johnson", "-₹150.00",R.drawable.img_2,"Buyer"),
            new Transaction("Feb 5, 2023", "Lisa Tran", "+₹250.00",R.drawable.img_2,"Seller"),
            new Transaction("Mar 3, 2023", "Michael Lee", "-₹320.00",R.drawable.img_2,"Buyer")
            )
    );

    public Set<String> qrID = new HashSet<String>();

}
