package com.userFront.service.UserServiceImpl;

import java.math.BigDecimal;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.userFront.dao.PrimaryAccountDao;
import com.userFront.dao.PrimaryTransactionDao;
import com.userFront.dao.RecipientDao;
import com.userFront.dao.SavingsAccountDao;
import com.userFront.dao.SavingsTransactionDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.Recipient;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

@Service
public class TransactionServiceImpl implements TransactionService {

	@Autowired
	private UserService userService;

	@Autowired
	private PrimaryTransactionDao primaryTransactionDao;

	@Autowired
	private SavingsTransactionDao savingsTransactionDao;

	@Autowired
	private PrimaryAccountDao primaryAccountDao;

	@Autowired
	private SavingsAccountDao savingsAccountDao;

	@Autowired
	private RecipientDao recipientDao;


	public List<PrimaryTransaction> findPrimaryTransactionList(String username){
		User user = userService.findByUsername(username);
		List<PrimaryTransaction> primaryTransactionList = user.getPrimaryAccount().getPrimaryTransactionList();

		return primaryTransactionList;
	}

	public List<SavingsTransaction> findSavingsTransactionList(String username) {
		User user = userService.findByUsername(username);
		List<SavingsTransaction> savingsTransactionList = user.getSavingsAccount().getSavingsTransactionList();

		return savingsTransactionList;
	}

	public void savePrimaryDepositTransaction(PrimaryTransaction primaryTransaction) {
		primaryTransactionDao.save(primaryTransaction);
	}

	public void saveSavingsDepositTransaction(SavingsTransaction savingsTransaction) {
		savingsTransactionDao.save(savingsTransaction);
	}

	public void savePrimaryWithdrawTransaction(PrimaryTransaction primaryTransaction) {
		primaryTransactionDao.save(primaryTransaction);
	}

	public void saveSavingsWithdrawTransaction(SavingsTransaction savingsTransaction) {
		savingsTransactionDao.save(savingsTransaction);
	}

	public boolean betweenAccountsTransfer(String transferFrom, String transferTo, String amount, PrimaryAccount primaryAccount, SavingsAccount savingsAccount) throws Exception {
		if (transferFrom.equalsIgnoreCase("Current") && transferTo.equalsIgnoreCase("Savings")) {
			primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(new BigDecimal(amount)));
			savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().add(new BigDecimal(amount)));
			primaryAccountDao.save(primaryAccount);
			savingsAccountDao.save(savingsAccount);

			Date date = new Date();

			PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Between account transfer from "+transferFrom+" to "+transferTo, "Account", "Finished", Double.parseDouble(amount), primaryAccount.getAccountBalance(), primaryAccount);
			primaryTransactionDao.save(primaryTransaction);
			return true;
		} else if (transferFrom.equalsIgnoreCase("Savings") && transferTo.equalsIgnoreCase("Current")) {
			primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().add(new BigDecimal(amount)));
			savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(new BigDecimal(amount)));
			primaryAccountDao.save(primaryAccount);
			savingsAccountDao.save(savingsAccount);

			Date date = new Date();

			SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Between account transfer from "+transferFrom+" to "+transferTo, "Transfer", "Finished", Double.parseDouble(amount), savingsAccount.getAccountBalance(), savingsAccount);
			savingsTransactionDao.save(savingsTransaction);
			return true;
		} else {
			return false;
		}
	}

	public List<Recipient> findRecipientList(Principal principal) {
		String username = principal.getName();
		List<Recipient> recipientList = recipientDao.findAll().stream() 			//convert list to stream
				.filter(recipient -> username.equals(recipient.getUser().getUsername()))	//filters the line, equals to username
				.collect(Collectors.toList());

		return recipientList;
	}

	public Recipient saveRecipient(Recipient recipient) {
		return recipientDao.save(recipient);
	}

	public Recipient findRecipientByName(String recipientName) {
		return recipientDao.findByName(recipientName);
	}

	public void deleteRecipientByName(String recipientName) {
		recipientDao.deleteByName(recipientName);
	}

	public boolean toSomeoneElseTransfer(Recipient recipient, String accountType, String amount, PrimaryAccount primaryAccount, SavingsAccount savingsAccount) {

		PrimaryAccount prec=primaryAccountDao.findByAccountNumber(Integer.parseInt(recipient.getAccountNumber()));
		SavingsAccount srec=savingsAccountDao.findByAccountNumber(Integer.parseInt(recipient.getAccountNumber()));
		if(prec!=null)
		{
			if (accountType.equalsIgnoreCase("Current")) {
				if(primaryAccount.getAccountBalance().compareTo(BigDecimal.valueOf(Double.parseDouble(amount)))>0)
				{
					primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(new BigDecimal(amount)));
					primaryAccountDao.save(primaryAccount);
					prec.setAccountBalance(prec.getAccountBalance().add(new BigDecimal(amount)));

					Date date = new Date();

					PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Debited to recipient "+recipient.getAccountNumber(), "Transfer", "Finished", Double.parseDouble(amount), primaryAccount.getAccountBalance(), primaryAccount);
					primaryTransactionDao.save(primaryTransaction);

					PrimaryTransaction recptTransaction = new PrimaryTransaction(date, "Credited from "+primaryAccount.getAccountNumber(), "Received", "Finished", Double.parseDouble(amount), prec.getAccountBalance(), prec);
					primaryTransactionDao.save(recptTransaction);
					return true;
				}
				else
				{
					return false;
				}

			} else if (accountType.equalsIgnoreCase("Savings")) {
				if(primaryAccount.getAccountBalance().compareTo(BigDecimal.valueOf(Double.parseDouble(amount)))>0)
				{
					savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(new BigDecimal(amount)));
					savingsAccountDao.save(savingsAccount);
					prec.setAccountBalance(prec.getAccountBalance().add(new BigDecimal(amount)));

					Date date = new Date();

					SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Debited to recipient "+recipient.getName(), "Transfer", "Finished", Double.parseDouble(amount), savingsAccount.getAccountBalance(), savingsAccount);
					savingsTransactionDao.save(savingsTransaction);

					PrimaryTransaction recptTransaction = new PrimaryTransaction(date, "Credited from "+primaryAccount.getAccountNumber(), "Received", "Finished", Double.parseDouble(amount), prec.getAccountBalance(), prec);
					primaryTransactionDao.save(recptTransaction);
					return true;
				}
				else
				{
					return false;
				}
			}
			return false;
		}
		else if(srec!=null)
		{
			if (accountType.equalsIgnoreCase("Current")) {
				if(primaryAccount.getAccountBalance().compareTo(BigDecimal.valueOf(Double.parseDouble(amount)))>0)
				{
					primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(new BigDecimal(amount)));
					primaryAccountDao.save(primaryAccount);
					srec.setAccountBalance(srec.getAccountBalance().add(new BigDecimal(amount)));

					Date date = new Date();

					PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Debited to recipient "+recipient.getAccountNumber(), "Transfer", "Finished", Double.parseDouble(amount), primaryAccount.getAccountBalance(), primaryAccount);
					primaryTransactionDao.save(primaryTransaction);

					SavingsTransaction recptTransaction = new SavingsTransaction(date, "Credited from "+primaryAccount.getAccountNumber(), "Received", "Finished", Double.parseDouble(amount), srec.getAccountBalance(), srec);
					savingsTransactionDao.save(recptTransaction);
					return true;
				}
				else
				{
					return false;
				}

			} else if (accountType.equalsIgnoreCase("Savings")) {
				if(primaryAccount.getAccountBalance().compareTo(BigDecimal.valueOf(Double.parseDouble(amount)))>0)
				{
					savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(new BigDecimal(amount)));
					savingsAccountDao.save(savingsAccount);
					srec.setAccountBalance(srec.getAccountBalance().add(new BigDecimal(amount)));

					Date date = new Date();

					SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Debited to recipient "+recipient.getName(), "Transfer", "Finished", Double.parseDouble(amount), savingsAccount.getAccountBalance(), savingsAccount);
					savingsTransactionDao.save(savingsTransaction);

					SavingsTransaction recptTransaction = new SavingsTransaction(date, "Credited from "+primaryAccount.getAccountNumber(), "Received", "Finished", Double.parseDouble(amount), srec.getAccountBalance(), srec);
					savingsTransactionDao.save(recptTransaction);
					return true;
				}
				else
				{
					return false;
				}
			}
			return false;
		}
		else
		{
			return false;
		}
	}
}
