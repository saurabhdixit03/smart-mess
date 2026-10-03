package com.smartmess.backend.config.seed;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.repository.CustomerRepository;

@Component
public class CustomerSeeder {

    private static final Logger log =
            LoggerFactory.getLogger(
                    CustomerSeeder.class
            );

    /*
     * Shared password for demo customers only.
     *
     * The password is always stored as a BCrypt hash.
     */
    private static final String DEMO_PASSWORD =
            "Password@123";

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public CustomerSeeder(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            Clock clock) {

        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    private record CustomerSeed(
            String fullName,
            String mobileNumber,
            String email,
            String remarks,
            long joinedDaysAgo,
            CustomerStatus status
    ) {
    }

    /*
     * Seeds sample customers within the dedicated demo mess.
     *
     * SeedDataService selects and passes the demo mess.
     * Other messes' customers do not affect this seed check.
     */
    @Transactional
    public void seed(Mess mess) {

        if (mess == null || mess.getMessId() == null) {

            throw new IllegalArgumentException(
                    "A persisted demo mess is required."
            );
        }

        if (!customerRepository.findAllByMess_MessId(
                mess.getMessId()
        ).isEmpty()) {

            log.info(
                    "Customers already exist for demo mess {}. Skipping demo seeding.",
                    mess.getMessId()
            );

            return;
        }

        /*
         * Email and mobile uniqueness remain global.
         *
         * Do not reuse another mess's customer account
         * when a reserved sample identity is already present.
         */
        List<CustomerSeed> demoCustomers =
                buildDemoCustomers();

        for (CustomerSeed seed : demoCustomers) {

            if (customerRepository.existsByEmail(seed.email())
                    || customerRepository.existsByMobileNumber(
                            seed.mobileNumber()
                    )) {

                throw new IllegalStateException(
                        "A reserved demo customer email or mobile number is already in use."
                );
            }
        }

        String encodedPassword =
                passwordEncoder.encode(
                        DEMO_PASSWORD
                );

        LocalDate today =
                LocalDate.now(clock);

        for (CustomerSeed seed : demoCustomers) {

            Customer customer =
                    new Customer();

            customer.setMess(mess);

            customer.setFullName(
                    seed.fullName()
            );

            customer.setMobileNumber(
                    seed.mobileNumber()
            );

            customer.setEmail(
                    seed.email()
            );

            customer.setPassword(
                    encodedPassword
            );

            customer.setRemarks(
                    seed.remarks()
            );

            customer.setJoiningDate(
                    today.minusDays(
                            seed.joinedDaysAgo()
                    )
            );

            customer.setStatus(
                    seed.status()
            );

            customerRepository.save(
                    customer
            );
        }

        log.info(
                "Demo Customers seeded successfully for mess {}. "
                        + "Active: 8, Inactive: 2.",
                mess.getMessId()
        );
    }

    private List<CustomerSeed> buildDemoCustomers() {

        return List.of(

                new CustomerSeed(
                        "Aarav Sharma",
                        "9876500001",
                        "aarav.sharma@example.com",
                        "Prefers full meal",
                        240,
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Priya Patil",
                        "9876500002",
                        "priya.patil@example.com",
                        "Prefers half meal",
                        220,
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Rohan Kulkarni",
                        "9876500003",
                        "rohan.kulkarni@example.com",
                        "Extra roti occasionally",
                        200,
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Sneha Joshi",
                        "9876500004",
                        "sneha.joshi@example.com",
                        null,
                        180,
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Aditya Deshmukh",
                        "9876500005",
                        "aditya.deshmukh@example.com",
                        "Night shift customer",
                        160,
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Neha Jadhav",
                        "9876500006",
                        "neha.jadhav@example.com",
                        null,
                        140,
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Rahul Pawar",
                        "9876500007",
                        "rahul.pawar@example.com",
                        "Vegetarian",
                        120,
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Anjali Shinde",
                        "9876500008",
                        "anjali.shinde@example.com",
                        null,
                        100,
                        CustomerStatus.ACTIVE
                ),

                new CustomerSeed(
                        "Vikas More",
                        "9876500009",
                        "vikas.more@example.com",
                        null,
                        210,
                        CustomerStatus.INACTIVE
                ),

                new CustomerSeed(
                        "Pooja Kale",
                        "9876500010",
                        "pooja.kale@example.com",
                        null,
                        150,
                        CustomerStatus.INACTIVE
                )
        );
    }
}