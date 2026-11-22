# SmartStay Hotel Booking System

CI6115 Programming III: Patterns and Algorithms, Coursework 1.
A console-based hotel booking system written in plain Java (no frameworks).

## Requirements

- JDK 21
- Maven 3.9 or later

## Build, test and run

```bash
mvn clean package          # compiles, runs the tests, builds target/smartstay.jar
mvn test                   # tests only
java -jar target/smartstay.jar
```

The admin menu's demo password is `admin123`.

## Manual testing at a chosen time

Some rules depend on the date and time. To fix the clock for a run, pass a date and time:

```bash
java -Dsmartstay.now=2026-11-06T15:00 -jar target/smartstay.jar
```

Seed reservations are dated relative to this time. Without the setting, the app uses the real time.

## Project structure

```
smartstay/
├─ pom.xml
└─ src/
   ├─ main/java/com/smartstay/
   │  ├─ SmartStayApp.java   entry point and composition root
   │  ├─ model/              Person, Guest, Admin, Room, RoomType, Reservation, DateRange, Invoice
   │  ├─ pricing/            DiscountPolicy (Strategy), BillingCalculator
   │  ├─ repository/         Repository<T, ID> and in-memory implementations
   │  ├─ service/            RoomService, GuestService, BookingService, InvoiceService
   │  ├─ validation/         GuestValidator, BookingRules
   │  ├─ algorithm/          MergeSort, BinarySearch
   │  ├─ exception/          ValidationException, BookingException, NotFoundException
   │  ├─ ui/                 menus, input reading, table output
   │  └─ util/               ID generation, formatting helpers
   └─ test/java/com/smartstay/   JUnit 5 tests mirroring the main packages
```

## Submission zip

Include `pom.xml`, `README.md` and `src/`. Exclude `target/` and IDE folders (`.idea/`, `.vscode/`, `out/`).
