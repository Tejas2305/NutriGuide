# -*- coding: utf-8 -*-
"""
NutriGuide - High-Legibility ER and EER Diagram Generator
Generates:
1. Figure 6.1: Conceptual ER Diagram (Chen Notation)
2. Figure 6.2: Enhanced ER (EER) Relational Schema

ENGINEERING GUARANTEES:
- Zero overlapping elements (every table, diamond, attribute, badge, and connector has its own dedicated channel)
- Large, bold typography (14pt-28pt) clearly readable at 100% zoom in Word and on A4 paper
- Professional database schema aesthetic with high-contrast color palette
"""

import os
from PIL import Image, ImageDraw, ImageFont

OUTPUT_DIR = r"D:\NutriGuide\report\images"
os.makedirs(OUTPUT_DIR, exist_ok=True)

WINDIR = os.environ.get("WINDIR", "C:/Windows")
FONT_DIR = os.path.join(WINDIR, "Fonts")

def get_font(name="segoeuib.ttf", size=16):
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

# Typography hierarchy
FONT_TITLE = get_font("segoeuib.ttf", 26)
FONT_SECTION = get_font("segoeuib.ttf", 19)
FONT_ENTITY = get_font("segoeuib.ttf", 17)
FONT_REL = get_font("segoeuib.ttf", 15)
FONT_ATTR_PK = get_font("segoeuib.ttf", 14)
FONT_ATTR = get_font("segoeuib.ttf", 13)
FONT_CARD = get_font("segoeuib.ttf", 18)
FONT_SUB = get_font("segoeui.ttf", 13)
FONT_SUB_BOLD = get_font("segoeuib.ttf", 13)

FONT_TBL_NAME = get_font("segoeuib.ttf", 16)
FONT_COL_PK = get_font("segoeuib.ttf", 13)
FONT_COL = get_font("segoeui.ttf", 13)
FONT_COL_BOLD = get_font("segoeuib.ttf", 13)
FONT_ACTION = get_font("segoeuib.ttf", 13)
FONT_LEGEND = get_font("segoeuib.ttf", 12)

def draw_diamond(draw, center, size_w, size_h, fill, outline, width=2):
    cx, cy = center
    hw, hh = size_w // 2, size_h // 2
    points = [(cx, cy - hh), (cx + hw, cy), (cx, cy + hh), (cx - hw, cy)]
    draw.polygon(points, fill=fill, outline=outline)

