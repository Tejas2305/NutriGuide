# -*- coding: utf-8 -*-
"""
NutriGuide - Compact System Architecture Diagram Generator (Figure 3.1)
Renders a streamlined, highly readable, A4-proportioned System Architecture diagram.
Optimized to fit neatly on an A4 page alongside section headings and text in Microsoft Word.
"""

import os
from PIL import Image, ImageDraw, ImageFont

OUTPUT_DIR = r"D:\NutriGuide\report\images"
os.makedirs(OUTPUT_DIR, exist_ok=True)
IMG_PATH = os.path.join(OUTPUT_DIR, "system_architecture.png")

WINDIR = os.environ.get("WINDIR", "C:/Windows")
FONT_DIR = os.path.join(WINDIR, "Fonts")

def get_font(name="segoeui.ttf", size=13):
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

FONT_HEADER = get_font("segoeuib.ttf", 20)
FONT_TIER_TITLE = get_font("segoeuib.ttf", 15)
FONT_CARD_TITLE = get_font("segoeuib.ttf", 13)
FONT_TEXT_BOLD = get_font("segoeuib.ttf", 11)
FONT_TEXT = get_font("segoeui.ttf", 11)
FONT_SUB = get_font("segoeui.ttf", 10)
FONT_PROTO = get_font("segoeuib.ttf", 11)

