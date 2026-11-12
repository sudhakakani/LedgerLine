package com.ledgerline.controller;
import com.ledgerline.dto.*;import com.ledgerline.service.AuthService;import jakarta.validation.Valid;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth") public class AuthController {private final AuthService service;public AuthController(AuthService s){service=s;}@PostMapping("/register") public ResponseEntity<Views.UserView> register(@Valid @RequestBody Requests.Register r){return ResponseEntity.status(HttpStatus.CREATED).body(service.register(r));}}
