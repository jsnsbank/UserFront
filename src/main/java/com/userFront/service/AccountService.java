package com.userFront.service;

import java.security.Principal;



import com.userFront.domain.PrimaryAccount;

import com.userFront.domain.SavingsAccount;

public interface AccountService {
	PrimaryAccount createPrimaryAccount();
    SavingsAccount createSavingsAccount();
    boolean deposit(String accountType, double amount, Principal principal);
    boolean withdraw(String accountType, double amount, Principal principal);
    
    
}
