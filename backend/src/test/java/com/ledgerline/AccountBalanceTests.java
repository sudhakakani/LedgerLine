package com.ledgerline;
import com.ledgerline.entity.*;import java.math.BigDecimal;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class AccountBalanceTests {
 @Test void debitCannotOverdraw(){AppUser u=new AppUser("Kakanisudha Lakshmi","kakanisudha@example.com","hash");Account a=new Account(u,"Daily","INR");a.credit(new BigDecimal("100.00"));assertThrows(IllegalArgumentException.class,()->a.debit(new BigDecimal("100.01")));assertEquals(new BigDecimal("100.00"),a.getBalance());}
 @Test void negativeOrZeroMovementsAreRejected(){AppUser u=new AppUser("Kakanisudha Lakshmi","kakanisudha@example.com","hash");Account a=new Account(u,"Daily","INR");assertThrows(IllegalArgumentException.class,()->a.credit(new BigDecimal("-1.00")));assertThrows(IllegalArgumentException.class,()->a.debit(BigDecimal.ZERO));assertEquals(new BigDecimal("0.00"),a.getBalance());}
 @Test void debitAndCreditPreserveExactDecimalAmounts(){AppUser u=new AppUser("Kakanisudha Lakshmi","kakanisudha@example.com","hash");Account a=new Account(u,"Daily","INR");a.credit(new BigDecimal("100.25"));a.debit(new BigDecimal("0.25"));assertEquals(new BigDecimal("100.00"),a.getBalance());}
}
