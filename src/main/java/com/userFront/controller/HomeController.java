package com.userFront.controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.userFront.dao.RoleDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.service.UserService;

@Controller
public class HomeController {
	
private static int count =0;
	@Autowired
	private UserService userService;
	
	@Autowired
    private RoleDao roleDao;
	
	
	@RequestMapping("/")
	public String home() {
		if(count == 0)
		{
			Role r1=new Role();
			r1.setRoleId(0);
			r1.setName("ROLE_USER");
			System.out.println(r1);
			roleDao.save(r1);
			
			Role r2=new Role();
			r2.setRoleId(1);
			r2.setName("ROLE_ADMIN");
			roleDao.save(r2);
		}
		count++;
		System.out.println(count);
		return "redirect:/index";
	}
	
	@RequestMapping("/index")
    public String index() {
        return "index";
    }
	
	@RequestMapping(value = "/signup", method = RequestMethod.GET)
    public String signup(Model model) {
		if(count == 0)
		{
			Role r1=new Role();
			r1.setRoleId(0);
			r1.setName("ROLE_USER");
			System.out.println(r1);
			roleDao.save(r1);
			
			Role r2=new Role();
			r2.setRoleId(1);
			r2.setName("ROLE_ADMIN");
			roleDao.save(r2);
		}
		count++;
		System.out.println(count);
		

		User user = new User();

        model.addAttribute("user", user);

        return "signup";
    }
	
	@RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String signupPost(@ModelAttribute("user") User user,  Model model, RedirectAttributes flashmap) {

		System.out.println(user);
        if(userService.checkUserExists(user.getUsername(), user.getEmail()))  {

            if (userService.checkEmailExists(user.getEmail())) {
                model.addAttribute("emailExists", true);
            }

            if (userService.checkUsernameExists(user.getUsername())) {
                model.addAttribute("usernameExists", true);
            }

            return "signup";
        } else {
        	 Set<UserRole> userRoles = new HashSet<>();
             userRoles.add(new UserRole(user, roleDao.findByName("ROLE_USER")));
             userService.createUser(user, userRoles);

            return "redirect:/";
        } 
		
    }
	
	@RequestMapping("/userFront")
	public String userFront(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName());
        PrimaryAccount primaryAccount = user.getPrimaryAccount();
        SavingsAccount savingsAccount = user.getSavingsAccount();
        model.addAttribute("user", user);
        model.addAttribute("primaryAccount", primaryAccount);
        model.addAttribute("savingsAccount", savingsAccount);

        return "userFront";
    }
}
