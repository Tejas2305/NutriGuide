"""
NutriGuide DBMS Report Diagram Generator
Generates high-resolution diagrams for the NutriGuide DBMS Project Report:
1. System Architecture Diagram (Figure 3.1)
2. ER Diagram (Figure 6.1)
3. EER Diagram (Figure 6.2)
4. Normalization Comparison Figures (10.1, 10.2, 10.3, 10.4, 10.5, 10.6)
"""

import os
from PIL import Image, ImageDraw, ImageFont

OUTPUT_DIR = r"D:\NutriGuide\report\images"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Font loading helper
WINDIR = os.environ.get("WINDIR", "C:/Windows")
FONT_DIR = os.path.join(WINDIR, "Fonts")

def get_font(name="segoeui.ttf", size=14):
    try:
        path = os.path.join(FONT_DIR, name)
        if os.path.exists(path):
            return ImageFont.truetype(path, size)
    except Exception:
        pass
    try:
        return ImageFont.truetype(os.path.join(FONT_DIR, "arial.ttf"), size)
    except Exception:
        return ImageFont.load_default()

FONT_TITLE = get_font("segoeuib.ttf", 26)
FONT_SUBTITLE = get_font("segoeui.ttf", 16)
FONT_SECTION = get_font("segoeuib.ttf", 18)
FONT_BOLD = get_font("segoeuib.ttf", 14)
FONT_REGULAR = get_font("segoeui.ttf", 13)
FONT_SMALL = get_font("segoeui.ttf", 11)
FONT_SMALL_BOLD = get_font("segoeuib.ttf", 11)
FONT_MONO = get_font("consola.ttf", 12)

