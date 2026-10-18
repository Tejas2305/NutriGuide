# NutriGuide – Personalized Nutrition and Diet Management System

A comprehensive, textbook-grade B.Tech Computer Science Java 8 + DBMS local PC desktop application.

---

## 1. Project Title & Overview
**NutriGuide** is a personal nutrition and diet tracking desktop application built for educational evaluation and demonstration. It features a complete two-tier local PC architecture:
1. **Java Swing Desktop Client**: A clean, Windows desktop interface utilizing modern styling, clear contrast, and non-blocking asynchronous operations (`SwingWorker`) running on the Event Dispatch Thread (EDT).
2. **Localhost Backend Service**: An independent Java backend running on `http://127.0.0.1:8081` (powered by Java's standard `com.sun.net.httpserver.HttpServer`), which owns all authentication, PBKDF2 password security, business rules, stored procedures, validations, and MySQL JDBC persistence.

> [!NOTE]
> **Educational Demonstration Notice**: NutriGuide is built for educational demonstration and personal wellness tracking. It is **not** a medical device, clinical diagnostic tool, or therapeutic system. Nutritional recommendations are rule-based educational guidelines and must not be interpreted as professional medical advice.

---

## 2. System Architecture

NutriGuide enforces strict separation of concerns across a two-process localhost client-server architecture:

```
+-------------------------------------------------------------------------+
|                  NutriGuide Swing Desktop Client                        |
|                                                                         |
|  - LoginFrame, RegisterFrame, DashboardFrame, MealLogFrame,             |
|    NutritionGoalFrame, RecommendationFrame, ReportFrame, ProfileFrame   |
|  - UITheme (unified typography, responsive layout, clear error states)  |
|  - NutriGuideApiClient (HTTP/JSON communication via SwingWorker)        |
|  - Zero JDBC imports, Zero SQL statements, Zero direct MySQL access    |
+------------------------------------+------------------------------------+
                                     | HTTP REST (JSON)
                                     | 127.0.0.1:8081 (Localhost only)
                                     | Bearer Session Tokens
                                     v
+-------------------------------------------------------------------------+
|                  NutriGuide Localhost Backend Server                    |
|                                                                         |
|  - NutriGuideServer (com.sun.net.httpserver.HttpServer)                 |
|  - SessionManager (Thread-safe session token store, sliding expiry)     |
|  - REST Handlers (/api/auth, /api/profile, /api/meals, /api/goals, etc.)|
|  - Request Validation & Payload Limiter (1 MB threshold)                |
|  - PasswordUtil: Salted PBKDF2-HMAC-SHA256 & transparent SHA-256 legacy  |
|    on-login migration                                                   |
|  - Business Services (AuthenticationService, NutritionService, etc.)   |
|  - Data Access Objects (UserDAO, MealDAO, NutritionGoalDAO, etc.)       |
+------------------------------------+------------------------------------+
                                     | JDBC Connection (DriverManager)
                                     | Parameterized PreparedStatements
                                     v
+-------------------------------------------------------------------------+
|                         MySQL 8.x Database                              |
|                                                                         |
|  - 10 Relational Tables in 3NF Normal Form                              |
|  - 2 Views (daily_nutrition_summary, food_category_details)             |
|  - 2 Stored Functions (calculate_bmi, get_total_calories)                |
|  - 1 Stored Procedure (get_daily_summary with OUT parameters)           |
|  - 2 Triggers (trg_after_meal_insert, trg_validate_meal_quantity)       |
|  - ACID Engine (InnoDB) with Foreign Key Cascades & Constraints         |
+-------------------------------------------------------------------------+
```

---

## 3. Key Features

1. **User Authentication & Session Management**:
   - Salted `PBKDF2WithHmacSHA256` password hashing (600,000 iterations OWASP benchmarked, 16-byte random salt).
   - Seamless on-login migration path for legacy unsalted SHA-256 accounts without user lockout.
   - Authenticated session tokens with automatic timeout; credentials never passed in URLs.
   - Comprehensive server-side and client-side input validation.
2. **Interactive Main Dashboard**:
   - Welcome banner and current user context.
   - Dynamic BMI calculation powered by MySQL stored function `calculate_bmi`.
   - Real-time display of today's actual caloric and macro intake (Protein, Carbs, Fat).
   - Active nutritional target goal comparison.
   - Live educational dietary guideline preview panel.
   - Navigation to all submodules with clean window management.
3. **Meal Logging & Nutrition Tracking**:
   - Food catalog retrieval from MySQL with nutrient scaling.
   - Dynamic meal assembly table prior to transactional commit.
   - ACID-compliant multi-item meal insertion with automatic rollback on failure.
   - Meal history browsing with safe cascading deletion.
4. **Nutrition Goal Management**:
   - Creation and activation of daily calorie and macro targets.
   - Atomic deactivation of older goals upon activating a new goal.
   - Goal audit history table.
5. **Rule-Based Recommendation Engine**:
   - Deterministic rule evaluation based on BMI category and calorie/protein intake.
   - Prominently labeled educational nutritional advice.
6. **Reports & Nutrition History**:
   - Stored procedure (`get_daily_summary`) and view (`daily_nutrition_summary`) integration.
   - Date range intake summaries and plaintext report generation.
7. **User Profile & Security Management**:
   - Profile updating (Height, Weight, Activity Level) with automatic BMI recalculation.
   - Secure password changing requiring current password verification and confirmation matching.
   - Strict user record ownership enforcement across all endpoints.

---

## 4. Localhost REST API Endpoints

The backend binds exclusively to `127.0.0.1:8081`. All authenticated endpoints require the `Authorization: Bearer <token>` header.

| Method | Endpoint | Description | Auth Required |
|:---|:---|:---|:---|
| `GET` | `/api/health` | Health check and live database connectivity status | No |
| `POST` | `/api/auth/register` | Register new user profile with salted PBKDF2 hash | No |
| `POST` | `/api/auth/login` | Authenticate user; transparently migrates legacy hashes | No |
| `POST` | `/api/auth/logout` | Invalidate current session token | Yes |
| `GET` | `/api/profile` | Retrieve authenticated user profile | Yes |
| `PUT` | `/api/profile` | Update user demographics (height, weight, activity) | Yes |
| `POST` | `/api/profile/password` | Update account password after verification | Yes |
| `GET` | `/api/profile/bmi` | Compute BMI via MySQL stored function | Yes |
| `GET` | `/api/foods` | Fetch available food catalog with nutrients | Yes |
| `GET` | `/api/meals?date=YYYY-MM-DD` | Retrieve authenticated user's meals for a date | Yes |
| `POST` | `/api/meals` | Log a meal with items atomically (ACID transaction) | Yes |
| `DELETE` | `/api/meals/{id}` | Delete a meal and its items (ownership enforced) | Yes |
| `GET` | `/api/goals/active` | Get current active nutrition goal | Yes |
| `GET` | `/api/goals/history` | Get goal history audit log | Yes |
| `POST` | `/api/goals` | Create a new active nutrition goal | Yes |
| `POST` | `/api/goals/{id}/deactivate` | Deactivate an active goal | Yes |
| `GET` | `/api/recommendations` | Get user recommendations | Yes |
| `POST` | `/api/recommendations/generate` | Generate rule-based educational guidelines | Yes |
| `GET` | `/api/recommendations/history` | Get historical persisted recommendations | Yes |
| `GET` | `/api/reports/daily?date=YYYY-MM-DD` | Get daily summary via `daily_nutrition_summary` view (`getDailySummaryViaView`) | Yes |
| `GET` | `/api/reports/range?from=...&to=...` | Get multi-day nutrition summaries | Yes |
| `GET` | `/api/reports/text?date=YYYY-MM-DD` | Generate formatted plaintext nutrition report | Yes |

---

## 5. Prerequisites & Database Setup

### Prerequisites
- **Java**: Java 8 SE (JDK 1.8.x) — required runtime and compiler target
- **Database**: MySQL 8.x Server running locally on default port `3306`
- **Build Tool**: Apache Maven (or bundled `mvnw.cmd` wrapper)

### Ensure MySQL Server Is Running
Before starting the backend, verify that your local MySQL service is active:
```powershell
net start MySQL80
```
*(Or verify via the Windows Services management console `services.msc` or MySQL Workbench).*

### Step 1: Create Database and Apply Schema in Order
Open your MySQL client or MySQL Workbench and create the designated database:
```sql
CREATE DATABASE IF NOT EXISTS nutriguide;
USE nutriguide;
```

Execute the scripts located in `src/main/resources/sql/` in this **exact dependency order**:
1. `tables.sql` — Schema definition for all 10 normalized 3NF tables, primary keys, foreign keys with CASCADE/RESTRICT rules, and CHECK constraints.
2. `views.sql` — Creates `daily_nutrition_summary` and `food_category_details` views.
3. `functions.sql` — Creates stored functions `calculate_bmi` and `get_total_calories`.
4. `procedures.sql` — Creates stored procedure `get_daily_summary` with IN and OUT parameters.
5. `triggers.sql` — Creates audit trigger `trg_after_meal_insert` and validation trigger `trg_validate_meal_quantity`.
6. `sample_data.sql` — Seeds 5 food categories, 15 reference foods, and initial baseline demo accounts.

**Execution Methods**:
- *Via Windows Command Prompt (cmd.exe)*:
  *(Note: Input redirection `<` is supported in `cmd.exe`. PowerShell does not support `<`; PowerShell users should use MySQL Workbench or run these commands through `cmd.exe`).*
  ```cmd
  mysql -u root -p nutriguide < src\main\resources\sql\tables.sql
  mysql -u root -p nutriguide < src\main\resources\sql\views.sql
  mysql -u root -p nutriguide < src\main\resources\sql\functions.sql
  mysql -u root -p nutriguide < src\main\resources\sql\procedures.sql
  mysql -u root -p nutriguide < src\main\resources\sql\triggers.sql
  mysql -u root -p nutriguide < src\main\resources\sql\sample_data.sql
  ```
- *Or inside the MySQL Interactive Console*:
  ```sql
  USE nutriguide;
  source src/main/resources/sql/tables.sql;
  source src/main/resources/sql/views.sql;
  source src/main/resources/sql/functions.sql;
  source src/main/resources/sql/procedures.sql;
  source src/main/resources/sql/triggers.sql;
  source src/main/resources/sql/sample_data.sql;
  ```

*Optional Lab & Viva Demonstration Scripts*:
- `queries.sql` — Formatted reference queries covering DBMS Experiments 3, 4, and 5 (string/numeric/date functions, GROUP BY, HAVING, joins, subqueries).
- `transactions_demo.sql` — ACID transaction demonstration and concurrency script for DBMS Experiment 10.

### Step 2: Configure Database Credentials
Create `src/main/resources/database.properties` by copying the provided example template:
```powershell
copy src\main\resources\database.properties.example src\main\resources\database.properties
```
Edit `src/main/resources/database.properties` to set your local MySQL password:
```properties
db.url=jdbc:mysql://localhost:3306/nutriguide?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YOUR_MYSQL_PASSWORD
db.driver=com.mysql.cj.jdbc.Driver
```
*(Note: `database.properties` is strictly ignored by Git to protect local credentials).*

---

## 6. How to Build, Run & Stop

### Step 1: Compile the Project (Java 8)
```powershell
.\mvnw.cmd clean compile
```

### Step 2: Start the Localhost Backend Service
Run the backend startup script in a dedicated command window:
```powershell
.\run-backend.bat
```
*Or using Maven directly:*
```powershell
.\mvnw.cmd exec:java -Dexec.mainClass="com.nutriguide.server.NutriGuideServer"
```
The server will start and bind strictly to `http://127.0.0.1:8081`.

### Step 3: Launch the Java Swing Desktop Client
In a second command window, launch the desktop user interface:
```powershell
.\run-client.bat
```
*Or using Maven directly:*
```powershell
.\mvnw.cmd exec:java -Dexec.mainClass="com.nutriguide.gui.NutriGuideGUI"
```

### Step 4: How to Stop Services
- **Desktop Client**: Close the window or click **Logout** / **Exit**.
- **Backend Server**: In the backend console window, press `Ctrl+C` to terminate the process gracefully and release port `8081`.
- **MySQL Service**: If you wish to stop the MySQL daemon after evaluation, run `net stop MySQL80` from an administrative prompt or use the Services console.

### Baseline Demo Accounts
- **Account 1**: `user@example.com` / `Password@123` (John Doe)
- **Account 2**: `jane@example.com` / `Password@123` (Jane Smith)

---

## 7. Known Limitations & Operational Boundaries

1. **Java 8 Target Requirement**: Built specifically for Java 8 SE (JDK 1.8.x). Employs classic textbook OOP patterns, explicit loops, POJOs, `SwingWorker`, and anonymous inner classes; modern language constructs (lambdas, streams, Optional) and external frameworks (Spring, Hibernate) are intentionally omitted for academic alignment.
2. **Localhost Single-Host Loopback**: The backend service binds exclusively to loopback address `127.0.0.1:8081`. It is designed for single-machine academic demonstration and does not accept remote network traffic.
3. **Desktop Swing Presentation**: The user interface is a native Java Swing application designed for standard desktop screen resolutions.
4. **Single Active Goal Policy**: The database and service layers enforce that each user account maintains exactly one active nutrition goal (`is_active = TRUE`) at any given time; activating a new goal automatically deactivates prior goals.
5. **Educational Scope**: NutriGuide is engineered for educational demonstration and personal diet tracking. It is not intended as a certified clinical diagnostic or medical device.

---

## 8. Verification Test Suites & Database Safety

NutriGuide includes 15 academic test and demonstration suites.

### Database Safety Guard Policy
To prevent test runs from polluting or altering your development database:
- **Offline / Pure In-Memory Suites** (Safe to run anytime):
  - `ModelLayerDemo`: Tests 10 POJO models, encapsulation, equals/hashCode, constructors (0 database access).
  - `UtilityLayerDemo`: Tests PBKDF2 salting, legacy SHA-256 verification, ValidationUtil, and custom exceptions (0 database access).
- **Read-Only Suites** (Safe on configured database):
  - `GuiPhase4BDemo`: Tests Dashboard UI binding against user 1 (read-only inspection).
  - `DaoLayerDemo`: Sections A-F perform read queries; Section G (writes) is automatically skipped unless a disposable database is configured.
  - `RecommendationEngineDemo`: Sections 1-2 test rule polymorphism in-memory; Section 3 (writes) is automatically skipped unless a disposable database is configured.
- **Database-Writing Integration Suites** (Fail-Closed Safety Guard):
  - `BackendIntegrationDemo`, `ServiceLayerDemo`, `TransactionDemo`, `FinalIntegrationDemo`, `GuiPhase4ADemo`, `GuiPhase4CDemo`, `GuiPhase4DDemo`, `GuiPhase4EDemo`, `GuiPhase4FDemo`, `GuiPhase4GDemo`.
  - These suites require an explicitly configured disposable test database whose name exactly matches the configured JDBC catalog (set via JVM property `-Dnutriguide.test.database.name=<exact_db_name>`). Flag-only bypasses and substring matching are disabled; if run against an unverified or production database, they refuse execution immediately to protect your data.

### Running Safe Offline Suites:
```powershell
.\mvnw.cmd exec:java -Dexec.mainClass="com.nutriguide.demo.ModelLayerDemo"
.\mvnw.cmd exec:java -Dexec.mainClass="com.nutriguide.demo.UtilityLayerDemo"
```

---

## 9. DBMS & Java Lab Concepts Demonstrated

### DBMS Curriculum Concepts
- **Exp 1**: ER/EER Modeling, cardinalities, M:N resolution via `meal_item` junction table.
- **Exp 2**: DDL (CREATE, constraints) and DML (INSERT, UPDATE, DELETE).
- **Exp 3**: SQL Built-in Functions (`ROUND`, `DATE_FORMAT`, `SUM`, `AVG`, `COUNT`, `MAX`, `MIN`).
- **Exp 4**: GROUP BY, HAVING, ORDER BY, and SQL Views (`daily_nutrition_summary`, `food_category_details`).
- **Exp 5**: SQL Joins (INNER, LEFT, correlated subqueries with EXISTS).
- **Exp 6**: Stored Functions (`calculate_bmi`, `get_total_calories`).
- **Exp 7**: Stored Procedures with IN and OUT parameters (`get_daily_summary`).
- **Exp 8**: Database Triggers (`trg_after_meal_insert` for auditing, `trg_validate_meal_quantity` for SIGNAL validation).
- **Exp 9**: Functional Dependencies and 1NF, 2NF, 3NF Normalization.
- **Exp 10**: ACID Transactions, explicit COMMIT/ROLLBACK, and MySQL InnoDB REPEATABLE READ MVCC snapshot isolation (`TransactionDemo.java`).

### Java Lab Curriculum Concepts
- **Exp 1–2**: OOP Fundamentals, Variables, Encapsulation, Constructors (Default, Parameterized, Copy), Method Overriding (`toString`, `equals`, `hashCode`).
- **Exp 3**: Static Utilities (`ValidationUtil`, `PasswordUtil`).
- **Exp 4**: Type Conversion and Wrapper Classes (`Integer.parseInt`, `Double.parseDouble`, `BigDecimal`).
- **Exp 5**: Collections Framework (`ArrayList`, `Vector`).
- **Exp 6**: Anonymous Inner Classes for GUI event handling.
- **Exp 7 & 11**: Interfaces and Polymorphism (`RecommendationEngine` interface implemented by `RuleBasedRecommendationEngine`).
- **Exp 8**: `super` keyword in custom exception hierarchies.
- **Exp 9**: `final` keyword on immutable utility classes and constants.
- **Exp 12**: Package Architecture (`com.nutriguide.*`).
- **Exp 14–16**: Exception Handling, nested try-catch, resource management (try-with-resources), and custom exception classes.
- **Exp 17–18**: Java Swing components and Layout Managers (`BorderLayout`, `FlowLayout`, `GridBagLayout`, `GridLayout`).
- **Exp 19–24**: JDBC Database Integration, `PreparedStatement`, `CallableStatement`, `ResultSet` navigation.
- **Exp 25–26**: Multi-threading via Event Dispatch Thread (`SwingUtilities.invokeLater`), asynchronous background execution (`SwingWorker`), and concurrent thread pooling in `NutriGuideServer`.
