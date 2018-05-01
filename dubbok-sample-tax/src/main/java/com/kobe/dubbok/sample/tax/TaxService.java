package com.kobe.dubbok.sample.tax;

import java.math.BigDecimal;

public interface TaxService {
	BigDecimal calculateTax(BigDecimal invoiceTotal);
}
