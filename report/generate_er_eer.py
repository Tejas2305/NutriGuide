"""
NutriGuide ER and EER Diagram Generator
Generates Figure 6.1 (ER Diagram - Chen Notation) and Figure 6.2 (EER Diagram - Relational Schema with Cardinalities)
"""

import os
from PIL import Image, ImageDraw, ImageFont

OUTPUT_DIR = r"D:\NutriGuide\report\images"
os.makedirs(OUTPUT_DIR, exist_ok=True)

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
FONT_SECTION = get_font("segoeuib.ttf", 16)
FONT_BOLD = get_font("segoeuib.ttf", 13)
FONT_REGULAR = get_font("segoeui.ttf", 12)
FONT_SMALL = get_font("segoeui.ttf", 11)
FONT_SMALL_BOLD = get_font("segoeuib.ttf", 11)

def draw_diamond(draw, center, size_w, size_h, fill, outline, width=2):
    cx, cy = center
    hw, hh = size_w // 2, size_h // 2
    points = [(cx, cy - hh), (cx + hw, cy), (cx, cy + hh), (cx - hw, cy)]
    draw.polygon(points, fill=fill, outline=outline)

# ==============================================================================
# 1. ER DIAGRAM (Chen Notation)
# ==============================================================================
def draw_er_diagram():
    W, H = 2200, 1600
    im = Image.new("RGB", (W, H), color=(253, 254, 255))
    draw = ImageDraw.Draw(im)

    # Canvas border & Header
    draw.rectangle([(20, 20), (W-20, H-20)], outline=(210, 225, 245), width=2)
    draw.rectangle([(20, 20), (W-20, 85)], fill=(24, 43, 73))
    draw.text((W//2, 52), "NutriGuide — Entity-Relationship (ER) Diagram (Chen Notation)", fill=(255, 255, 255), font=FONT_TITLE, anchor="mm")

    # Legend at top right
    leg_x, leg_y = W - 450, 105
    draw.rectangle([(leg_x, leg_y), (W-40, leg_y+75)], fill=(245, 248, 255), outline=(180, 200, 230), width=1)
    draw.text((leg_x+10, leg_y+10), "Legend (Chen Notation):", fill=(20, 40, 80), font=FONT_SMALL_BOLD)
    # Rectangle
    draw.rectangle([(leg_x+15, leg_y+32), (leg_x+65, leg_y+50)], fill=(220, 235, 255), outline=(50, 100, 200), width=2)
    draw.text((leg_x+75, leg_y+41), "Entity", fill=(40, 40, 40), font=FONT_SMALL, anchor="lm")
    # Diamond
    draw_diamond(draw, (leg_x+160, leg_y+41), 36, 22, fill=(255, 230, 235), outline=(200, 60, 90), width=2)
    draw.text((leg_x+185, leg_y+41), "Relationship", fill=(40, 40, 40), font=FONT_SMALL, anchor="lm")
    # Ellipse
    draw.ellipse([(leg_x+285, leg_y+32), (leg_x+335, leg_y+50)], fill=(255, 252, 235), outline=(180, 150, 50), width=1)
    draw.text((leg_x+345, leg_y+41), "Attribute", fill=(40, 40, 40), font=FONT_SMALL, anchor="lm")

    # Entities and their centers
    # 1. USER_PROFILE (Center hub)
    u_cx, u_cy = 500, 550
    # 2. NUTRITION_GOAL (Top left)
    g_cx, g_cy = 200, 220
    # 3. MEAL (Center bottom)
    m_cx, m_cy = 500, 1000
    # 4. MEAL_ITEM (Associative, Center bottom-right)
    mi_cx, mi_cy = 1050, 1000
    # 5. FOOD (Right middle)
    f_cx, f_cy = 1600, 1000
    # 6. FOOD_CATEGORY (Far Right top)
    fc_cx, fc_cy = 1600, 450
    # 7. MEAL_PLAN (Left middle)
    mp_cx, mp_cy = 1050, 550
    # 8. MEAL_PLAN_ITEM (Center)
    mpi_cx, mpi_cy = 1350, 750
    # 9. RECOMMENDATION (Top right)
    r_cx, r_cy = 950, 220
    # 10. ACTIVITY_LOG (Bottom left)
    a_cx, a_cy = 200, 850

    entities = [
        ("USER_PROFILE", (u_cx, u_cy), 180, 60),
        ("NUTRITION_GOAL", (g_cx, g_cy), 180, 60),
        ("MEAL", (m_cx, m_cy), 160, 60),
        ("MEAL_ITEM", (mi_cx, mi_cy), 160, 60),
        ("FOOD", (f_cx, f_cy), 160, 60),
        ("FOOD_CATEGORY", (fc_cx, fc_cy), 180, 60),
        ("MEAL_PLAN", (mp_cx, mp_cy), 160, 60),
        ("MEAL_PLAN_ITEM", (mpi_cx, mpi_cy), 180, 60),
        ("RECOMMENDATION", (r_cx, r_cy), 190, 60),
        ("ACTIVITY_LOG", (a_cx, a_cy), 170, 60),
    ]

    # Relationships (Diamond, center, text, connected entities)
    rels = [
        ("sets", ((u_cx + g_cx)//2, (u_cy + g_cy)//2), (u_cx, u_cy), (g_cx, g_cy), "1", "N"),
        ("logs", ((u_cx + m_cx)//2, (u_cy + m_cy)//2), (u_cx, u_cy), (m_cx, m_cy), "1", "N"),
        ("composed_of", ((m_cx + mi_cx)//2, (m_cy + mi_cy)//2), (m_cx, m_cy), (mi_cx, mi_cy), "1", "N"),
        ("includes", ((f_cx + mi_cx)//2, (f_cy + mi_cy)//2), (f_cx, f_cy), (mi_cx, mi_cy), "1", "N"),
        ("classifies", ((fc_cx + f_cx)//2, (fc_cy + f_cy)//2), (fc_cx, fc_cy), (f_cx, f_cy), "1", "N"),
        ("schedules", ((u_cx + mp_cx)//2, (u_cy + mp_cy)//2), (u_cx, u_cy), (mp_cx, mp_cy), "1", "N"),
        ("specifies", ((mp_cx + mpi_cx)//2, (mp_cy + mpi_cy)//2), (mp_cx, mp_cy), (mpi_cx, mpi_cy), "1", "N"),
        ("references", ((f_cx + mpi_cx)//2, (f_cy + mpi_cy)//2), (f_cx, f_cy), (mpi_cx, mpi_cy), "1", "N"),
        ("receives", ((u_cx + r_cx)//2, (u_cy + r_cy)//2), (u_cx, u_cy), (r_cx, r_cy), "1", "N"),
        ("audits", ((u_cx + a_cx)//2, (u_cy + a_cy)//2), (u_cx, u_cy), (a_cx, a_cy), "1", "N"),
    ]

    # Draw lines connecting entities and relationships
    for name, r_pos, e1_pos, e2_pos, c1, c2 in rels:
        draw.line([e1_pos, r_pos], fill=(70, 90, 120), width=2)
        draw.line([r_pos, e2_pos], fill=(70, 90, 120), width=2)
        # Cardinality labels
        mid1 = ((e1_pos[0]*2 + r_pos[0])//3, (e1_pos[1]*2 + r_pos[1])//3 - 10)
        mid2 = ((e2_pos[0]*2 + r_pos[0])//3, (e2_pos[1]*2 + r_pos[1])//3 - 10)
        draw.text(mid1, c1, fill=(180, 30, 30), font=FONT_BOLD)
        draw.text(mid2, c2, fill=(180, 30, 30), font=FONT_BOLD)

    # Draw relationship diamonds
    for name, r_pos, _, _, _, _ in rels:
        draw_diamond(draw, r_pos, 110, 60, fill=(255, 235, 240), outline=(210, 70, 90), width=2)
        draw.text(r_pos, name, fill=(150, 20, 40), font=FONT_BOLD, anchor="mm")

    # Draw entities
    for name, pos, w, h in entities:
        cx, cy = pos
        draw.rectangle([(cx-w//2, cy-h//2), (cx+w//2, cy+h//2)], fill=(225, 240, 255), outline=(40, 90, 180), width=3)
        draw.text((cx, cy), name, fill=(10, 40, 110), font=FONT_BOLD, anchor="mm")

    # Attributes for USER_PROFILE
    u_attrs = [
        ("user_id (PK)", (u_cx - 200, u_cy - 120), True),
        ("name", (u_cx - 240, u_cy - 50), False),
        ("email (UQ)", (u_cx - 230, u_cy + 30), False),
        ("password_hash", (u_cx - 190, u_cy + 100), False),
        ("age", (u_cx - 80, u_cy + 130), False),
        ("gender", (u_cx + 40, u_cy + 130), False),
        ("height_cm", (u_cx + 140, u_cy + 120), False),
        ("weight_kg", (u_cx + 170, u_cy + 60), False),
        ("activity_level", (u_cx + 160, u_cy - 100), False),
    ]
    for attr, pos, is_pk in u_attrs:
        draw.line([(u_cx, u_cy), pos], fill=(160, 180, 200), width=1)
        ax, ay = pos
        aw, ah = 110, 30
        draw.ellipse([(ax-aw//2, ay-ah//2), (ax+aw//2, ay+ah//2)], fill=(255, 253, 230), outline=(170, 140, 40), width=1)
        draw.text((ax, ay), attr, fill=(100, 70, 10), font=FONT_SMALL_BOLD if is_pk else FONT_SMALL, anchor="mm")
        if is_pk:
            draw.line([(ax-32, ay+7), (ax+32, ay+7)], fill=(100, 70, 10), width=1)

    # Attributes for FOOD
    f_attrs = [
        ("food_id (PK)", (f_cx + 180, f_cy - 100), True),
        ("food_name (UQ)", (f_cx + 200, f_cy - 40), False),
        ("serving_size", (f_cx + 200, f_cy + 20), False),
        ("calories", (f_cx + 180, f_cy + 80), False),
        ("protein", (f_cx + 100, f_cy + 120), False),
        ("carbohydrates", (f_cx - 20, f_cy + 120), False),
        ("fat", (f_cx - 120, f_cy + 120), False),
    ]
    for attr, pos, is_pk in f_attrs:
        draw.line([(f_cx, f_cy), pos], fill=(160, 180, 200), width=1)
        ax, ay = pos
        aw, ah = 110, 30
        draw.ellipse([(ax-aw//2, ay-ah//2), (ax+aw//2, ay+ah//2)], fill=(255, 253, 230), outline=(170, 140, 40), width=1)
        draw.text((ax, ay), attr, fill=(100, 70, 10), font=FONT_SMALL_BOLD if is_pk else FONT_SMALL, anchor="mm")
        if is_pk:
            draw.line([(ax-32, ay+7), (ax+32, ay+7)], fill=(100, 70, 10), width=1)

    # Attributes for MEAL & MEAL_ITEM
    m_attrs = [
        ("meal_id (PK)", (m_cx - 160, m_cy - 50), True),
        ("meal_type", (m_cx - 160, m_cy + 20), False),
        ("meal_date", (m_cx - 120, m_cy + 90), False),
    ]
    for attr, pos, is_pk in m_attrs:
        draw.line([(m_cx, m_cy), pos], fill=(160, 180, 200), width=1)
        ax, ay = pos
        aw, ah = 95, 28
        draw.ellipse([(ax-aw//2, ay-ah//2), (ax+aw//2, ay+ah//2)], fill=(255, 253, 230), outline=(170, 140, 40), width=1)
        draw.text((ax, ay), attr, fill=(100, 70, 10), font=FONT_SMALL_BOLD if is_pk else FONT_SMALL, anchor="mm")
        if is_pk:
            draw.line([(ax-30, ay+6), (ax+30, ay+6)], fill=(100, 70, 10), width=1)

    mi_attrs = [
        ("meal_item_id (PK)", (mi_cx - 20, mi_cy + 90), True),
        ("quantity", (mi_cx + 80, mi_cy + 90), False),
    ]
    for attr, pos, is_pk in mi_attrs:
        draw.line([(mi_cx, mi_cy), pos], fill=(160, 180, 200), width=1)
        ax, ay = pos
        aw, ah = 105, 28
        draw.ellipse([(ax-aw//2, ay-ah//2), (ax+aw//2, ay+ah//2)], fill=(255, 253, 230), outline=(170, 140, 40), width=1)
        draw.text((ax, ay), attr, fill=(100, 70, 10), font=FONT_SMALL_BOLD if is_pk else FONT_SMALL, anchor="mm")
        if is_pk:
            draw.line([(ax-40, ay+6), (ax+40, ay+6)], fill=(100, 70, 10), width=1)

    # Footer note
    draw.text((W//2, H-25), "Figure 6.1: NutriGuide Conceptual ER Diagram (Chen Notation) with M:N Associative Resolution", fill=(90, 105, 120), font=FONT_SMALL_BOLD, anchor="mm")

    path = os.path.join(OUTPUT_DIR, "er_diagram.png")
    im.save(path, quality=95)
    print("Saved:", path)

# ==============================================================================
# 2. EER DIAGRAM (Enhanced ER / Relational Schema)
# ==============================================================================
def draw_eer_diagram():
    W, H = 2200, 1600
    im = Image.new("RGB", (W, H), color=(252, 254, 255))
    draw = ImageDraw.Draw(im)

    draw.rectangle([(20, 20), (W-20, H-20)], outline=(200, 215, 235), width=2)
    draw.rectangle([(20, 20), (W-20, 85)], fill=(18, 50, 95))
    draw.text((W//2, 52), "NutriGuide — Enhanced Entity-Relationship (EER) Relational Schema", fill=(255, 255, 255), font=FONT_TITLE, anchor="mm")

    # Table layout coordinates (x, y, w, h)
    # Row 1: user_profile (left), nutrition_goal (mid-left), recommendation (mid-right), activity_log (right)
    # Row 2: meal (left), meal_item (mid), food (mid-right), food_category (right)
    # Row 3: meal_plan (left), meal_plan_item (mid)
    tables_data = [
        ("USER_PROFILE", 80, 120, 380, 310, (230, 240, 255), (50, 100, 190), [
            ("user_id", "INT AUTO_INCREMENT", "PK"),
            ("name", "VARCHAR(100)", "NOT NULL"),
            ("email", "VARCHAR(100)", "UNIQUE"),
            ("password_hash", "VARCHAR(255)", "NOT NULL"),
            ("age", "INT", "CHECK > 0"),
            ("gender", "VARCHAR(20)", "NOT NULL"),
            ("height_cm", "DECIMAL(5,2)", "CHECK > 0"),
            ("weight_kg", "DECIMAL(5,2)", "CHECK > 0"),
            ("activity_level", "VARCHAR(50)", "NOT NULL"),
            ("created_at", "TIMESTAMP", "DEFAULT NOW()")
        ]),
        ("NUTRITION_GOAL", 530, 120, 380, 260, (245, 235, 255), (140, 80, 190), [
            ("goal_id", "INT AUTO_INCREMENT", "PK"),
            ("user_id", "INT [FK -> user_profile]", "FK, CASCADE"),
            ("target_calories", "DECIMAL(7,2)", "CHECK >= 0"),
            ("target_protein", "DECIMAL(6,2)", "CHECK >= 0"),
            ("target_carbs", "DECIMAL(6,2)", "CHECK >= 0"),
            ("target_fats", "DECIMAL(6,2)", "CHECK >= 0"),
            ("is_active", "BOOLEAN", "DEFAULT TRUE"),
            ("created_at", "TIMESTAMP", "DEFAULT NOW()")
        ]),
        ("RECOMMENDATION", 980, 120, 380, 200, (255, 235, 245), (190, 70, 120), [
            ("rec_id", "INT AUTO_INCREMENT", "PK"),
            ("user_id", "INT [FK -> user_profile]", "FK, CASCADE"),
            ("rec_type", "VARCHAR(50)", "NOT NULL"),
            ("message", "VARCHAR(500)", "NOT NULL"),
            ("generated_at", "TIMESTAMP", "DEFAULT NOW()")
        ]),
        ("ACTIVITY_LOG (Audit)", 1430, 120, 380, 190, (255, 245, 230), (200, 110, 30), [
            ("log_id", "INT AUTO_INCREMENT", "PK"),
            ("user_id", "INT [FK -> user_profile]", "FK, CASCADE"),
            ("action_taken", "VARCHAR(255)", "Trigger-Logged"),
            ("log_time", "TIMESTAMP", "DEFAULT NOW()")
        ]),
        ("MEAL", 80, 520, 380, 200, (235, 245, 235), (60, 150, 80), [
            ("meal_id", "INT AUTO_INCREMENT", "PK"),
            ("user_id", "INT [FK -> user_profile]", "FK, CASCADE"),
            ("meal_type", "VARCHAR(50)", "NOT NULL"),
            ("meal_date", "DATE", "NOT NULL"),
            ("created_at", "TIMESTAMP", "DEFAULT NOW()")
        ]),
        ("MEAL_ITEM (Associative M:N)", 530, 520, 420, 200, (255, 250, 235), (200, 140, 40), [
            ("meal_item_id", "INT AUTO_INCREMENT", "PK"),
            ("meal_id", "INT [FK -> meal]", "FK, CASCADE"),
            ("food_id", "INT [FK -> food]", "FK, RESTRICT"),
            ("quantity", "DECIMAL(5,2)", "CHECK > 0")
        ]),
        ("FOOD", 1020, 520, 390, 280, (240, 250, 255), (40, 130, 180), [
            ("food_id", "INT AUTO_INCREMENT", "PK"),
            ("category_id", "INT [FK -> food_category]", "FK, RESTRICT"),
            ("food_name", "VARCHAR(100)", "UNIQUE"),
            ("serving_size", "DECIMAL(6,2)", "CHECK > 0"),
            ("calories", "DECIMAL(6,2)", "CHECK >= 0"),
            ("protein", "DECIMAL(5,2)", "CHECK >= 0"),
            ("carbohydrates", "DECIMAL(5,2)", "CHECK >= 0"),
            ("fat", "DECIMAL(5,2)", "CHECK >= 0")
        ]),
        ("FOOD_CATEGORY", 1480, 520, 360, 150, (245, 245, 255), (100, 100, 200), [
            ("category_id", "INT AUTO_INCREMENT", "PK"),
            ("category_name", "VARCHAR(100)", "UNIQUE")
        ]),
        ("MEAL_PLAN", 80, 850, 380, 220, (255, 240, 235), (190, 90, 70), [
            ("meal_plan_id", "INT AUTO_INCREMENT", "PK"),
            ("user_id", "INT [FK -> user_profile]", "FK, CASCADE"),
            ("plan_name", "VARCHAR(100)", "NOT NULL"),
            ("start_date", "DATE", "NOT NULL"),
            ("end_date", "DATE", "NOT NULL"),
            ("is_active", "BOOLEAN", "DEFAULT TRUE"),
            ("created_at", "TIMESTAMP", "DEFAULT NOW()")
        ]),
        ("MEAL_PLAN_ITEM (Associative M:N)", 530, 850, 440, 210, (255, 245, 240), (190, 110, 80), [
            ("meal_plan_item_id", "INT AUTO_INCREMENT", "PK"),
            ("meal_plan_id", "INT [FK -> meal_plan]", "FK, CASCADE"),
            ("food_id", "INT [FK -> food]", "FK, RESTRICT"),
            ("planned_meal_type", "VARCHAR(50)", "NOT NULL"),
            ("quantity", "DECIMAL(5,2)", "CHECK > 0")
        ])
    ]

    # Draw table cards
    for title, x, y, w, h, fill_col, border_col, cols in tables_data:
        draw.rounded_rectangle([(x, y), (x+w, y+h)], radius=8, fill=(255, 255, 255), outline=border_col, width=2)
        # Header banner
        draw.rounded_rectangle([(x, y), (x+w, y+36)], radius=8, fill=fill_col, outline=border_col, width=1)
        draw.rectangle([(x, y+24), (x+w, y+36)], fill=fill_col) # square bottom corners of header
        draw.line([(x, y+36), (x+w, y+36)], fill=border_col, width=2)
        draw.text((x+15, y+10), title, fill=(20, 35, 60), font=FONT_BOLD)

        # Columns
        for idx, (col_name, col_type, col_rule) in enumerate(cols):
            cy = y + 46 + idx * 22
            # Light zebra striping
            if idx % 2 == 1:
                draw.rectangle([(x+2, cy-2), (x+w-2, cy+18)], fill=(250, 252, 255))
            # Text
            col_font = FONT_SMALL_BOLD if "PK" in col_rule else FONT_SMALL
            col_color = (160, 30, 30) if "PK" in col_rule else (30, 70, 150) if "FK" in col_rule else (40, 40, 40)
            draw.text((x+15, cy), col_name, fill=col_color, font=col_font)
            draw.text((x+160, cy), col_type, fill=(100, 100, 100), font=FONT_SMALL)
            draw.text((x+w-15, cy), col_rule, fill=(120, 80, 20) if "CHECK" in col_rule or "UNIQUE" in col_rule else (80, 80, 80), font=FONT_SMALL, anchor="ra")

    # Connectors & Cardinalities
    # 1. user_profile -> nutrition_goal (1:N)
    draw.line([(460, 220), (530, 220)], fill=(70, 100, 150), width=2)
    draw.text((470, 200), "1", fill=(180, 20, 20), font=FONT_BOLD)
    draw.text((515, 200), "N", fill=(180, 20, 20), font=FONT_BOLD)

    # 2. user_profile -> recommendation (1:N)
    draw.line([(460, 170), (980, 170)], fill=(70, 100, 150), width=2)
    draw.text((470, 150), "1", fill=(180, 20, 20), font=FONT_BOLD)
    draw.text((960, 150), "N", fill=(180, 20, 20), font=FONT_BOLD)

    # 3. user_profile -> activity_log (1:N)
    draw.line([(460, 140), (1430, 140)], fill=(70, 100, 150), width=2)
    draw.text((470, 120), "1", fill=(180, 20, 20), font=FONT_BOLD)
    draw.text((1410, 120), "N", fill=(180, 20, 20), font=FONT_BOLD)

    # 4. user_profile -> meal (1:N)
    draw.line([(270, 430), (270, 520)], fill=(70, 100, 150), width=2)
    draw.text((280, 440), "1", fill=(180, 20, 20), font=FONT_BOLD)
    draw.text((280, 500), "N", fill=(180, 20, 20), font=FONT_BOLD)

    # 5. meal -> meal_item (1:N CASCADE)
    draw.line([(460, 600), (530, 600)], fill=(50, 140, 70), width=2)
    draw.text((470, 580), "1", fill=(180, 20, 20), font=FONT_BOLD)
    draw.text((515, 580), "N", fill=(180, 20, 20), font=FONT_BOLD)

    # 6. food -> meal_item (1:N RESTRICT)
    draw.line([(1020, 600), (950, 600)], fill=(40, 120, 170), width=2)
    draw.text((1005, 580), "1", fill=(180, 20, 20), font=FONT_BOLD)
    draw.text((960, 580), "N", fill=(180, 20, 20), font=FONT_BOLD)

    # 7. food_category -> food (1:N RESTRICT)
    draw.line([(1480, 580), (1410, 580)], fill=(80, 90, 180), width=2)
    draw.text((1465, 560), "1", fill=(180, 20, 20), font=FONT_BOLD)
    draw.text((1420, 560), "N", fill=(180, 20, 20), font=FONT_BOLD)

    # 8. user_profile -> meal_plan (1:N)
    draw.line([(120, 430), (120, 850)], fill=(70, 100, 150), width=2)
    draw.text((130, 440), "1", fill=(180, 20, 20), font=FONT_BOLD)
    draw.text((130, 830), "N", fill=(180, 20, 20), font=FONT_BOLD)

    # 9. meal_plan -> meal_plan_item (1:N CASCADE)
    draw.line([(460, 920), (530, 920)], fill=(180, 80, 60), width=2)
    draw.text((470, 900), "1", fill=(180, 20, 20), font=FONT_BOLD)
    draw.text((515, 900), "N", fill=(180, 20, 20), font=FONT_BOLD)

    # 10. food -> meal_plan_item (1:N RESTRICT)
    draw.line([(1200, 800), (1200, 920), (970, 920)], fill=(40, 120, 170), width=2)
    draw.text((1210, 810), "1", fill=(180, 20, 20), font=FONT_BOLD)
    draw.text((980, 900), "N", fill=(180, 20, 20), font=FONT_BOLD)

    # 11. Trigger flow (AFTER INSERT ON meal -> activity_log)
    # Dashed arrow
    trigger_pts = [(460, 550), (1700, 550), (1700, 310)]
    for i in range(len(trigger_pts)-1):
        p1 = trigger_pts[i]
        p2 = trigger_pts[i+1]
        # dashed line
        dx = p2[0] - p1[0]
        dy = p2[1] - p1[1]
        dist = int((dx**2 + dy**2)**0.5)
        for s in range(0, dist, 12):
            e = min(s + 6, dist)
            x1 = p1[0] + dx * s / dist
            y1 = p1[1] + dy * s / dist
            x2 = p1[0] + dx * e / dist
            y2 = p1[1] + dy * e / dist
            draw.line([(x1, y1), (x2, y2)], fill=(220, 100, 30), width=2)
    draw.polygon([(1695, 315), (1705, 315), (1700, 310)], fill=(220, 100, 30))
    draw.text((1100, 535), "Trigger: trg_after_meal_insert (AFTER INSERT ON meal => logs event)", fill=(190, 80, 20), font=FONT_SMALL_BOLD)

    # Summary box at bottom
    draw.rounded_rectangle([(80, 1100), (W-80, 1540)], radius=10, fill=(245, 248, 252), outline=(180, 200, 225), width=1)
    draw.text((110, 1120), "Key Architectural & Integrity Highlights of the NutriGuide EER Model:", fill=(20, 50, 100), font=FONT_BOLD)
    notes = [
        "1. Strict 3NF Normalization: Eliminates data redundancy, update anomalies, and repeating groups across all 10 domain entities.",
        "2. Associative Junction Tables: Resolves M:N cardinality between Meal and Food via meal_item, and Meal_Plan and Food via meal_plan_item.",
        "3. Referential Actions: Enforces ON DELETE CASCADE on user-owned dependent entities (goals, meals, plans), while enforcing ON DELETE RESTRICT on food catalog items to preserve historical meal audit logs.",
        "4. Declarative Check Constraints: Guards physiological boundaries (height > 0, weight > 0, age > 0) and dietary quantities (serving_size > 0, portion quantity > 0, target calories >= 0).",
        "5. Automated Audit Trail: MySQL trigger trg_after_meal_insert captures every logged meal transaction into activity_log without requiring application-side plumbing."
    ]
    for idx, note in enumerate(notes):
        draw.text((120, 1160 + idx*32), note, fill=(50, 65, 80), font=FONT_REGULAR)

    # Footer note
    draw.text((W//2, H-25), "Figure 6.2: NutriGuide Enhanced Entity-Relationship (EER) Relational Schema with Constraints and Foreign Keys", fill=(90, 105, 120), font=FONT_SMALL_BOLD, anchor="mm")

    path = os.path.join(OUTPUT_DIR, "eer_diagram.png")
    im.save(path, quality=95)
    print("Saved:", path)

draw_er_diagram()
draw_eer_diagram()
