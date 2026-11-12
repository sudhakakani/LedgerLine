package com.ledgerline.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public final class Requests { private Requests(){}
 public record Register(@NotBlank @Size(max=120) String fullName,@NotBlank @Email @Size(max=180) String email,@NotBlank @Size(min=8,max=72) String password){}
 public record CreateAccount(@NotBlank @Size(max=80) String name,@NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency){}
 public record Money(@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount,@Size(max=240) String description){}
 public record Transfer(@NotNull Long fromAccountId,@NotNull Long toAccountId,@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount,@Size(max=240) String description){}
}