def draw_compact_architecture():
    # Width 1280, Height 780 (Compact 16:10 aspect ratio, fits on A4 page with 3.8 - 4.5 in width)
    W, H = 1320, 800
    im = Image.new("RGB", (W, H), color=(255, 255, 255))
    draw = ImageDraw.Draw(im)

    # 1. Outer Border & Main Title Banner
    draw.rounded_rectangle([(15, 12), (W-15, H-12)], radius=10, outline=(203, 213, 225), width=2)
    
    # Title bar
    draw.rounded_rectangle([(15, 12), (W-15, 62)], radius=10, fill=(26, 54, 93))
    # Fix bottom corners of title bar
    draw.rectangle([(15, 45), (W-15, 62)], fill=(26, 54, 93))
    draw.text((W//2, 37), "NutriGuide — System Architecture & Multi-Tier Topology", fill=(255, 255, 255), font=FONT_HEADER, anchor="mm")

    # =========================================================================
    # TIER 1: PRESENTATION LAYER (Java Swing Desktop GUI)
    # =========================================================================
    t1_y = 75
    t1_h = 160
    draw.rounded_rectangle([(35, t1_y), (W-35, t1_y + t1_h)], radius=8, fill=(240, 247, 255), outline=(66, 153, 225), width=2)
    
    # Tier Title Badge
    draw.rounded_rectangle([(45, t1_y + 8), (560, t1_y + 36)], radius=6, fill=(43, 108, 176))
    draw.text((302, t1_y + 22), "LAYER 1: PRESENTATION TIER (Java Swing GUI Client)", fill=(255, 255, 255), font=FONT_TIER_TITLE, anchor="mm")
    draw.text((580, t1_y + 22), "• Runs on Swing Event Dispatch Thread (EDT) with background SwingWorker threads", fill=(74, 85, 104), font=FONT_SUB)

    # 4 GUI Subsystem Cards
    gui_cards = [
        ("Authentication & Profile", ["LoginFrame (PBKDF2 Client)", "RegisterFrame (Intake)", "ProfileFrame (Credentials)"], (225, 239, 254), (147, 197, 253)),
        ("Dashboard & Analytics", ["DashboardFrame (Live Metrics)", "Real-time Calorie Meter", "Dynamic BMI Status Display"], (219, 234, 254), (147, 197, 253)),
        ("Nutrition & Meal Logs", ["MealLogFrame (Atomic Logging)", "NutritionGoalFrame (Targets)", "Food Catalog Search / Select"], (238, 242, 255), (165, 180, 252)),
        ("Advisory & Reports", ["RecommendationFrame (Rules)", "ReportFrame (Range Summaries)", "Plaintext & Export View"], (245, 243, 255), (196, 181, 253))
    ]
    card_w = (W - 70 - 45) // 4
    for idx, (title, items, bg_c, border_c) in enumerate(gui_cards):
        cx = 45 + idx * (card_w + 15)
        cy = t1_y + 45
        draw.rounded_rectangle([(cx, cy), (cx + card_w, cy + 102)], radius=6, fill=bg_c, outline=border_c, width=1)
        draw.text((cx + card_w//2, cy + 18), title, fill=(30, 58, 138), font=FONT_CARD_TITLE, anchor="mm")
        for line_i, item_t in enumerate(items):
            draw.text((cx + 12, cy + 38 + line_i * 19), f"• {item_t}", fill=(51, 65, 85), font=FONT_TEXT)

    # =========================================================================
    # PROTOCOL 1: REST OVER LOCALHOST
    # =========================================================================
    p1_y = t1_y + t1_h + 10
    p1_h = 42
    draw.rounded_rectangle([(180, p1_y), (W-180, p1_y + p1_h)], radius=6, fill=(248, 250, 252), outline=(148, 163, 184), width=1)
    
    # Bi-directional arrows
    draw.line([(140, t1_y + t1_h + 2), (140, p1_y + p1_h + 10)], fill=(43, 108, 176), width=2)
    draw.polygon([(136, t1_y + t1_h + 8), (144, t1_y + t1_h + 8), (140, t1_y + t1_h)], fill=(43, 108, 176))
    draw.polygon([(136, p1_y + p1_h + 4), (144, p1_y + p1_h + 4), (140, p1_y + p1_h + 10)], fill=(43, 108, 176))
    
    draw.line([(W-140, t1_y + t1_h + 2), (W-140, p1_y + p1_h + 10)], fill=(43, 108, 176), width=2)
    draw.polygon([(W-144, t1_y + t1_h + 8), (W-136, t1_y + t1_h + 8), (W-140, t1_y + t1_h)], fill=(43, 108, 176))
    draw.polygon([(W-144, p1_y + p1_h + 4), (W-136, p1_y + p1_h + 4), (W-140, p1_y + p1_h + 10)], fill=(43, 108, 176))

    draw.text((W//2, p1_y + 14), "HTTP REST API over Loopback  (http://127.0.0.1:8081)", fill=(194, 65, 12), font=FONT_PROTO, anchor="mm")
    draw.text((W//2, p1_y + 29), "JSON Payloads | Bearer Token SessionManager | Parameter Validation & Defensive Error Mapping", fill=(100, 116, 139), font=FONT_SUB, anchor="mm")

    # =========================================================================
    # TIER 2: APPLICATION & BUSINESS LOGIC LAYER
    # =========================================================================
    t2_y = p1_y + p1_h + 12
    t2_h = 195
    draw.rounded_rectangle([(35, t2_y), (W-35, t2_y + t2_h)], radius=8, fill=(255, 251, 235), outline=(217, 119, 6), width=2)
    
    # Tier Title Badge
    draw.rounded_rectangle([(45, t2_y + 8), (620, t2_y + 36)], radius=6, fill=(180, 83, 9))
    draw.text((332, t2_y + 22), "LAYER 2: APPLICATION & BUSINESS SERVICES (Java 8 Server)", fill=(255, 255, 255), font=FONT_TIER_TITLE, anchor="mm")
    draw.text((640, t2_y + 22), "• Java HttpServer (com.sun.net.httpserver) | SessionManager | Business Rules", fill=(113, 63, 18), font=FONT_SUB)

    # 3 Large Component Modules
    col_w = (W - 70 - 30) // 3
    
    # Module 2.1: REST Handlers & Security
    cx1 = 45
    cy1 = t2_y + 45
    draw.rounded_rectangle([(cx1, cy1), (cx1 + col_w, cy1 + 138)], radius=6, fill=(254, 243, 199), outline=(252, 211, 77), width=1)
    draw.text((cx1 + col_w//2, cy1 + 18), "REST Handlers & Security", fill=(146, 64, 14), font=FONT_CARD_TITLE, anchor="mm")
    handlers_list = [
        "AuthHandler (/api/auth/register, /login)",
        "ProfileHandler (/api/profile - user data)",
        "MealHandler (/api/meals - multi-item logs)",
        "GoalHandler (/api/goals - active targets)",
        "Security: PasswordUtil (600,000 iter PBKDF2)",
        "Validation: ValidationUtil defensive guards"
    ]
    for idx, h_item in enumerate(handlers_list):
        draw.text((cx1 + 12, cy1 + 36 + idx * 16), f"• {h_item}", fill=(69, 26, 3), font=FONT_TEXT)

    # Module 2.2: Business Services & Recommendation Engine
    cx2 = cx1 + col_w + 15
    draw.rounded_rectangle([(cx2, cy1), (cx2 + col_w, cy1 + 138)], radius=6, fill=(240, 253, 244), outline=(134, 239, 172), width=1)
    draw.text((cx2 + col_w//2, cy1 + 18), "Business Services & Rules Engine", fill=(22, 101, 52), font=FONT_CARD_TITLE, anchor="mm")
    services_list = [
        "AuthenticationService: Token issuance & verify",
        "MealService: Atomic transaction boundary",
        "NutritionService: Goal compliance checking",
        "RecommendationService & Rule Engine:",
        "  - Evaluates caloric deficit vs. surplus",
        "  - Analyzes protein intake vs. WHO thresholds"
    ]
    for idx, s_item in enumerate(services_list):
        draw.text((cx2 + 12, cy1 + 36 + idx * 16), f"• {s_item}", fill=(20, 83, 45), font=FONT_TEXT)

    # Module 2.3: Data Access Layer (DAOs)
    cx3 = cx2 + col_w + 15
    draw.rounded_rectangle([(cx3, cy1), (cx3 + col_w, cy1 + 138)], radius=6, fill=(238, 242, 255), outline=(165, 180, 252), width=1)
    draw.text((cx3 + col_w//2, cy1 + 18), "Data Access Layer (JDBC DAOs)", fill=(55, 48, 163), font=FONT_CARD_TITLE, anchor="mm")
    dao_list = [
        "UserDAO & NutritionGoalDAO",
        "MealDAO & MealItemDAO (Atomic writes)",
        "FoodDAO & FoodCategoryDAO (Catalog)",
        "RecommendationDAO & ActivityLogDAO",
        "MealPlanDAO & MealPlanItemDAO",
        "DBConnectionFactory (HikariCP / DriverMgr)"
    ]
    for idx, d_item in enumerate(dao_list):
        draw.text((cx3 + 12, cy1 + 36 + idx * 16), f"• {d_item}", fill=(30, 27, 75), font=FONT_TEXT)

    # =========================================================================
    # PROTOCOL 2: JDBC PROTOCOL
    # =========================================================================
    p2_y = t2_y + t2_h + 10
    p2_h = 42
    draw.rounded_rectangle([(180, p2_y), (W-180, p2_y + p2_h)], radius=6, fill=(248, 250, 252), outline=(148, 163, 184), width=1)
    
    # Bi-directional arrows
    draw.line([(140, t2_y + t2_h + 2), (140, p2_y + p2_h + 10)], fill=(180, 83, 9), width=2)
    draw.polygon([(136, t2_y + t2_h + 8), (144, t2_y + t2_h + 8), (140, t2_y + t2_h)], fill=(180, 83, 9))
    draw.polygon([(136, p2_y + p2_h + 4), (144, p2_y + p2_h + 4), (140, p2_y + p2_h + 10)], fill=(180, 83, 9))
    
    draw.line([(W-140, t2_y + t2_h + 2), (W-140, p2_y + p2_h + 10)], fill=(180, 83, 9), width=2)
    draw.polygon([(W-144, t2_y + t2_h + 8), (W-136, t2_y + t2_h + 8), (W-140, t2_y + t2_h)], fill=(180, 83, 9))
    draw.polygon([(W-144, p2_y + p2_h + 4), (W-136, p2_y + p2_h + 4), (W-140, p2_y + p2_h + 10)], fill=(180, 83, 9))

    draw.text((W//2, p2_y + 14), "JDBC Protocol (MySQL Connector/J 8.0.33)  over Port 3306", fill=(30, 64, 175), font=FONT_PROTO, anchor="mm")
    draw.text((W//2, p2_y + 29), "Parameterized SQL PreparedStatements | CallableStatements | Transaction Autocommit Control (ACID)", fill=(100, 116, 139), font=FONT_SUB, anchor="mm")

    # =========================================================================
    # TIER 3: DATABASE LAYER (MySQL 8.x InnoDB)
    # =========================================================================
    t3_y = p2_y + p2_h + 12
    t3_h = 220
    draw.rounded_rectangle([(35, t3_y), (W-35, t3_y + t3_h)], radius=8, fill=(240, 253, 244), outline=(22, 163, 74), width=2)
    
    # Tier Title Badge
    draw.rounded_rectangle([(45, t3_y + 8), (640, t3_y + 36)], radius=6, fill=(21, 128, 61))
    draw.text((342, t3_y + 22), "LAYER 3: RELATIONAL DATABASE (MySQL 8.x InnoDB — Schema: nutriguide)", fill=(255, 255, 255), font=FONT_TIER_TITLE, anchor="mm")
    draw.text((660, t3_y + 22), "• ACID Compliance | 3NF/BCNF Normalized | Integrity Constraints | Stored Routines", fill=(20, 83, 45), font=FONT_SUB)

    # 3 Database Submodules
    # 3.1 10 Normalized Tables
    cx1 = 45
    cy3 = t3_y + 45
    draw.rounded_rectangle([(cx1, cy3), (cx1 + col_w, cy3 + 162)], radius=6, fill=(255, 255, 255), outline=(187, 247, 208), width=1)
    draw.text((cx1 + col_w//2, cy3 + 16), "10 Relational Tables (3NF/BCNF)", fill=(21, 128, 61), font=FONT_CARD_TITLE, anchor="mm")
    tables_left = [
        "1. user_profile (User demographics & hash)",
        "2. nutrition_goal (Target calories/macros)",
        "3. food_category (5 Food classifications)",
        "4. food (15 Reference nutrient items)",
        "5. meal (Meal header: user, type, date)",
        "6. meal_item (Associative M:N line items)",
        "7. meal_plan (Scheduled diet plan header)",
        "8. meal_plan_item (Associative plan items)",
        "9. recommendation (Diet advisory log)",
        "10. activity_log (Audit trail records)"
    ]
    for idx, t_text in enumerate(tables_left):
        draw.text((cx1 + 10, cy3 + 32 + idx * 13), t_text, fill=(30, 41, 59), font=FONT_SUB)

    # 3.2 Integrity, Keys & Constraints
    cx2 = cx1 + col_w + 15
    draw.rounded_rectangle([(cx2, cy3), (cx2 + col_w, cy3 + 162)], radius=6, fill=(255, 255, 255), outline=(187, 247, 208), width=1)
    draw.text((cx2 + col_w//2, cy3 + 16), "Integrity & Referential Rules", fill=(21, 128, 61), font=FONT_CARD_TITLE, anchor="mm")
    rules_list = [
        "Primary Keys: Surrogate auto-increment PKs",
        "Unique Keys: uq_user_email, uq_food_name",
        "Foreign Keys with Explicit Actions:",
        "  - user_profile -> dependents: ON DELETE CASCADE",
        "  - meal -> meal_item: ON DELETE CASCADE",
        "  - food -> meal_item: ON DELETE RESTRICT",
        "  - food_category -> food: ON DELETE RESTRICT",
        "CHECK Constraints: age > 0, height_cm > 0,",
        "  weight_kg > 0, quantity > 0, calories >= 0"
    ]
    for idx, r_text in enumerate(rules_list):
        draw.text((cx2 + 10, cy3 + 32 + idx * 14), r_text, fill=(30, 41, 59), font=FONT_SUB)

    # 3.3 Database Programmability
    cx3 = cx2 + col_w + 15
    draw.rounded_rectangle([(cx3, cy3), (cx3 + col_w, cy3 + 162)], radius=6, fill=(255, 255, 255), outline=(187, 247, 208), width=1)
    draw.text((cx3 + col_w//2, cy3 + 16), "Database Programmability", fill=(21, 128, 61), font=FONT_CARD_TITLE, anchor="mm")
    prog_list = [
        "Views (Exp 4 & 5):",
        "  - daily_nutrition_summary (pre-aggregated)",
        "  - food_category_details (pre-joined catalog)",
        "Stored Functions (Exp 6):",
        "  - calculate_bmi(weight_kg, height_cm)",
        "  - get_total_calories(user_id, date)",
        "Stored Procedure (Exp 7):",
        "  - get_daily_summary(user_id, date, OUT 4 macros)",
        "Triggers (Exp 8):",
        "  - trg_after_meal_insert (Auto audit log)",
        "  - trg_validate_meal_quantity (SIGNAL 45000)"
    ]
    for idx, p_text in enumerate(prog_list):
        draw.text((cx3 + 10, cy3 + 32 + idx * 13), p_text, fill=(30, 41, 59), font=FONT_SUB)

    im.save(IMG_PATH, quality=95)
    print(f"Generated Compact System Architecture Diagram: {IMG_PATH} ({W}x{H})")

if __name__ == "__main__":
    draw_compact_architecture()
