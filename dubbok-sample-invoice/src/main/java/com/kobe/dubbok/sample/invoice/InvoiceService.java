package com.kobe.dubbok.sample.invoice;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.kobe.dubbok.client.DynamicProxy;
import com.kobe.dubbok.sample.tax.TaxService;

public class InvoiceService {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("dubbok-consumer.xml");
        final DynamicProxy rpcProxy = context.getBean(DynamicProxy.class);

        int threadNum = 10;
        int loopCount = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threadNum);
        final CountDownLatch latch = new CountDownLatch(loopCount);

        try {
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < loopCount; i++) {
                executor.submit(new Runnable() {
                    
                	public void run() {
                        TaxService taxService = rpcProxy.create(TaxService.class);
                        BigDecimal invoiceTotal = new BigDecimal(100);
                        BigDecimal taxTotal = taxService.calculateTax(invoiceTotal);
                        System.out.println("Tax is calculated: " + taxTotal);
                        latch.countDown();
                    }
                });
            }
            latch.await();

            long timeDuration = System.currentTimeMillis() - startTime;
            System.out.println("thread count: " + threadNum);
            System.out.println("loop count: " + loopCount);
            System.out.println("time duration: " + timeDuration + "ms");
            System.out.println("tps: " + (double) loopCount / ((double) timeDuration / 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        System.exit(0);
    }
}
