package com.example.demo;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login.html";
    }

    @Autowired
    private UserRepository userRepository;
@PostMapping("/login")
public String login(@RequestParam String email,
                    @RequestParam String password,
                    HttpSession session) {

    boolean validUser = userRepository.findByEmailAndPasswordAndActive(email, password, true).isPresent();

    if (validUser) {
        session.setAttribute("loggedInEmail", email);
        return "redirect:/menu.html"; // success
    } else {
        return "redirect:/login.html?error=true"; // static page error handling
    }
}

}
