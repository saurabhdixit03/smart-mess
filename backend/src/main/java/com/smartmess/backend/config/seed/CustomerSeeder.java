package com.smartmess.backend.config.seed;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.repository.CustomerRepository;

@Component
public class CustomerSeeder {

    private static final Logger log =
            LoggerFactory.getLogger(CustomerSeeder.class);

    private final CustomerRepository customerRepository;

    public CustomerSeeder(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    private record CustomerSeed(

            String fullName,

            String mobileNumber,

            String email,

            String remarks,

            LocalDate joiningDate,

            CustomerStatus status

    ) {
    }

    public void seed() {

        if (customerRepository.count() > 0) {
            return;
        }

        for (CustomerSeed seed : buildDemoCustomers()) {

            Customer customer = new Customer();

            customer.setFullName(seed.fullName());

            customer.setMobileNumber(seed.mobileNumber());

            customer.setEmail(seed.email());

            customer.setRemarks(seed.remarks());

            customer.setJoiningDate(seed.joiningDate());

            customer.setStatus(seed.status());

            customerRepository.save(customer);
        }

        log.info("Demo Customers seeded successfully.");
    }

    private List<CustomerSeed> buildDemoCustomers() {

        return List.of(

                new CustomerSeed(
                        "Aarav Sharma",
                        "9876500001",
                        "aarav.sharma@example.com",
                        "Prefers full meal",
                        LocalDate.of(2026, 1, 10),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Priya Patil",
                        "9876500002",
                        "priya.patil@example.com",
                        "Prefers half meal",
                        LocalDate.of(2026, 2, 5),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Rohan Kulkarni",
                        "9876500003",
                        "rohan.kulkarni@example.com",
                        "Extra roti occasionally",
                        LocalDate.of(2026, 2, 18),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Sneha Joshi",
                        "9876500004",
                        "sneha.joshi@example.com",
                        null,
                        LocalDate.of(2026, 3, 12),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Aditya Deshmukh",
                        "9876500005",
                        "aditya.deshmukh@example.com",
                        "Night shift customer",
                        LocalDate.of(2026, 4, 2),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Neha Jadhav",
                        "9876500006",
                        "neha.jadhav@example.com",
                        null,
                        LocalDate.of(2026, 4, 25),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Rahul Pawar",
                        "9876500007",
                        "rahul.pawar@example.com",
                        "Vegetarian",
                        LocalDate.of(2026, 5, 15),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Anjali Shinde",
                        "9876500008",
                        "anjali.shinde@example.com",
                        null,
                        LocalDate.of(2026, 6, 8),
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Vikas More",
                        "9876500009",
                        "vikas.more@example.com",
                        null,
                        LocalDate.of(2026, 1, 22),
                        CustomerStatus.INACTIVE
                ),

                new CustomerSeed(
                        "Pooja Kale",
                        "9876500010",
                        "pooja.kale@example.com",
                        null,
                        LocalDate.of(2026, 3, 30),
                        CustomerStatus.INACTIVE
                )

        );
    }
}