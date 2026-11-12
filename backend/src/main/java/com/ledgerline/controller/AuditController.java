package com.ledgerline.controller;
import com.ledgerline.entity.AuditEvent;import com.ledgerline.repository.AuditEventRepository;import java.util.List;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin/audit") public class AuditController {private final AuditEventRepository audits;public AuditController(AuditEventRepository a){audits=a;}@GetMapping @PreAuthorize("hasAnyRole('ADMINISTRATOR','AUDITOR')") public List<AuditEvent> list(){return audits.findTop200ByOrderByCreatedAtDesc();}}
