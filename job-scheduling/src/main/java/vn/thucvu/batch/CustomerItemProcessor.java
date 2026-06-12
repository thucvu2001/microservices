package vn.thucvu.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class CustomerItemProcessor implements ItemProcessor<Customer, Customer> {

    private static final Logger log = LoggerFactory.getLogger(CustomerItemProcessor.class);

    @Override
    public Customer process(final Customer customer) {
        final String firstName = customer.getFirstName().toUpperCase();
        final String lastName = customer.getFirstName().toUpperCase();

        final Customer transformedCustomer = new Customer(firstName, lastName);

        log.info("Converting ({}) into ({})", customer, transformedCustomer);

        return transformedCustomer;
    }

}