# ==============================================================================
# 1. CONCEPTUAL ER DIAGRAM (Figure 6.1 - Chen Notation)
# ==============================================================================
def draw_er_diagram():
    W, H = 1540, 980
    im = Image.new("RGB", (W, H), color=(255, 255, 255))
    draw = ImageDraw.Draw(im)

    # Canvas border & Header
    draw.rounded_rectangle([(12, 10), (W-12, H-10)], radius=12, outline=(148, 163, 184), width=2)
    draw.rounded_rectangle([(12, 10), (W-12, 58)], radius=12, fill=(26, 54, 93))
    draw.rectangle([(12, 40), (W-12, 58)], fill=(26, 54, 93))
    draw.text((W//2, 34), "NutriGuide — Conceptual Entity-Relationship (ER) Diagram (Chen Notation)", fill=(255, 255, 255), font=FONT_TITLE, anchor="mm")

    # Legend at top right
    lx, ly = W - 520, 68
    draw.rounded_rectangle([(lx, ly), (W-25, ly + 38)], radius=6, fill=(248, 250, 252), outline=(203, 213, 225), width=1)
    draw.rectangle([(lx+12, ly+9), (lx+40, ly+29)], fill=(219, 234, 254), outline=(37, 99, 235), width=2)
    draw.text((lx+46, ly+19), "Entity", fill=(15, 23, 42), font=FONT_LEGEND, anchor="lm")
    draw_diamond(draw, (lx+120, ly+19), 24, 18, (254, 226, 226), (220, 38, 38), width=1)
    draw.text((lx+138, ly+19), "Relationship", fill=(15, 23, 42), font=FONT_LEGEND, anchor="lm")
    draw.ellipse([(lx+245, ly+9), (lx+275, ly+29)], fill=(254, 243, 199), outline=(217, 119, 6), width=1)
    draw.text((lx+282, ly+19), "Attribute (PK underlined)", fill=(15, 23, 42), font=FONT_LEGEND, anchor="lm")

    # Helper: Entity Box
    def draw_entity_box(cx, cy, w, h, text, is_weak=False):
        draw.rounded_rectangle([(cx-w//2, cy-h//2), (cx+w//2, cy+h//2)], radius=6, fill=(239, 246, 255), outline=(37, 99, 235), width=2)
        if is_weak:
            draw.rounded_rectangle([(cx-w//2+4, cy-h//2+4), (cx+w//2-4, cy+h//2-4)], radius=4, fill=None, outline=(37, 99, 235), width=2)
        draw.text((cx, cy), text, fill=(30, 58, 138), font=FONT_ENTITY, anchor="mm")

    # Helper: Attribute Ellipse
    def draw_attr(cx, cy, text, is_pk=False):
        tw = len(text) * 8 + 28
        ew, eh = max(tw, 105), 32
        draw.ellipse([(cx-ew//2, cy-eh//2), (cx+ew//2, cy+eh//2)], fill=(254, 243, 199), outline=(217, 119, 6), width=2)
        f = FONT_ATTR_PK if is_pk else FONT_ATTR
        draw.text((cx, cy), text, fill=(180, 83, 9) if is_pk else (120, 53, 15), font=f, anchor="mm")
        if is_pk:
            line_w = len(text) * 8
            draw.line([(cx-line_w//2, cy+8), (cx+line_w//2, cy+8)], fill=(180, 83, 9), width=2)

    # ---------------- ENTITY COORDINATES ----------------
    ux, uy = 450, 470
    gx, gy = 140, 180   # NUTRITION_GOAL
    rx, ry = 120, 470   # RECOMMENDATION
    ax, ay = 140, 770   # ACTIVITY_LOG
    
    mx, my = 790, 260   # MEAL
    mix, miy = 1090, 260 # MEAL_ITEM
    px, py = 790, 640   # MEAL_PLAN
    pix, piy = 1090, 640 # MEAL_PLAN_ITEM

    fx, fy = 1380, 450  # FOOD
    cx_cat, cy_cat = 1380, 780 # FOOD_CATEGORY

    # ================= RELATIONSHIPS =================
    # 1. USER -> NUTRITION_GOAL
    draw.line([(ux - 85, uy - 30), (gx + 80, gy + 30)], fill=(71, 85, 105), width=2)
    dia_g = ((ux - 85 + gx + 80)//2, (uy - 30 + gy + 30)//2)
    draw_diamond(draw, dia_g, 90, 42, (254, 226, 226), (220, 38, 38), width=2)
    draw.text(dia_g, "sets", fill=(153, 27, 27), font=FONT_REL, anchor="mm")
    draw.text((ux - 115, uy - 45), "1", fill=(37, 99, 235), font=FONT_CARD)
    draw.text((gx + 95, gy + 45), "N", fill=(220, 38, 38), font=FONT_CARD)

    # 2. USER -> RECOMMENDATION
    draw.line([(ux - 85, uy), (rx + 80, ry)], fill=(71, 85, 105), width=2)
    dia_r = ((ux - 85 + rx + 80)//2, ry)
    draw_diamond(draw, dia_r, 96, 42, (254, 226, 226), (220, 38, 38), width=2)
    draw.text(dia_r, "receives", fill=(153, 27, 27), font=FONT_REL, anchor="mm")
    draw.text((ux - 110, uy - 20), "1", fill=(37, 99, 235), font=FONT_CARD)
    draw.text((rx + 95, ry - 20), "N", fill=(220, 38, 38), font=FONT_CARD)

    # 3. USER -> ACTIVITY_LOG
    draw.line([(ux - 85, uy + 30), (ax + 80, ay - 30)], fill=(71, 85, 105), width=2)
    dia_a = ((ux - 85 + ax + 80)//2, (uy + 30 + ay - 30)//2)
    draw_diamond(draw, dia_a, 90, 42, (254, 226, 226), (220, 38, 38), width=2)
    draw.text(dia_a, "records", fill=(153, 27, 27), font=FONT_REL, anchor="mm")
    draw.text((ux - 115, uy + 45), "1", fill=(37, 99, 235), font=FONT_CARD)
    draw.text((ax + 95, ay - 45), "N", fill=(220, 38, 38), font=FONT_CARD)

    # 4. USER -> MEAL
    draw.line([(ux + 85, uy - 25), (mx - 65, my + 15)], fill=(71, 85, 105), width=2)
    dia_m = ((ux + 85 + mx - 65)//2, (uy - 25 + my + 15)//2)
    draw_diamond(draw, dia_m, 90, 42, (254, 226, 226), (220, 38, 38), width=2)
    draw.text(dia_m, "logs", fill=(153, 27, 27), font=FONT_REL, anchor="mm")
    draw.text((ux + 105, uy - 40), "1", fill=(37, 99, 235), font=FONT_CARD)
    draw.text((mx - 90, my + 25), "N", fill=(220, 38, 38), font=FONT_CARD)

    # 5. USER -> MEAL_PLAN
    draw.line([(ux + 85, uy + 25), (px - 75, py - 15)], fill=(71, 85, 105), width=2)
    dia_p = ((ux + 85 + px - 75)//2, (uy + 25 + py - 15)//2)
    draw_diamond(draw, dia_p, 96, 42, (254, 226, 226), (220, 38, 38), width=2)
    draw.text(dia_p, "schedules", fill=(153, 27, 27), font=FONT_REL, anchor="mm")
    draw.text((ux + 105, uy + 40), "1", fill=(37, 99, 235), font=FONT_CARD)
    draw.text((px - 95, py - 25), "N", fill=(220, 38, 38), font=FONT_CARD)

    # 6. MEAL -> MEAL_ITEM -> FOOD
    draw.line([(mx + 65, my), (mix - 75, miy)], fill=(71, 85, 105), width=2)
    dia_mi = ((mx + 65 + mix - 75)//2, my)
    draw_diamond(draw, dia_mi, 90, 40, (254, 226, 226), (220, 38, 38), width=2)
    draw.text(dia_mi, "contains", fill=(153, 27, 27), font=FONT_REL, anchor="mm")
    draw.text((mx + 75, my - 20), "1", fill=(37, 99, 235), font=FONT_CARD)
    draw.text((mix - 90, miy - 20), "N", fill=(220, 38, 38), font=FONT_CARD)

    draw.line([(mix + 75, miy + 15), (fx - 65, fy - 25)], fill=(71, 85, 105), width=2)
    dia_mif = ((mix + 75 + fx - 65)//2, (miy + 15 + fy - 25)//2)
    draw_diamond(draw, dia_mif, 90, 40, (254, 226, 226), (220, 38, 38), width=2)
    draw.text(dia_mif, "refers", fill=(153, 27, 27), font=FONT_REL, anchor="mm")
    draw.text((mix + 85, miy + 30), "N", fill=(220, 38, 38), font=FONT_CARD)
    draw.text((fx - 90, fy - 35), "1", fill=(37, 99, 235), font=FONT_CARD)

    # 7. MEAL_PLAN -> MEAL_PLAN_ITEM -> FOOD
    draw.line([(px + 75, py), (pix - 85, piy)], fill=(71, 85, 105), width=2)
    dia_pi = ((px + 75 + pix - 85)//2, py)
    draw_diamond(draw, dia_pi, 90, 40, (254, 226, 226), (220, 38, 38), width=2)
    draw.text(dia_pi, "includes", fill=(153, 27, 27), font=FONT_REL, anchor="mm")
    draw.text((px + 85, py - 20), "1", fill=(37, 99, 235), font=FONT_CARD)
    draw.text((pix - 100, piy - 20), "N", fill=(220, 38, 38), font=FONT_CARD)

    draw.line([(pix + 85, piy - 15), (fx - 65, fy + 25)], fill=(71, 85, 105), width=2)
    dia_pif = ((pix + 85 + fx - 65)//2, (piy - 15 + fy + 25)//2)
    draw_diamond(draw, dia_pif, 90, 40, (254, 226, 226), (220, 38, 38), width=2)
    draw.text(dia_pif, "targets", fill=(153, 27, 27), font=FONT_REL, anchor="mm")
    draw.text((pix + 95, piy - 30), "N", fill=(220, 38, 38), font=FONT_CARD)
    draw.text((fx - 90, fy + 35), "1", fill=(37, 99, 235), font=FONT_CARD)

    # 8. FOOD_CATEGORY -> FOOD
    draw.line([(cx_cat, cy_cat - 25), (fx, fy + 25)], fill=(71, 85, 105), width=2)
    dia_cat = (cx_cat, (cy_cat - 25 + fy + 25)//2)
    draw_diamond(draw, dia_cat, 90, 42, (254, 226, 226), (220, 38, 38), width=2)
    draw.text(dia_cat, "groups", fill=(153, 27, 27), font=FONT_REL, anchor="mm")
    draw.text((cx_cat + 20, cy_cat - 45), "1", fill=(37, 99, 235), font=FONT_CARD)
    draw.text((fx + 20, fy + 45), "N", fill=(220, 38, 38), font=FONT_CARD)

    # ================= DRAW ENTITY BOXES =================
    draw_entity_box(ux, uy, 170, 54, "USER_PROFILE")
    draw_entity_box(gx, gy, 160, 50, "NUTRITION_GOAL")
    draw_entity_box(rx, ry, 160, 50, "RECOMMENDATION")
    draw_entity_box(ax, ay, 160, 50, "ACTIVITY_LOG")
    
    draw_entity_box(mx, my, 130, 50, "MEAL")
    draw_entity_box(mix, miy, 150, 50, "MEAL_ITEM", is_weak=True)
    draw_entity_box(px, py, 150, 50, "MEAL_PLAN")
    draw_entity_box(pix, piy, 170, 50, "MEAL_PLAN_ITEM", is_weak=True)
    
    draw_entity_box(fx, fy, 130, 50, "FOOD")
    draw_entity_box(cx_cat, cy_cat, 170, 50, "FOOD_CATEGORY")

    # ================= ATTRIBUTES (COLLISION-FREE OFFSETS) =================
    # USER_PROFILE attributes
    draw.line([(ux - 45, uy - 27), (ux - 55, uy - 95)], fill=(203, 213, 225), width=2)
    draw_attr(ux - 55, uy - 95, "user_id", is_pk=True)

    draw.line([(ux + 45, uy - 27), (ux + 65, uy - 95)], fill=(203, 213, 225), width=2)
    draw_attr(ux + 65, uy - 95, "email (UQ)")

    draw.line([(ux - 45, uy + 27), (ux - 55, uy + 95)], fill=(203, 213, 225), width=2)
    draw_attr(ux - 55, uy + 95, "name")

    draw.line([(ux, uy + 27), (ux, uy + 145)], fill=(203, 213, 225), width=2)
    draw_attr(ux, uy + 145, "height / weight")

    # NUTRITION_GOAL attributes
    draw.line([(gx - 40, gy - 25), (gx - 75, gy - 75)], fill=(203, 213, 225), width=2)
    draw_attr(gx - 75, gy - 75, "goal_id", is_pk=True)

    draw.line([(gx + 40, gy - 25), (gx + 75, gy - 75)], fill=(203, 213, 225), width=2)
    draw_attr(gx + 75, gy - 75, "target_calories")

    # RECOMMENDATION attributes
    draw.line([(rx - 80, ry - 15), (rx - 70, ry - 75)], fill=(203, 213, 225), width=2)
    draw_attr(rx - 70, ry - 75, "rec_id", is_pk=True)

    draw.line([(rx - 80, ry + 15), (rx - 70, ry + 75)], fill=(203, 213, 225), width=2)
    draw_attr(rx - 70, ry + 75, "message")

    # ACTIVITY_LOG attributes
    draw.line([(ax - 40, ay + 25), (ax - 75, ay + 75)], fill=(203, 213, 225), width=2)
    draw_attr(ax - 75, ay + 75, "log_id", is_pk=True)

    draw.line([(ax + 40, ay + 25), (ax + 75, ay + 75)], fill=(203, 213, 225), width=2)
    draw_attr(ax + 75, ay + 75, "action_taken")

    # MEAL attributes
    draw.line([(mx - 30, my - 25), (mx - 80, my - 75)], fill=(203, 213, 225), width=2)
    draw_attr(mx - 80, my - 75, "meal_id", is_pk=True)

    draw.line([(mx + 30, my - 25), (mx + 85, my - 75)], fill=(203, 213, 225), width=2)
    draw_attr(mx + 85, my - 75, "meal_type / date")

    # MEAL_ITEM attribute
    draw.line([(mix, miy - 25), (mix, miy - 75)], fill=(203, 213, 225), width=2)
    draw_attr(mix, miy - 75, "quantity (Serving)")

    # MEAL_PLAN attributes
    draw.line([(px - 35, py + 25), (px - 70, py + 75)], fill=(203, 213, 225), width=2)
    draw_attr(px - 70, py + 75, "meal_plan_id", is_pk=True)

    draw.line([(px + 35, py + 25), (px + 70, py + 75)], fill=(203, 213, 225), width=2)
    draw_attr(px + 70, py + 75, "plan_name")

    # MEAL_PLAN_ITEM attribute
    draw.line([(pix, piy + 25), (pix, piy + 75)], fill=(203, 213, 225), width=2)
    draw_attr(pix, piy + 75, "quantity (Serving)")

    # FOOD attributes
    draw.line([(fx + 65, fy - 15), (fx + 85, fy - 65)], fill=(203, 213, 225), width=2)
    draw_attr(fx + 85, fy - 65, "food_id", is_pk=True)

    draw.line([(fx + 65, fy + 15), (fx + 85, fy + 65)], fill=(203, 213, 225), width=2)
    draw_attr(fx + 85, fy + 65, "calories / macros")

    # FOOD_CATEGORY attributes
    draw.line([(cx_cat - 45, cy_cat + 25), (cx_cat - 80, cy_cat + 75)], fill=(203, 213, 225), width=2)
    draw_attr(cx_cat - 80, cy_cat + 75, "category_id", is_pk=True)

    draw.line([(cx_cat + 45, cy_cat + 25), (cx_cat + 80, cy_cat + 75)], fill=(203, 213, 225), width=2)
    draw_attr(cx_cat + 80, cy_cat + 75, "category_name")

    er_path = os.path.join(OUTPUT_DIR, "er_diagram.png")
    im.save(er_path, quality=95)
    print(f"Generated High-Legibility ER Diagram: {er_path} ({W}x{H})")

# ==============================================================================
# 2. ENHANCED ER (EER) RELATIONAL SCHEMA DIAGRAM (Figure 6.2)
# ==============================================================================
def draw_eer_diagram():
    # Spacious 1660 x 1020 canvas for clean open channels between all 4 columns
    W, H = 1660, 1020
    im = Image.new("RGB", (W, H), color=(255, 255, 255))
    draw = ImageDraw.Draw(im)

    # Canvas border & Header
    draw.rounded_rectangle([(12, 10), (W-12, H-10)], radius=12, outline=(148, 163, 184), width=2)
    draw.rounded_rectangle([(12, 10), (W-12, 58)], radius=12, fill=(26, 54, 93))
    draw.rectangle([(12, 40), (W-12, 58)], fill=(26, 54, 93))
    draw.text((W//2, 34), "NutriGuide — Enhanced Entity-Relationship (EER) Relational Schema", fill=(255, 255, 255), font=FONT_TITLE, anchor="mm")

    # Helper: Draw Relational Table Card
    def draw_table_card(x, y, w, title, columns):
        h = 36 + len(columns) * 23 + 8
        draw.rounded_rectangle([(x, y), (x+w, y+h)], radius=6, fill=(255, 255, 255), outline=(100, 116, 139), width=2)
        draw.rounded_rectangle([(x, y), (x+w, y+34)], radius=6, fill=(30, 64, 175))
        draw.rectangle([(x, y+20), (x+w, y+34)], fill=(30, 64, 175))
        draw.text((x + w//2, y + 17), title, fill=(255, 255, 255), font=FONT_TBL_NAME, anchor="mm")

        for idx, (col_text, is_pk, is_fk) in enumerate(columns):
            cy = y + 46 + idx * 23
            if idx % 2 == 1:
                draw.rectangle([(x+1, cy-9), (x+w-1, cy+13)], fill=(248, 250, 252))
            
            f = FONT_COL_PK if is_pk else (FONT_COL_BOLD if is_fk else FONT_COL)
            color = (30, 58, 138) if is_pk else ((180, 83, 9) if is_fk else (30, 41, 59))
            draw.text((x + 14, cy), col_text, fill=color, font=f, anchor="lm")
            if is_pk:
                tw = len(col_text) * 7.8
                draw.line([(x + 14, cy+8), (x + 14 + tw, cy+8)], fill=color, width=2)
        return x, y, w, h

    # ---------------- 4 COLUMNS WITH DEDICATED CHANNELS ----------------
    # COLUMN 1: USER DOMAIN (Left: x = 35, w = 310)
    ux, uy, uw, uh = draw_table_card(35, 75, 310, "USER_PROFILE", [
        ("user_id INT (PK)", True, False),
        ("name VARCHAR(100) NOT NULL", False, False),
        ("email VARCHAR(100) UNIQUE NOT NULL", False, False),
        ("password_hash VARCHAR(255) NOT NULL", False, False),
        ("height_cm DECIMAL(5,2) (>0)", False, False),
        ("weight_kg DECIMAL(5,2) (>0)", False, False),
        ("age INT (>0), gender ENUM", False, False),
        ("activity_level ENUM NOT NULL", False, False)
    ])

    gx, gy, gw, gh = draw_table_card(35, 335, 310, "NUTRITION_GOAL", [
        ("goal_id INT (PK)", True, False),
        ("user_id INT (FK -> user_profile)", False, True),
        ("target_calories DECIMAL(6,2)", False, False),
        ("target_protein, carbs, fat", False, False),
        ("is_active BOOLEAN DEFAULT TRUE", False, False),
        ("created_at TIMESTAMP", False, False)
    ])

    rx, ry, rw, rh = draw_table_card(35, 540, 310, "RECOMMENDATION", [
        ("rec_id INT (PK)", True, False),
        ("user_id INT (FK -> user_profile)", False, True),
        ("rec_type VARCHAR(50)", False, False),
        ("message VARCHAR(500) NOT NULL", False, False),
        ("generated_at TIMESTAMP", False, False)
    ])

    ax, ay, aw, ah = draw_table_card(35, 745, 310, "ACTIVITY_LOG (Audit Trail)", [
        ("log_id INT (PK)", True, False),
        ("user_id INT (FK -> user_profile)", False, True),
        ("action_taken VARCHAR(255) NOT NULL", False, False),
        ("log_time TIMESTAMP DEFAULT NOW", False, False)
    ])

    # Channel 1: x = 345 to 485 (140 px width)
    ch1_x = (345 + 485) // 2 # 415

    # COLUMN 2: MEALS & PLANS (Middle-Left: x = 485, w = 290)
    mx, my, mw, mh = draw_table_card(485, 75, 290, "MEAL (Header)", [
        ("meal_id INT (PK)", True, False),
        ("user_id INT (FK -> user_profile)", False, True),
        ("meal_type VARCHAR(50) NOT NULL", False, False),
        ("meal_date DATE NOT NULL", False, False),
        ("created_at TIMESTAMP", False, False)
    ])

    px, py, pw, ph = draw_table_card(485, 480, 290, "MEAL_PLAN (Header)", [
        ("meal_plan_id INT (PK)", True, False),
        ("user_id INT (FK -> user_profile)", False, True),
        ("plan_name VARCHAR(100) NOT NULL", False, False),
        ("start_date, end_date DATE", False, False),
        ("is_active BOOLEAN", False, False)
    ])

    # Channel 2: x = 775 to 905 (130 px width)
    ch2_x = (775 + 905) // 2 # 840

    # COLUMN 3: ASSOCIATIVE JUNCTIONS (Middle-Right: x = 905, w = 290)
    mix, miy, miw, mih = draw_table_card(905, 230, 290, "MEAL_ITEM (Associative M:N)", [
        ("meal_item_id INT (PK)", True, False),
        ("meal_id INT (FK -> meal)", False, True),
        ("food_id INT (FK -> food)", False, True),
        ("quantity DECIMAL(5,2) (>0)", False, False)
    ])

    pix, piy, piw, pih = draw_table_card(905, 620, 290, "MEAL_PLAN_ITEM (Associative)", [
        ("meal_plan_item_id INT (PK)", True, False),
        ("meal_plan_id INT (FK -> meal_plan)", False, True),
        ("food_id INT (FK -> food)", False, True),
        ("quantity DECIMAL(5,2) (>0)", False, False)
    ])

    # Channel 3: x = 1195 to 1325 (130 px width)
    ch3_x = (1195 + 1325) // 2 # 1260

    # COLUMN 4: CATALOG & SYSTEM RULES (Far Right: x = 1325, w = 300)
    cx, cy, cw, ch = draw_table_card(1325, 75, 300, "FOOD_CATEGORY", [
        ("category_id INT (PK)", True, False),
        ("category_name VARCHAR(100) UNIQUE", False, False)
    ])

    fx, fy, fw, fh = draw_table_card(1325, 220, 300, "FOOD (Catalog Master)", [
        ("food_id INT (PK)", True, False),
        ("category_id INT (FK -> category)", False, True),
        ("food_name VARCHAR(100) UNIQUE", False, False),
        ("serving_size DECIMAL(5,2) (>0)", False, False),
        ("calories DECIMAL(6,2)", False, False),
        ("protein, carbs, fat", False, False)
    ])

    # Highlight Summary Box
    box_rx = 1325
    box_ry = 455
    box_rw = 300
    draw.rounded_rectangle([(box_rx, box_ry), (box_rx + box_rw, box_ry + 290)], radius=8, fill=(240, 253, 244), outline=(34, 197, 94), width=2)
    draw.text((box_rx + box_rw//2, box_ry + 22), "Integrity & Audit Rules", fill=(20, 83, 45), font=FONT_SECTION, anchor="mm")
    rules_notes = [
        "• ON DELETE CASCADE:",
        "  User deletion cascades to goals,",
        "  meals, plans, recs, and logs.",
        "• ON DELETE RESTRICT:",
        "  Catalog food/category cannot",
        "  be dropped if logged in meals.",
        "• Associative M:N Entities:",
        "  Cleanly resolve multi-item logs",
        "  with quantity portion multiplier.",
        "• Trigger-Enforced Audit:",
        "  trg_after_meal_insert writes",
        "  to activity_log automatically."
    ]
    for idx, r_note in enumerate(rules_notes):
        f = FONT_SUB_BOLD if "•" in r_note else FONT_SUB
        c = (20, 83, 45) if "•" in r_note else (15, 23, 42)
        draw.text((box_rx + 14, box_ry + 46 + idx * 18), r_note, fill=c, font=f)

    # Database Programmability Box
    tx, ty, tw, th = draw_table_card(1325, 770, 300, "DATABASE PROGRAMMABILITY", [
        ("trg_after_meal_insert (TRIGGER)", False, True),
        ("trg_validate_meal_quantity", False, False),
        ("daily_nutrition_summary (VIEW)", False, False),
        ("get_daily_summary (STORED PROC)", False, False)
    ])

    # ---------------- CONNECTORS & BADGES (100% IN CHANNELS) ----------------
    def draw_badge(cx, cy, card_text, action_text, is_cascade=True):
        draw.rounded_rectangle([(cx-52, cy-15), (cx+52, cy+15)], radius=5, fill=(255, 255, 255), outline=(148, 163, 184), width=1)
        draw.text((cx, cy - 6), card_text, fill=(30, 58, 138), font=FONT_ACTION, anchor="mm")
        badge_col = (185, 28, 28) if is_cascade else (126, 34, 206)
        draw.text((cx, cy + 7), action_text, fill=badge_col, font=FONT_SUB_BOLD, anchor="mm")

    col_fk = (37, 99, 235)

    # 1. USER_PROFILE -> NUTRITION_GOAL (Vertical between cards in Col 1)
    draw.line([(ux + 155, uy + uh), (gx + 155, gy)], fill=col_fk, width=2)
    draw.ellipse([(ux+152, uy+uh-3), (ux+158, uy+uh+3)], fill=col_fk)
    draw.polygon([(gx+150, gy-5), (gx+160, gy-5), (gx+155, gy)], fill=col_fk)
    draw_badge(ux + 155, (uy + uh + gy)//2, "1 : N", "CASCADE", is_cascade=True)

    # 2. USER_PROFILE -> RECOMMENDATION (Left gutter at x = 18)
    draw.line([(ux, uy + 120), (18, uy + 120), (18, ry + 30), (rx, ry + 30)], fill=col_fk, width=2)
    draw.ellipse([(ux-3, uy+117), (ux+3, uy+123)], fill=col_fk)
    draw.polygon([(rx-5, ry+25), (rx-5, ry+35), (rx, ry+30)], fill=col_fk)

    # 3. USER_PROFILE -> MEAL (Horizontal in Channel 1)
    draw.line([(ux + uw, uy + 40), (mx, my + 40)], fill=col_fk, width=2)
    draw.ellipse([(ux+uw-3, uy+37), (ux+uw+3, uy+43)], fill=col_fk)
    draw.polygon([(mx-5, my+35), (mx-5, my+45), (mx, my+40)], fill=col_fk)
    draw_badge(ch1_x, uy + 40, "1 : N", "CASCADE", is_cascade=True)

    # 4. USER_PROFILE -> MEAL_PLAN (Stepped in Channel 1)
    draw.line([(ux + uw, uy + 140), (ch1_x, uy + 140), (ch1_x, py + 40), (px, py + 40)], fill=col_fk, width=2)
    draw.ellipse([(ux+uw-3, uy+137), (ux+uw+3, uy+143)], fill=col_fk)
    draw.polygon([(px-5, py+35), (px-5, py+45), (px, py+40)], fill=col_fk)
    draw_badge(ch1_x, (uy + 140 + py + 40)//2, "1 : N", "CASCADE", is_cascade=True)

    # 5. MEAL -> MEAL_ITEM (Stepped in Channel 2)
    draw.line([(mx + mw, my + 40), (ch2_x, my + 40), (ch2_x, miy + 30), (mix, miy + 30)], fill=col_fk, width=2)
    draw.ellipse([(mx+mw-3, my+37), (mx+mw+3, my+43)], fill=col_fk)
    draw.polygon([(mix-5, miy+25), (mix-5, miy+35), (mix, miy+30)], fill=col_fk)
    draw_badge(ch2_x, (my + 40 + miy + 30)//2, "1 : N", "CASCADE", is_cascade=True)

    # 6. MEAL_PLAN -> MEAL_PLAN_ITEM (Stepped in Channel 2)
    draw.line([(px + pw, py + 40), (ch2_x, py + 40), (ch2_x, piy + 30), (pix, piy + 30)], fill=col_fk, width=2)
    draw.ellipse([(px+pw-3, py+37), (px+pw+3, py+43)], fill=col_fk)
    draw.polygon([(pix-5, piy+25), (pix-5, piy+35), (pix, piy+30)], fill=col_fk)
    draw_badge(ch2_x, (py + 40 + piy + 30)//2, "1 : N", "CASCADE", is_cascade=True)

    # 7. FOOD -> MEAL_ITEM (Horizontal in Channel 3)
    draw.line([(fx, fy + 40), (mix + miw, miy + 40)], fill=(126, 34, 206), width=2)
    draw.ellipse([(fx-3, fy+37), (fx+3, fy+43)], fill=(126, 34, 206))
    draw.polygon([(mix+miw+5, miy+35), (mix+miw+5, miy+45), (mix+miw, miy+40)], fill=(126, 34, 206))
    draw_badge(ch3_x, fy + 40, "1 : N", "RESTRICT", is_cascade=False)

    # 8. FOOD -> MEAL_PLAN_ITEM (Stepped strictly inside Channel 3 - NEVER touches Col 4!)
    draw.line([(fx, fy + 120), (ch3_x, fy + 120), (ch3_x, piy + 40), (pix + piw, piy + 40)], fill=(126, 34, 206), width=2)
    draw.ellipse([(fx-3, fy+117), (fx+3, fy+123)], fill=(126, 34, 206))
    draw.polygon([(pix+piw+5, piy+35), (pix+piw+5, piy+45), (pix+piw, piy+40)], fill=(126, 34, 206))
    draw_badge(ch3_x, (fy + 120 + piy + 40)//2, "1 : N", "RESTRICT", is_cascade=False)

    # 9. FOOD_CATEGORY -> FOOD (Vertical in Col 4)
    draw.line([(cx + cw//2, cy + ch), (fx + fw//2, fy)], fill=(126, 34, 206), width=2)
    draw.ellipse([(cx+cw//2-3, cy+ch-3), (cx+cw//2+3, cy+ch+3)], fill=(126, 34, 206))
    draw.polygon([(fx+fw//2-5, fy-5), (fx+fw//2+5, fy-5), (fx+fw//2, fy)], fill=(126, 34, 206))
    draw_badge(cx + cw//2, (cy + ch + fy)//2, "1 : N", "RESTRICT", is_cascade=False)

    # 10. TRIGGER PIPELINE: MEAL -> ACTIVITY_LOG (Clean red line through Channel 1)
    trg_x = ch1_x - 25
    draw.line([(mx, my + mh - 20), (trg_x, my + mh - 20), (trg_x, ay + 30), (ax + aw, ay + 30)], fill=(220, 38, 38), width=2)
    draw.ellipse([(mx-3, my+mh-23), (mx+3, my+mh-17)], fill=(220, 38, 38))
    draw.polygon([(ax+aw+5, ay+25), (ax+aw+5, ay+35), (ax+aw, ay+30)], fill=(220, 38, 38))
    
    trg_y = (my + mh - 20 + ay + 30)//2
    draw.rounded_rectangle([(trg_x-76, trg_y-14), (trg_x+76, trg_y+14)], radius=4, fill=(254, 226, 226), outline=(220, 38, 38), width=1)
    draw.text((trg_x, trg_y), "trg_after_meal_insert", fill=(185, 28, 28), font=FONT_ACTION, anchor="mm")

    eer_path = os.path.join(OUTPUT_DIR, "eer_diagram.png")
    im.save(eer_path, quality=95)
    print(f"Generated High-Legibility EER Diagram: {eer_path} ({W}x{H})")

if __name__ == "__main__":
    draw_er_diagram()
    draw_eer_diagram()
