package com.example.billing;

import org.springframework.stereotype.Service;

@Service
public class BeanDependencyService {
    private final BillingComponent billingComponent;

    public BeanDependencyService(BillingComponent billingComponent) {
        this.billingComponent = billingComponent;
    }
}
