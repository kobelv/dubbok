package com.kobe.dubbok.sample.tax;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.kobe.dubbok.server.RpcServiceProvider;

@RpcServiceProvider(TaxService.class)
public class VATTaxService implements TaxService
{
	private static Logger LOGGER = LoggerFactory.getLogger(VATTaxService.class);
	
	public BigDecimal calculateTax(BigDecimal invoiceTotal) {
		BigDecimal result = invoiceTotal.multiply(new BigDecimal(0.06));
		return result.setScale(2, RoundingMode.HALF_UP);
	}
    
	public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("dubbok-provider.xml");
        LOGGER.debug("dubbok server started");
    }
}