# ==============================================================================
# 1. SYSTEM ARCHITECTURE DIAGRAM
# ==============================================================================
def draw_system_architecture():
    W, H = 2000, 1400
    im = Image.new("RGB", (W, H), color=(250, 252, 255))
    draw = ImageDraw.Draw(im)

    # Canvas border
    draw.rectangle([(20, 20), (W-20, H-20)], outline=(200, 215, 235), width=2)

    # Title Banner
    draw.rectangle([(20, 20), (W-20, 90)], fill=(30, 58, 110))
    draw.text((W//2, 55), "NutriGuide — System Architecture & Multi-Tier Topology", fill=(255, 255, 255), font=FONT_TITLE, anchor="mm")

    # Layer 1: Presentation Layer (Swing Desktop Client)
    l1_top = 120
    l1_h = 240
    draw.rounded_rectangle([(60, l1_top), (W-60, l1_top+l1_h)], radius=12, fill=(238, 244, 255), outline=(90, 140, 220), width=2)
    draw.text((80, l1_top+20), "LAYER 1: PRESENTATION LAYER (Java Swing Desktop GUI)", fill=(20, 50, 120), font=FONT_SECTION)
    draw.text((80, l1_top+45), "Native Windows Desktop Application running on Event Dispatch Thread (EDT) with asynchronous SwingWorker background execution", fill=(80, 100, 130), font=FONT_SMALL)

    # Frames inside Presentation Layer
    frames = [
        ("LoginFrame", "Auth & Password Hashing", (220, 235, 255)),
        ("RegisterFrame", "User Demographic Intake", (220, 235, 255)),
        ("DashboardFrame", "Real-Time Macros & BMI", (210, 240, 230)),
        ("MealLogFrame", "Multi-Item Transaction Entry", (255, 240, 225)),
        ("NutritionGoalFrame", "Calorie & Macro Target Allocation", (245, 230, 255)),
        ("RecommendationFrame", "Rule-Based Nutrition Guidance", (255, 235, 240)),
        ("ReportFrame", "Daily/Range Summaries & Plaintext", (240, 245, 225)),
        ("ProfileFrame", "Account Details & Password Change", (230, 240, 255))
    ]
    fw = 210
    fh = 100
    for i, (title, sub, col) in enumerate(frames):
        fx = 80 + (i % 4) * (fw + 30)
        fy = l1_top + 70 + (i // 4) * (fh + 15)
        draw.rounded_rectangle([(fx, fy), (fx+fw, fy+fh)], radius=8, fill=col, outline=(150, 180, 220), width=1)
        draw.text((fx+fw//2, fy+30), title, fill=(20, 40, 80), font=FONT_BOLD, anchor="mm")
        draw.text((fx+fw//2, fy+60), sub, fill=(70, 90, 120), font=FONT_SMALL, anchor="mm")

    # Bridge 1: HTTP REST Protocol Banner
    b1_top = l1_top + l1_h + 20
    b1_h = 70
    draw.rounded_rectangle([(250, b1_top), (W-250, b1_top+b1_h)], radius=10, fill=(245, 247, 250), outline=(120, 150, 190), width=2)
    draw.text((W//2, b1_top+25), "HTTP REST API over Loopback (http://127.0.0.1:8081)", fill=(180, 60, 20), font=FONT_BOLD, anchor="mm")
    draw.text((W//2, b1_top+48), "Stateful Session Tokens (Bearer Authorization Header) | JSON Serialization & Deserialization | Rate & Payload Limiting", fill=(90, 100, 115), font=FONT_SMALL, anchor="mm")

    # Down / Up Arrows
    draw.line([(W//2 - 100, l1_top+l1_h), (W//2 - 100, b1_top)], fill=(80, 130, 210), width=3)
    draw.polygon([(W//2 - 105, b1_top-2), (W//2 - 95, b1_top-2), (W//2 - 100, b1_top+8)], fill=(80, 130, 210))
    draw.line([(W//2 + 100, b1_top+b1_h), (W//2 + 100, b1_top+b1_h+20)], fill=(80, 130, 210), width=3)
    draw.polygon([(W//2 + 95, b1_top+b1_h+18), (W//2 + 105, b1_top+b1_h+18), (W//2 + 100, b1_top+b1_h+26)], fill=(80, 130, 210))

    # Layer 2: Application / Server Layer (NutriGuideServer)
    l2_top = b1_top + b1_h + 20
    l2_h = 320
    draw.rounded_rectangle([(60, l2_top), (W-60, l2_top+l2_h)], radius=12, fill=(255, 252, 245), outline=(220, 170, 90), width=2)
    draw.text((80, l2_top+20), "LAYER 2: APPLICATION & BUSINESS LOGIC LAYER (Java 8 Localhost HTTP Server)", fill=(140, 80, 10), font=FONT_SECTION)
    draw.text((80, l2_top+45), "HttpServer (com.sun.net.httpserver) | Thread-Safe SessionManager | Security & Password Hashing | Recommendation Engine", fill=(110, 90, 60), font=FONT_SMALL)

    # Submodules inside Layer 2
    # 2.1 Handlers
    box_y = l2_top + 70
    bh = 220
    bw1 = 400
    draw.rounded_rectangle([(80, box_y), (80+bw1, box_y+bh)], radius=8, fill=(255, 245, 230), outline=(230, 190, 130), width=1)
    draw.text((80+bw1//2, box_y+20), "HTTP REST Handlers", fill=(140, 70, 0), font=FONT_BOLD, anchor="mm")
    handlers = ["AuthHandler (/api/auth/*)", "ProfileHandler (/api/profile/*)", "MealHandler (/api/meals/*)", "GoalHandler (/api/goals/*)", "RecommendationHandler", "ReportHandler (/api/reports/*)", "FoodHandler (/api/foods)", "HealthHandler (/api/health)"]
    for idx, h_text in enumerate(handlers):
        draw.text((100, box_y+50 + idx*20), f"• {h_text}", fill=(80, 50, 20), font=FONT_SMALL)

    # 2.2 Business Services
    bw2 = 420
    bx2 = 80 + bw1 + 25
    draw.rounded_rectangle([(bx2, box_y), (bx2+bw2, box_y+bh)], radius=8, fill=(245, 250, 240), outline=(170, 210, 150), width=1)
    draw.text((bx2+bw2//2, box_y+20), "Business Services & Rules", fill=(40, 110, 30), font=FONT_BOLD, anchor="mm")
    services = ["AuthenticationService (PBKDF2/SHA-256)", "UserService (Demographics & BMI)", "MealService (ACID Transaction Manager)", "FoodService (Catalog & Macro Scaling)", "NutritionService (Daily Goal Tracking)", "RecommendationService (Rules Evaluation)", "ReportService (Summary View & Plaintext)", "MealPlanService (Prospective Diet Plans)"]
    for idx, s_text in enumerate(services):
        draw.text((bx2+20, box_y+50 + idx*20), f"• {s_text}", fill=(30, 70, 20), font=FONT_SMALL)

    # 2.3 DAOs and Security
    bw3 = 450
    bx3 = bx2 + bw2 + 25
    draw.rounded_rectangle([(bx3, box_y), (bx3+bw3, box_y+bh)], radius=8, fill=(240, 245, 255), outline=(160, 190, 230), width=1)
    draw.text((bx3+bw3//2, box_y+20), "Persistence Layer & Data Access Objects", fill=(30, 70, 140), font=FONT_BOLD, anchor="mm")
    daos = ["UserDAO & NutritionGoalDAO", "MealDAO & MealItemDAO (Atomic Multi-Item)", "FoodDAO & FoodCategoryDAO", "MealPlanDAO & MealPlanItemDAO", "RecommendationDAO & ActivityLogDAO", "DBConnectionFactory (Hikari/DriverManager)", "Security: PasswordUtil (600,000 iter PBKDF2)", "Validation: ValidationUtil (Defensive Guards)"]
    for idx, d_text in enumerate(daos):
        draw.text((bx3+20, box_y+50 + idx*20), f"• {d_text}", fill=(20, 50, 90), font=FONT_SMALL)

    # 2.4 Rule Engine Badge
    bw4 = 430
    bx4 = bx3 + bw3 + 25
    draw.rounded_rectangle([(bx4, box_y), (W-80, box_y+bh)], radius=8, fill=(255, 240, 245), outline=(230, 160, 180), width=1)
    draw.text((bx4+(W-80-bx4)//2, box_y+20), "Recommendation Engine", fill=(150, 30, 70), font=FONT_BOLD, anchor="mm")
    rules = ["RecommendationEngine (Interface)", "RuleBasedRecommendationEngine (Impl)", "Deterministic Heuristics Evaluator", "Deficit / Surplus Caloric Analysis", "Protein Intake Target Comparisons", "WHO / Dietary Health Advisory Rules", "Automatic Daily Recommendation Log", "ACID Persistence to MySQL"]
    for idx, r_text in enumerate(rules):
        draw.text((bx4+20, box_y+50 + idx*20), f"• {r_text}", fill=(100, 20, 50), font=FONT_SMALL)

    # Bridge 2: JDBC Protocol Banner
    b2_top = l2_top + l2_h + 20
    b2_h = 60
    draw.rounded_rectangle([(250, b2_top), (W-250, b2_top+b2_h)], radius=10, fill=(245, 247, 250), outline=(120, 150, 190), width=2)
    draw.text((W//2, b2_top+20), "JDBC Protocol (MySQL Connector/J 8.0.33) over Port 3306", fill=(30, 100, 180), font=FONT_BOLD, anchor="mm")
    draw.text((W//2, b2_top+42), "Parameterized PreparedStatements | CallableStatements for Stored Procedures | Explicit Connection Pooling & Isolation", fill=(90, 100, 115), font=FONT_SMALL, anchor="mm")

    # Layer 3: Database Layer (MySQL 8.x)
    l3_top = b2_top + b2_h + 20
    l3_h = 390
    draw.rounded_rectangle([(60, l3_top), (W-60, l3_top+l3_h)], radius=12, fill=(248, 255, 250), outline=(70, 160, 100), width=2)
    draw.text((80, l3_top+20), "LAYER 3: RELATIONAL DATABASE LAYER (MySQL 8.x Server — Schema: nutriguide)", fill=(20, 100, 50), font=FONT_SECTION)
    draw.text((80, l3_top+45), "InnoDB Storage Engine | ACID Compliance | Strict 3NF Normalized Architecture | Stored Procedures | Views | Triggers", fill=(60, 110, 80), font=FONT_SMALL)

    # 4 Components of Database Layer
    cw = (W - 120 - 75) // 4
    cy = l3_top + 70
    ch = 290

    # 3.1 10 Normalized Relations
    cx1 = 80
    draw.rounded_rectangle([(cx1, cy), (cx1+cw, cy+ch)], radius=8, fill=(255, 255, 255), outline=(130, 190, 150), width=1)
    draw.text((cx1+cw//2, cy+20), "10 Relational Tables (3NF)", fill=(20, 90, 50), font=FONT_BOLD, anchor="mm")
    tables = [
        "1. user_profile (Demographics & Hash)",
        "2. nutrition_goal (Target Calories/Macros)",
        "3. food_category (Categorization)",
        "4. food (15 Reference Nutrient Items)",
        "5. meal (Header with User, Type, Date)",
        "6. meal_item (Associative M:N Table)",
        "7. meal_plan (Prospective Regimen)",
        "8. meal_plan_item (Associative Junction)",
        "9. recommendation (Persisted Guidance)",
        "10. activity_log (Audit Event Trail)"
    ]
    for idx, t_text in enumerate(tables):
        draw.text((cx1+15, cy+48 + idx*23), t_text, fill=(40, 70, 50), font=FONT_SMALL)

    # 3.2 Integrity & Constraints
    cx2 = cx1 + cw + 25
    draw.rounded_rectangle([(cx2, cy), (cx2+cw, cy+ch)], radius=8, fill=(255, 255, 255), outline=(130, 190, 150), width=1)
    draw.text((cx2+cw//2, cy+20), "Declarative Integrity Layer", fill=(20, 90, 50), font=FONT_BOLD, anchor="mm")
    constraints = [
        "• Primary Keys (AUTO_INCREMENT on all)",
        "• Foreign Keys with Referential Rules:",
        "    - meal -> meal_item: CASCADE",
        "    - food -> meal_item: RESTRICT",
        "    - user -> child tables: CASCADE",
        "• Unique Constraints:",
        "    - email in user_profile",
        "    - food_name in food",
        "• Check Constraints:",
        "    - height_cm > 0, weight_kg > 0",
        "    - age > 0, serving_size > 0",
        "    - target macros >= 0, quantity > 0",
        "• InnoDB REPEATABLE READ Isolation"
    ]
    for idx, c_text in enumerate(constraints):
        draw.text((cx2+15, cy+48 + idx*20), c_text, fill=(40, 70, 50), font=FONT_SMALL)

    # 3.3 Programmability Layer (Views, Functions, Procedures)
    cx3 = cx2 + cw + 25
    draw.rounded_rectangle([(cx3, cy), (cx3+cw, cy+ch)], radius=8, fill=(255, 255, 255), outline=(130, 190, 150), width=1)
    draw.text((cx3+cw//2, cy+20), "Programmability Layer", fill=(20, 90, 50), font=FONT_BOLD, anchor="mm")
    progs = [
        "SQL Views (Exp 4 & 5):",
        "  • daily_nutrition_summary",
        "      (Aggregates daily calories/macros)",
        "  • food_category_details",
        "      (Pre-joined foods with categories)",
        "Stored Functions (Exp 6):",
        "  • calculate_bmi(weight_kg, height_cm)",
        "      (DETERMINISTIC subprogram)",
        "  • get_total_calories(user_id, date)",
        "      (Macro summation routine)",
        "Stored Procedures (Exp 7):",
        "  • get_daily_summary()",
        "      (IN: user, date | OUT: cal, pro, carb, fat)"
    ]
    for idx, p_text in enumerate(progs):
        draw.text((cx3+15, cy+48 + idx*20), p_text, fill=(40, 70, 50), font=FONT_SMALL)

    # 3.4 Triggers & Audit Engine
    cx4 = cx3 + cw + 25
    draw.rounded_rectangle([(cx4, cy), (cx4+cw, cy+ch)], radius=8, fill=(255, 255, 255), outline=(130, 190, 150), width=1)
    draw.text((cx4+cw//2, cy+20), "Triggers & Auditing (Exp 8)", fill=(20, 90, 50), font=FONT_BOLD, anchor="mm")
    triggers = [
        "AFTER INSERT Trigger:",
        "  • trg_after_meal_insert",
        "      - Event: AFTER INSERT ON meal",
        "      - Action: Copies meal event directly",
        "        into activity_log audit trail.",
        "BEFORE INSERT / UPDATE Validation:",
        "  • trg_validate_meal_quantity",
        "      - Event: BEFORE INSERT ON meal_item",
        "      - Action: Validates quantity > 0.",
        "      - Raises: SQLSTATE '45000' SIGNAL",
        "        upon non-positive quantity.",
        "  • trg_validate_meal_quantity_update",
        "      - Enforces quantity > 0 on update."
    ]
    for idx, tr_text in enumerate(triggers):
        draw.text((cx4+15, cy+48 + idx*20), tr_text, fill=(40, 70, 50), font=FONT_SMALL)

    # Footer note
    draw.text((W//2, H-12), "Figure 3.1: NutriGuide System Architecture & Three-Tier Deployment Model", fill=(100, 115, 130), font=FONT_SMALL_BOLD, anchor="mm")

    path = os.path.join(OUTPUT_DIR, "system_architecture.png")
    im.save(path, quality=95)
    print("Saved:", path)

draw_system_architecture()
