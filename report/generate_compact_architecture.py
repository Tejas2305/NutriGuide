# -*- coding: utf-8 -*-
"""
NutriGuide - High-Legibility System Architecture Diagram Generator (Figure 3.1)
Renders with BIG, BOLD, HIGH-CONTRAST typography (16-32pt) so every word is
effortlessly readable on an A4 page in Microsoft Word without zooming.
"""

import os
from PIL import Image, ImageDraw, ImageFont

OUTPUT_DIR = r"D:\NutriGuide\report\images"
os.makedirs(OUTPUT_DIR, exist_ok=True)
IMG_PATH = os.path.join(OUTPUT_DIR, "system_architecture.png")

WINDIR = os.environ.get("WINDIR", "C:/Windows")
FONT_DIR = os.path.join(WINDIR, "Fonts")

def get_font(name="segoeuib.ttf", size=18):
    try:
        path = os.path.join(FONT_DIR, name)
        if os.path.exists(path):
            return ImageFont.truetype(path, size)
    except Exception:
        pass
    try:
        return ImageFont.truetype(os.path.join(FONT_DIR, "arialbd.ttf"), size)
    except Exception:
        return ImageFont.load_default()

FONT_TITLE = get_font("segoeuib.ttf", 28)
FONT_TIER = get_font("segoeuib.ttf", 20)
FONT_CARD_TITLE = get_font("segoeuib.ttf", 17)
FONT_ITEM = get_font("segoeuib.ttf", 15)
FONT_ITEM_REG = get_font("segoeui.ttf", 14)
FONT_BANNER = get_font("segoeuib.ttf", 16)
FONT_SUB = get_font("segoeui.ttf", 13)

