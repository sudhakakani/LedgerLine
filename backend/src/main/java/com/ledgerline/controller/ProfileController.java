package com.ledgerline.controller;
import com.ledgerline.dto.Views;import com.ledgerline.repository.UserRepository;import java.security.Principal;import java.util.NoSuchElementException;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api") public class ProfileController {private final UserRepository users;public ProfileController(UserRepository u){users=u;}@GetMapping("/me") public Views.UserView me(Principal p){return Views.UserView.of(users.findByEmail(p.getName()).orElseThrow(()->new NoSuchElementException("User not found")));}}
