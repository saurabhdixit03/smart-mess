package com.smartmess.backend.config.seed;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeedDataService {
    
    private final MealPricingSeeder mealPricingSeeder;
    private final MessSettingsSeeder messSettingsSeeder;
    private final CustomerSeeder customerSeeder;
    private final MenuSeeder menuSeeder;
    private final MealResponseSeeder mealResponseSeeder;
    private final MealRecordSeeder mealRecordSeeder; 

    @PostConstruct
    public void seedDemoData() {

        mealPricingSeeder.seed();

        messSettingsSeeder.seed();

        customerSeeder.seed();

        menuSeeder.seed();

        mealResponseSeeder.seed();
        
        mealRecordSeeder.seedMealRecords();

        
    }
   
        
        

        /*
         * Future Modules
         *
         * seedCustomers();
         * seedMenus();
         * seedMealResponses();
         * seedMealRecords();
         * seedBills();
         * seedPayments();
         */

    

//    private void seedMealPricing() {
//
//        if (mealPricingRepository.count() > 0) {
//            return;
//        }
//
//        MealPricing mealPricing = new MealPricing();
//
//        mealPricing.setHalfMealPrice(BigDecimal.valueOf(60));
//
//        mealPricing.setFullMealPrice(BigDecimal.valueOf(80));
//
//        mealPricing.setExtraRotiPrice(BigDecimal.valueOf(10));
//
//        mealPricingRepository.save(mealPricing);
//
//        log.info("Demo Meal Pricing seeded successfully.");
//
//    }

//    private void seedMessSettings() {
//
//        if (messSettingsRepository.count() > 0) {
//            return;
//        }
//
//        MessSettings settings = new MessSettings();
//
//        settings.setReceiverName("Smart Mess");
//
//        settings.setUpiId("smartmess@upi");
//
//        messSettingsRepository.save(settings);
//
//        log.info("Demo Mess Settings seeded successfully.");
//
//    }
    
    //****************************************************************************************************//
    
//    private record CustomerSeed(
//
//            String fullName,
//
//            String mobileNumber,
//
//            String email,
//
//            String remarks,
//
//            LocalDate joiningDate,
//
//            CustomerStatus status
//
//    ) {
//    }
//    
//    private void seedCustomers() {
//
//        if (customerRepository.count() > 0) {
//            return;
//        }
//        
//        List<CustomerSeed> customers = List.of(
//
//                new CustomerSeed(
//                        "Aarav Sharma",
//                        "9876500001",
//                        "aarav.sharma@example.com",
//                        "Prefers full meal",
//                        LocalDate.of(2026, 1, 10),
//                        CustomerStatus.ACTIVE
//                ),
//
//                new CustomerSeed(
//                        "Priya Patil",
//                        "9876500002",
//                        "priya.patil@example.com",
//                        "Prefers half meal",
//                        LocalDate.of(2026, 2, 5),
//                        CustomerStatus.ACTIVE
//                ),
//
//                new CustomerSeed(
//                        "Rohan Kulkarni",
//                        "9876500003",
//                        "rohan.kulkarni@example.com",
//                        "Extra roti occasionally",
//                        LocalDate.of(2026, 2, 18),
//                        CustomerStatus.ACTIVE
//                ),
//
//                new CustomerSeed(
//                        "Sneha Joshi",
//                        "9876500004",
//                        "sneha.joshi@example.com",
//                        null,
//                        LocalDate.of(2026, 3, 12),
//                        CustomerStatus.ACTIVE
//                ),
//
//                new CustomerSeed(
//                        "Aditya Deshmukh",
//                        "9876500005",
//                        "aditya.deshmukh@example.com",
//                        "Night shift customer",
//                        LocalDate.of(2026, 4, 2),
//                        CustomerStatus.ACTIVE
//                ),
//
//                new CustomerSeed(
//                        "Neha Jadhav",
//                        "9876500006",
//                        "neha.jadhav@example.com",
//                        null,
//                        LocalDate.of(2026, 4, 25),
//                        CustomerStatus.ACTIVE
//                ),
//
//                new CustomerSeed(
//                        "Rahul Pawar",
//                        "9876500007",
//                        "rahul.pawar@example.com",
//                        "Vegetarian",
//                        LocalDate.of(2026, 5, 15),
//                        CustomerStatus.ACTIVE
//                ),
//
//                new CustomerSeed(
//                        "Anjali Shinde",
//                        "9876500008",
//                        "anjali.shinde@example.com",
//                        null,
//                        LocalDate.of(2026, 6, 8),
//                        CustomerStatus.ACTIVE
//                ),
//
//                new CustomerSeed(
//                        "Vikas More",
//                        "9876500009",
//                        "vikas.more@example.com",
//                        null,
//                        LocalDate.of(2026, 1, 22),
//                        CustomerStatus.INACTIVE
//                ),
//
//                new CustomerSeed(
//                        "Pooja Kale",
//                        "9876500010",
//                        "pooja.kale@example.com",
//                        null,
//                        LocalDate.of(2026, 3, 30),
//                        CustomerStatus.INACTIVE
//                )
//
//        );
//        
//        for (CustomerSeed seed : customers) {
//
//            Customer customer = new Customer();
//
//            customer.setFullName(seed.fullName());
//
//            customer.setMobileNumber(seed.mobileNumber());
//
//            customer.setEmail(seed.email());
//
//            customer.setRemarks(seed.remarks());
//
//            customer.setJoiningDate(seed.joiningDate());
//
//            customer.setStatus(seed.status());
//
//            customerRepository.save(customer);
//
//        }
//
//        log.info("Demo Customers seeded successfully.");
//
//    }

    //*******************************************************************************************************//
    
//    private record MenuSeed(
//
//            LocalDate menuDate,
//
//            MealSession mealSession,
//
//            String sabjiOne,
//
//            String sabjiTwo,
//
//            String dal,
//
//            String rice,
//
//            String sweet
//
//    ) {
//    }
//    
//    private void seedMenus() {
//
//        if (menuRepository.count() > 0) {
//        	return;
//        }
//        	
//        List<MenuSeed> menus = List.of(
//
//        	        new MenuSeed(
//        	                LocalDate.now(),
//        	                MealSession.LUNCH,
//        	                "Aloo Matar",
//        	                "Bhindi Fry",
//        	                "Dal Tadka",
//        	                "Jeera Rice",
//        	                "Gulab Jamun"
//        	        ),
//
//        	        new MenuSeed(
//        	                LocalDate.now(),
//        	                MealSession.DINNER,
//        	                "Mix Veg",
//        	                "Paneer Bhurji",
//        	                "Dal Fry",
//        	                "Steamed Rice",
//        	                "Kheer"
//        	        )
//
//        	);
//    
//        	
//        	for (MenuSeed seed : menus) {
//
//        	    Menu menu = new Menu();
//
//        	    menu.setMenuDate(seed.menuDate());
//
//        	    menu.setMealSession(seed.mealSession());
//
//        	    menu.setSabjiOne(seed.sabjiOne());
//
//        	    menu.setSabjiTwo(seed.sabjiTwo());
//
//        	    menu.setDal(seed.dal());
//
//        	    menu.setRice(seed.rice());
//
//        	    menu.setSweet(seed.sweet());
//
//        	    menuRepository.save(menu);
//
//        	}
//
//        	log.info("Demo Menus seeded successfully.");
//        	
//          }
    
    //******************************************************************************************//
    
//    private record MealResponseSeed(
//
//            int customerIndex,
//
//            MealSession mealSession,
//
//            MealResponseStatus responseStatus,
//
//            MealOption mealOption,
//
//            int extraRotiCount
//
//    ) {
//    }
//
//    private void seedMealResponses() {
//
//        if (mealResponseRepository.count() > 0) {
//            return;
//        }
//
//        List<Customer> customers =
//                customerRepository.findByStatus(CustomerStatus.ACTIVE);
//
//        if (customers.isEmpty()) {
//
//            log.warn("Skipping MealResponse seeding because no active customers are available.");
//
//            return;
//        }
//
//        Menu todayLunch = menuRepository
//                .findByMenuDateAndMealSession(
//                        LocalDate.now(),
//                        MealSession.LUNCH
//                )
//                .orElseThrow(() -> new IllegalStateException(
//                        "Today's Lunch menu not found."
//                ));
//
//        Menu todayDinner = menuRepository
//                .findByMenuDateAndMealSession(
//                        LocalDate.now(),
//                        MealSession.DINNER
//                )
//                .orElseThrow(() -> new IllegalStateException(
//                        "Today's Dinner menu not found."
//                ));
//
//        List<MealResponseSeed> responseSeeds = List.of(
//
//                // ---------------- TODAY LUNCH ----------------
//
//                new MealResponseSeed(0, MealSession.LUNCH, MealResponseStatus.ACCEPTED, MealOption.FULL, 1),
//                new MealResponseSeed(1, MealSession.LUNCH, MealResponseStatus.ACCEPTED, MealOption.HALF, 0),
//                new MealResponseSeed(2, MealSession.LUNCH, MealResponseStatus.DECLINED, null, 0),
//                new MealResponseSeed(3, MealSession.LUNCH, MealResponseStatus.ACCEPTED, MealOption.FULL, 2),
//                new MealResponseSeed(4, MealSession.LUNCH, MealResponseStatus.ACCEPTED, MealOption.HALF, 0),
//                new MealResponseSeed(5, MealSession.LUNCH, MealResponseStatus.DECLINED, null, 0),
//                new MealResponseSeed(6, MealSession.LUNCH, MealResponseStatus.ACCEPTED, MealOption.FULL, 1),
//                new MealResponseSeed(7, MealSession.LUNCH, MealResponseStatus.ACCEPTED, MealOption.FULL, 0),
//
//                // ---------------- TODAY DINNER ----------------
//
//                new MealResponseSeed(0, MealSession.DINNER, MealResponseStatus.ACCEPTED, MealOption.FULL, 0),
//                new MealResponseSeed(1, MealSession.DINNER, MealResponseStatus.DECLINED, null, 0),
//                new MealResponseSeed(2, MealSession.DINNER, MealResponseStatus.ACCEPTED, MealOption.HALF, 0),
//                new MealResponseSeed(3, MealSession.DINNER, MealResponseStatus.ACCEPTED, MealOption.FULL, 1),
//                new MealResponseSeed(4, MealSession.DINNER, MealResponseStatus.DECLINED, null, 0),
//                new MealResponseSeed(5, MealSession.DINNER, MealResponseStatus.ACCEPTED, MealOption.HALF, 0),
//                new MealResponseSeed(6, MealSession.DINNER, MealResponseStatus.ACCEPTED, MealOption.FULL, 2),
//                new MealResponseSeed(7, MealSession.DINNER, MealResponseStatus.DECLINED, null, 0)
//
//        );
//        
//        for (MealResponseSeed seed : responseSeeds) {
//
//            Customer customer = customers.get(seed.customerIndex());
//
//            Menu menu = (seed.mealSession() == MealSession.LUNCH)
//                    ? todayLunch
//                    : todayDinner;
//
//            MealResponse response = new MealResponse();
//
//            response.setCustomer(customer);
//            response.setMenu(menu);
//            response.setResponseStatus(seed.responseStatus());
//            response.setMealOption(seed.mealOption());
//            response.setExtraRotiCount(seed.extraRotiCount());
//            response.setRespondedAt(LocalDateTime.now());
//
//            mealResponseRepository.save(response);
//        }
//
//        log.info("Demo Meal Responses seeded successfully.");
//    }
    
    //***************************************************************************************************//
    
//    private record MealRecordSeed(
//
//            MealResponse mealResponse,
//
//            MealOption mealOption,
//
//            int extraRotiCount
//
//    ) {
//    }
//    
//    private void seedMealRecords() {
//
//        if (mealRecordRepository.count() > 0) {
//            return;
//        }
//
//        List<MealResponse> acceptedResponses =
//                mealResponseRepository.findAll()
//                        .stream()
//                        .filter(response ->
//                                response.getResponseStatus() == MealResponseStatus.ACCEPTED)
//                        .toList();
//
//        if (acceptedResponses.isEmpty()) {
//
//            log.warn("Skipping MealRecord seeding because no accepted meal responses exist.");
//
//            return;
//        }
//        
//        List<MealRecordSeed> mealRecordSeeds = acceptedResponses.stream()
//
//                .map(response -> new MealRecordSeed(
//
//                        response,
//
//                        response.getMealOption(),
//
//                        response.getExtraRotiCount()
//
//                ))
//
//                .toList();
//        
//        MealPricing mealPricing = mealPricingRepository
//                .findTopByOrderByUpdatedAtDesc()
//                .orElseThrow(() -> new IllegalStateException(
//                        "Meal pricing not found."));
//        
//        for (MealRecordSeed seed : mealRecordSeeds) {
//
//            BigDecimal mealPrice;
//
//            if (seed.mealOption() == MealOption.FULL) {
//
//                mealPrice = mealPricing.getFullMealPrice();
//
//            } else {
//
//                mealPrice = mealPricing.getHalfMealPrice();
//            }
//
//            BigDecimal totalAmount =
//                    mealPrice.add(
//
//                            mealPricing.getExtraRotiPrice()
//                                    .multiply(BigDecimal.valueOf(seed.extraRotiCount()))
//                    );
//
//            MealRecord mealRecord = MealRecord.builder()
//
//                    .customer(seed.mealResponse().getCustomer())
//
//                    .menu(seed.mealResponse().getMenu())
//
//                    .mealResponse(seed.mealResponse())
//
//                    .mealOption(seed.mealOption())
//
//                    .mealPrice(mealPrice)
//
//                    .extraRotiCount(seed.extraRotiCount())
//
//                    .extraRotiPrice(mealPricing.getExtraRotiPrice())
//
//                    .totalAmount(totalAmount)
//
//                    .collectedAt(LocalDateTime.now())
//
//                    .build();
//
//            mealRecordRepository.save(mealRecord);
//        }

    }