def draw_high_legibility_architecture():
    W, H = 1300, 750
    im = Image.new("RGB", (W, H), color=(255, 255, 255))
    draw = ImageDraw.Draw(im)

    # 1. Outer Border & Header Banner
    draw.rounded_rectangle([(12, 10), (W-12, H-10)], radius=10, outline=(148, 163, 184), width=2)
    
    # Title Banner
    draw.rounded_rectangle([(12, 10), (W-12, 58)], radius=10, fill=(26, 54, 93))
    draw.rectangle([(12, 40), (W-12, 58)], fill=(26, 54, 93))
    draw.text((W//2, 34), "NutriGuide — System Architecture & Multi-Tier Topology", fill=(255, 255, 255), font=FONT_TITLE, anchor="mm")

    # =========================================================================
    # TIER 1: PRESENTATION LAYER
    # =========================================================================
    t1_y = 70
    t1_h = 165
    draw.rounded_rectangle([(25, t1_y), (W-25, t1_y + t1_h)], radius=8, fill=(239, 246, 255), outline=(37, 99, 235), width=2)
    
    # Tier Badge
    draw.rounded_rectangle([(35, t1_y + 8), (560, t1_y + 40)], radius=6, fill=(30, 64, 175))
    draw.text((297, t1_y + 24), "LAYER 1: PRESENTATION LAYER (Java Swing GUI)", fill=(255, 255, 255), font=FONT_TIER, anchor="mm")
    draw.text((580, t1_y + 24), "Event Dispatch Thread (EDT) + SwingWorker Async Execution", fill=(30, 41, 59), font=FONT_SUB)

    # 4 GUI Cards
    gui_cards = [
        ("Authentication & Profile", ["LoginFrame (PBKDF2)", "RegisterFrame", "ProfileFrame"], (219, 234, 254), (96, 165, 250)),
        ("Dashboard & Analytics", ["DashboardFrame", "Live Daily Calorie Meter", "Dynamic BMI Calculator"], (219, 234, 254), (96, 165, 250)),
        ("Nutrition & Meal Logs", ["MealLogFrame (Atomic)", "NutritionGoalFrame", "Food Catalog Selection"], (219, 234, 254), (96, 165, 250)),
        ("Advisory & Reports", ["RecommendationFrame", "ReportFrame (Summaries)", "Plaintext & Export View"], (219, 234, 254), (96, 165, 250))
    ]
    card_w = (W - 50 - 45) // 4
    for idx, (title, items, bg_c, border_c) in enumerate(gui_cards):
        cx = 35 + idx * (card_w + 15)
        cy = t1_y + 48
        draw.rounded_rectangle([(cx, cy), (cx + card_w, cy + 105)], radius=6, fill=bg_c, outline=border_c, width=2)
        draw.text((cx + card_w//2, cy + 20), title, fill=(15, 23, 42), font=FONT_CARD_TITLE, anchor="mm")
        for line_i, item_t in enumerate(items):
            draw.text((cx + 12, cy + 42 + line_i * 20), f"• {item_t}", fill=(30, 41, 59), font=FONT_ITEM)

    # =========================================================================
    # PROTOCOL 1: REST OVER LOCALHOST
    # =========================================================================
    p1_y = t1_y + t1_h + 10
    p1_h = 44
    draw.rounded_rectangle([(160, p1_y), (W-160, p1_y + p1_h)], radius=6, fill=(248, 250, 252), outline=(100, 116, 139), width=2)
    
    # Bi-directional arrows
    draw.line([(120, t1_y + t1_h + 2), (120, p1_y + p1_h + 10)], fill=(37, 99, 235), width=3)
    draw.polygon([(115, t1_y + t1_h + 8), (125, t1_y + t1_h + 8), (120, t1_y + t1_h)], fill=(37, 99, 235))
    draw.polygon([(115, p1_y + p1_h + 4), (125, p1_y + p1_h + 4), (120, p1_y + p1_h + 10)], fill=(37, 99, 235))

    draw.line([(W-120, t1_y + t1_h + 2), (W-120, p1_y + p1_h + 10)], fill=(37, 99, 235), width=3)
    draw.polygon([(W-125, t1_y + t1_h + 8), (W-115, t1_y + t1_h + 8), (W-120, t1_y + t1_h)], fill=(37, 99, 235))
    draw.polygon([(W-125, p1_y + p1_h + 4), (W-115, p1_y + p1_h + 4), (W-120, p1_y + p1_h + 10)], fill=(37, 99, 235))

    draw.text((W//2, p1_y + 15), "HTTP REST API over Loopback  (http://127.0.0.1:8081)", fill=(194, 65, 12), font=FONT_BANNER, anchor="mm")
    draw.text((W//2, p1_y + 32), "JSON Payloads | Bearer Token SessionManager | Parameter Validation", fill=(71, 85, 105), font=FONT_SUB, anchor="mm")

    # =========================================================================
    # TIER 2: APPLICATION & BUSINESS LOGIC LAYER
    # =========================================================================
    t2_y = p1_y + p1_h + 10
    t2_h = 185
    draw.rounded_rectangle([(25, t2_y), (W-25, t2_y + t2_h)], radius=8, fill=(254, 252, 232), outline=(217, 119, 6), width=2)
    
    # Tier Badge
    draw.rounded_rectangle([(35, t2_y + 8), (620, t2_y + 40)], radius=6, fill=(180, 83, 9))
    draw.text((327, t2_y + 24), "LAYER 2: APPLICATION & LOGIC LAYER (Java 8 Server)", fill=(255, 255, 255), font=FONT_TIER, anchor="mm")
    draw.text((640, t2_y + 24), "HttpServer | Thread-Safe SessionManager | Security & Password Hashing", fill=(113, 63, 18), font=FONT_SUB)

    # 3 Large Modules in Tier 2
    col_w = (W - 50 - 30) // 3
    
    # 2.1 REST Handlers & Security
    cx1 = 35
    cy2 = t2_y + 48
    draw.rounded_rectangle([(cx1, cy2), (cx1 + col_w, cy2 + 125)], radius=6, fill=(254, 243, 199), outline=(245, 158, 11), width=2)
    draw.text((cx1 + col_w//2, cy2 + 18), "REST Handlers & Security", fill=(120, 53, 15), font=FONT_CARD_TITLE, anchor="mm")
    handlers = [
        "AuthHandler: /api/auth/register, /login",
        "ProfileHandler: /api/profile (demographics)",
        "MealHandler: /api/meals (atomic multi-item)",
        "GoalHandler: /api/goals (active targets)",
        "PasswordUtil: Salted PBKDF2 (600,000 iter)"
    ]
    for idx, h_item in enumerate(handlers):
        draw.text((cx1 + 12, cy2 + 38 + idx * 16), f"• {h_item}", fill=(69, 26, 3), font=FONT_ITEM)

    # 2.2 Business Services & Rules Engine
    cx2 = cx1 + col_w + 15
    draw.rounded_rectangle([(cx2, cy2), (cx2 + col_w, cy2 + 125)], radius=6, fill=(240, 253, 244), outline=(74, 222, 128), width=2)
    draw.text((cx2 + col_w//2, cy2 + 18), "Business Services & Rules", fill=(20, 83, 45), font=FONT_CARD_TITLE, anchor="mm")
    services = [
        "AuthenticationService: Token issuance",
        "MealService: Transaction boundaries",
        "NutritionService: Goal compliance check",
        "RecommendationService: Rule Engine",
        "ReportService: Summaries & text export"
    ]
    for idx, s_item in enumerate(services):
        draw.text((cx2 + 12, cy2 + 38 + idx * 16), f"• {s_item}", fill=(20, 83, 45), font=FONT_ITEM)

    # 2.3 Data Access Objects (DAOs)
    cx3 = cx2 + col_w + 15
    draw.rounded_rectangle([(cx3, cy2), (cx3 + col_w, cy2 + 125)], radius=6, fill=(245, 243, 255), outline=(192, 132, 252), width=2)
    draw.text((cx3 + col_w//2, cy2 + 18), "Data Access Objects (DAOs)", fill=(88, 28, 135), font=FONT_CARD_TITLE, anchor="mm")
    daos = [
        "UserDAO & NutritionGoalDAO",
        "MealDAO & MealItemDAO (Atomic writes)",
        "FoodDAO & FoodCategoryDAO (Catalog)",
        "MealPlanDAO & MealPlanItemDAO",
        "DBConnectionFactory: PreparedStatement"
    ]
    for idx, d_item in enumerate(daos):
        draw.text((cx3 + 12, cy2 + 38 + idx * 15), f"• {d_item}", fill=(59, 7, 100), font=FONT_ITEM)

    # =========================================================================
    # PROTOCOL 2: JDBC PROTOCOL
    # =========================================================================
    p2_y = t2_y + t2_h + 10
    p2_h = 44
    draw.rounded_rectangle([(160, p2_y), (W-160, p2_y + p2_h)], radius=6, fill=(248, 250, 252), outline=(100, 116, 139), width=2)
    
    # Bi-directional arrows
    draw.line([(120, t2_y + t2_h + 2), (120, p2_y + p2_h + 10)], fill=(180, 83, 9), width=3)
    draw.polygon([(115, t2_y + t2_h + 8), (125, t2_y + t2_h + 8), (120, t2_y + t2_h)], fill=(180, 83, 9))
    draw.polygon([(115, p2_y + p2_h + 4), (125, p2_y + p2_h + 4), (120, p2_y + p2_h + 10)], fill=(180, 83, 9))

    draw.line([(W-120, t2_y + t2_h + 2), (W-120, p2_y + p2_h + 10)], fill=(180, 83, 9), width=3)
    draw.polygon([(W-125, t2_y + t2_h + 8), (W-115, t2_y + t2_h + 8), (W-120, t2_y + t2_h)], fill=(180, 83, 9))
    draw.polygon([(W-125, p2_y + p2_h + 4), (W-115, p2_y + p2_h + 4), (W-120, p2_y + p2_h + 10)], fill=(180, 83, 9))

    draw.text((W//2, p2_y + 15), "JDBC Protocol (MySQL Connector/J 8.0.33)  over Port 3306", fill=(30, 64, 175), font=FONT_BANNER, anchor="mm")
    draw.text((W//2, p2_y + 32), "Parameterized PreparedStatements | CallableStatements | Transaction Autocommit Control (ACID)", fill=(71, 85, 105), font=FONT_SUB, anchor="mm")

    # =========================================================================
    # TIER 3: DATABASE LAYER (MySQL 8.x InnoDB)
    # =========================================================================
    t3_y = p2_y + p2_h + 10
    t3_h = 200
    draw.rounded_rectangle([(25, t3_y), (W-25, t3_y + t3_h)], radius=8, fill=(240, 253, 244), outline=(22, 163, 74), width=2)
    
    # Tier Badge
    draw.rounded_rectangle([(35, t3_y + 8), (660, t3_y + 40)], radius=6, fill=(21, 128, 61))
    draw.text((347, t3_y + 24), "LAYER 3: RELATIONAL DATABASE (MySQL 8.x InnoDB)", fill=(255, 255, 255), font=FONT_TIER, anchor="mm")
    draw.text((680, t3_y + 24), "Schema: nutriguide | ACID Transactions | 3NF/BCNF Relations", fill=(20, 83, 45), font=FONT_SUB)

    # 3 Submodules in Tier 3
    # 3.1 10 Relations
    cx1 = 35
    cy3 = t3_y + 48
    draw.rounded_rectangle([(cx1, cy3), (cx1 + col_w, cy3 + 140)], radius=6, fill=(255, 255, 255), outline=(187, 247, 208), width=2)
    draw.text((cx1 + col_w//2, cy3 + 18), "10 Relational Tables (3NF)", fill=(21, 128, 61), font=FONT_CARD_TITLE, anchor="mm")
    tables_txt = [
        "user_profile, nutrition_goal",
        "food_category, food (15 items)",
        "meal, meal_item (M:N junction)",
        "meal_plan, meal_plan_item",
        "recommendation, activity_log"
    ]
    for idx, t_text in enumerate(tables_txt):
        draw.text((cx1 + 14, cy3 + 38 + idx * 20), f"• {t_text}", fill=(15, 23, 42), font=FONT_ITEM)

    # 3.2 Integrity Rules
    cx2 = cx1 + col_w + 15
    draw.rounded_rectangle([(cx2, cy3), (cx2 + col_w, cy3 + 140)], radius=6, fill=(255, 255, 255), outline=(187, 247, 208), width=2)
    draw.text((cx2 + col_w//2, cy3 + 18), "Integrity & Referential Rules", fill=(21, 128, 61), font=FONT_CARD_TITLE, anchor="mm")
    integrity_txt = [
        "Primary Keys: Auto-increment INT PKs",
        "Unique Keys: uq_user_email, uq_food_name",
        "Foreign Keys: ON DELETE CASCADE",
        "Catalog Protection: ON DELETE RESTRICT",
        "CHECK: age > 0, height_cm > 0, weight_kg > 0"
    ]
    for idx, i_text in enumerate(integrity_txt):
        draw.text((cx2 + 14, cy3 + 38 + idx * 20), f"• {i_text}", fill=(15, 23, 42), font=FONT_ITEM)

    # 3.3 Database Programmability
    cx3 = cx2 + col_w + 15
    draw.rounded_rectangle([(cx3, cy3), (cx3 + col_w, cy3 + 140)], radius=6, fill=(255, 255, 255), outline=(187, 247, 208), width=2)
    draw.text((cx3 + col_w//2, cy3 + 18), "Database Programmability", fill=(21, 128, 61), font=FONT_CARD_TITLE, anchor="mm")
    prog_txt = [
        "Views: daily_nutrition_summary, food_details",
        "Functions: calculate_bmi, get_total_calories",
        "Procedure: get_daily_summary (4 OUT params)",
        "Trigger: trg_after_meal_insert (Audit Log)",
        "Trigger: trg_validate_meal_quantity (SIGNAL)"
    ]
    for idx, p_text in enumerate(prog_txt):
        draw.text((cx3 + 14, cy3 + 38 + idx * 20), f"• {p_text}", fill=(15, 23, 42), font=FONT_ITEM)

    im.save(IMG_PATH, quality=95)
    print(f"Generated High-Legibility Architecture Diagram: {IMG_PATH} ({W}x{H})")

if __name__ == "__main__":
    draw_high_legibility_architecture()
