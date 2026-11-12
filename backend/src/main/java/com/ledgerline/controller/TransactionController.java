package com.ledgerline.controller;
import com.ledgerline.dto.*;import com.ledgerline.service.LedgerService;import jakarta.validation.Valid;import java.security.Principal;import java.util.List;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api") public class TransactionController {private final LedgerService service;public TransactionController(LedgerService s){service=s;}
 @PostMapping("/accounts/{id}/deposit") public Views.TransactionView deposit(@PathVariable Long id,@Valid @RequestBody Requests.Money r,@RequestHeader(value="Idempotency-Key",required=true) String k,Principal p){return service.deposit(p.getName(),id,r,k);}
 @PostMapping("/accounts/{id}/withdraw") public Views.TransactionView withdraw(@PathVariable Long id,@Valid @RequestBody Requests.Money r,@RequestHeader(value="Idempotency-Key",required=true) String k,Principal p){return service.withdraw(p.getName(),id,r,k);}
 @PostMapping("/transfers") public Views.TransactionView transfer(@Valid @RequestBody Requests.Transfer r,@RequestHeader(value="Idempotency-Key",required=true) String k,Principal p){return service.transfer(p.getName(),r,k);}
 @GetMapping("/accounts/{id}/entries") public List<Views.EntryView> history(@PathVariable Long id,Principal p){return service.history(p.getName(),id);}}
