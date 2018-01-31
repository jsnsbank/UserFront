package com.userFront.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.Recipient;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.User;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

@Controller
@RequestMapping("/transfer")
public class TransferController {

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/betweenAccounts", method = RequestMethod.GET)
	public String betweenAccounts(Model model, Principal principal) {
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("transferFrom", "");
		model.addAttribute("transferTo", "");
		model.addAttribute("amount", "");
		model.addAttribute("user", user);

		return "betweenAccounts";
	}

	@RequestMapping(value = "/betweenAccounts", method = RequestMethod.POST)
	public String betweenAccountsPost(@ModelAttribute("transferFrom") String transferFrom,@ModelAttribute("transferTo") String transferTo,@ModelAttribute("amount") String amount,Principal principal) throws Exception {
		if(Double.parseDouble(amount)>0)
		{
			User user = userService.findByUsername(principal.getName());
			PrimaryAccount primaryAccount = user.getPrimaryAccount();
			SavingsAccount savingsAccount = user.getSavingsAccount();
			boolean stat=transactionService.betweenAccountsTransfer(transferFrom, transferTo, amount, primaryAccount, savingsAccount);
			if(stat)
			{
				return "redirect:/userFront?transfersucc";
			}
			else
			{
				return "redirect:/userFront?transferfail";
			}
		}
		else
		{
			return "redirect:/userFront?neg";
		}
	}

	@RequestMapping(value = "/recipient", method = RequestMethod.GET)
	public String recipient(Model model, Principal principal) {
		List<Recipient> recipientList = transactionService.findRecipientList(principal);
		User user = userService.findByUsername(principal.getName());
		Recipient recipient = new Recipient();

		model.addAttribute("recipientList", recipientList);
		model.addAttribute("recipient", recipient);
		model.addAttribute("user", user);

		return "recipient";
	}

	@RequestMapping(value = "/recipient/save", method = RequestMethod.POST)
	public String recipientPost(@ModelAttribute("recipient") Recipient recipient, Principal principal) {

		User user = userService.findByUsername(principal.getName());
		recipient.setUser(user);
		transactionService.saveRecipient(recipient);
		return "redirect:/transfer/recipient";
	}

	@RequestMapping(value = "/recipient/edit", method = RequestMethod.GET)
	public String recipientEdit(@RequestParam(value = "recipientName") String recipientName, Model model, Principal principal){
		User user = userService.findByUsername(principal.getName());
		Recipient recipient = transactionService.findRecipientByName(recipientName);
		List<Recipient> recipientList = transactionService.findRecipientList(principal);

		model.addAttribute("recipientList", recipientList);
		model.addAttribute("recipient", recipient);
		model.addAttribute("user", user);
		return "recipient";
	}

	@RequestMapping(value = "/recipient/delete", method = RequestMethod.GET)
	@Transactional
	public String recipientDelete(@RequestParam(value = "recipientName") String recipientName, Model model, Principal principal){


		transactionService.deleteRecipientByName(recipientName);

		List<Recipient> recipientList = transactionService.findRecipientList(principal);

		Recipient recipient = new Recipient();
		model.addAttribute("recipient", recipient);
		model.addAttribute("recipientList", recipientList);
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user", user);
		return "recipient";
	}

	@RequestMapping(value = "/toSomeoneElse",method = RequestMethod.GET)
	public String toSomeoneElse(Model model, Principal principal) {
		List<Recipient> recipientList = transactionService.findRecipientList(principal);

		model.addAttribute("recipientList", recipientList);
		model.addAttribute("accountType", "");
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user", user);
		return "toSomeoneElse";
	}

	@RequestMapping(value = "/toSomeoneElse",method = RequestMethod.POST)
	public String toSomeoneElsePost(@ModelAttribute("recipientName") String recipientName, @ModelAttribute("accountType") String accountType, @ModelAttribute("amount") String amount, Principal principal) {
		if(Double.parseDouble(amount)>0)
		{
			User user = userService.findByUsername(principal.getName());
			Recipient recipient = transactionService.findRecipientByName(recipientName);
			boolean transactionStatus=transactionService.toSomeoneElseTransfer(recipient, accountType, amount, user.getPrimaryAccount(), user.getSavingsAccount());
			if(transactionStatus)
			{
				return "redirect:/userFront?transsucc";
			}
			else
			{
				return "redirect:/userFront?transfail";
			}
		}
		else
		{
			return "redirect:/userFront?neg";
		}
	}
}
