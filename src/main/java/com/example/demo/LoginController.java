package com.example.demo;

import com.example.demo.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;
@PostMapping("/login")
public String login(@RequestParam String email,
                    @RequestParam String password,
                    HttpSession session,
                    Model model) {

    return userRepository.findByEmailAndPasswordAndActive(email, password, true)
           .map(user -> {
                session.setAttribute("loggedInEmail", email); // store email in session
                return "redirect:/menu.html"; // or /index
            })// successful login
            .orElseGet(() -> {
                RedirectAttributes redirectAttributes = null;
                redirectAttributes.addFlashAttribute("error", "Invalid email or password!");
                return "redirect:/login.html"; // redirect back on error
            });
}

}
