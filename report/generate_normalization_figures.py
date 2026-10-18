"""
NutriGuide Normalization Figures Generator
Generates:
Figure 10.1: Before 1NF (Repeating groups in single cells)
Figure 10.2: After 1NF (Atomic attributes in meal & meal_item)
Figure 10.3: Before 2NF (Partial dependencies on composite key)
Figure 10.4: After 2NF (Decomposed relations without partial dependencies)
Figure 10.5: Before 3NF (Transitive dependency food_id -> category_id -> category_name)
Figure 10.6: After 3NF (Separate food_category relation satisfying 3NF/BCNF)
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

FONT_HEADER = get_font("segoeuib.ttf", 15)
FONT_BOLD = get_font("segoeuib.ttf", 13)
FONT_REGULAR = get_font("segoeui.ttf", 12)
FONT_SMALL = get_font("segoeui.ttf", 11)
FONT_CAPTION = get_font("segoeuib.ttf", 12)

def draw_table(draw, x, y, col_widths, headers, rows, header_bg=(230, 240, 255), alert_col=None, alert_rows=None):
    total_w = sum(col_widths)
    h_row = 30
    # Header
    draw.rectangle([(x, y), (x + total_w, y + h_row)], fill=header_bg, outline=(160, 180, 210), width=1)
    cx = x
    for idx, (h, w) in enumerate(zip(headers, col_widths)):
        draw.text((cx + 8, y + 6), h, fill=(20, 40, 80), font=FONT_BOLD)
        if idx > 0:
            draw.line([(cx, y), (cx, y + h_row + len(rows)*26)], fill=(190, 205, 225), width=1)
        cx += w

    # Rows
    cy = y + h_row
    for r_idx, row in enumerate(rows):
        row_bg = (255, 255, 255)
        if alert_rows and r_idx in alert_rows:
            row_bg = (255, 240, 240)
        elif r_idx % 2 == 1:
            row_bg = (248, 250, 254)

        draw.rectangle([(x, cy), (x + total_w, cy + 26)], fill=row_bg, outline=(210, 220, 235), width=1)
        cx = x
        for c_idx, (val, w) in enumerate(zip(row, col_widths)):
            text_col = (180, 30, 30) if (alert_col and c_idx in alert_col) else (40, 50, 60)
            draw.text((cx + 8, cy + 5), str(val), fill=text_col, font=FONT_REGULAR)
            cx += w
        cy += 26

    # Outer border
    draw.rectangle([(x, y), (x + total_w, cy)], outline=(140, 165, 200), width=2)
    return cy

# ==============================================================================
# Figure 10.1: Before 1NF
# ==============================================================================
def draw_fig_10_1():
    W, H = 1400, 320
    im = Image.new("RGB", (W, H), color=(255, 255, 255))
    draw = ImageDraw.Draw(im)

    headers = ["meal_id", "user_id", "meal_type", "meal_date", "food_names (Repeating Group)", "quantities (Repeating Group)"]
    widths = [90, 80, 120, 120, 520, 430]
    rows = [
        ["1", "1", "Lunch", "2026-10-18", "Grilled Chicken Breast, Cooked Rice, Salad", "1.0, 1.5, 0.5"],
        ["2", "1", "Dinner", "2026-10-18", "Yellow Dal, Whole Wheat Roti, Fresh Paneer", "1.0, 2.0, 0.75"],
        ["3", "2", "Breakfast", "2026-10-18", "Rolled Oats, Boiled Egg, Banana", "1.0, 2.0, 1.0"]
    ]
    end_y = draw_table(draw, 20, 30, widths, headers, rows, header_bg=(255, 235, 235), alert_col=[4, 5])
    draw.text((25, end_y + 12), "* Red highlight indicates non-atomic multi-valued attributes / repeating groups violating First Normal Form (1NF).", fill=(180, 40, 40), font=FONT_SMALL)
    draw.text((W//2, H - 20), "Figure 10.1: Before 1NF — Non-atomic repeating food items and quantities packed into single cells", fill=(50, 60, 70), font=FONT_CAPTION, anchor="mm")

    path = os.path.join(OUTPUT_DIR, "figure_10_1_before_1nf.png")
    im.save(path)
    print("Saved:", path)

# ==============================================================================
# Figure 10.2: After 1NF
# ==============================================================================
def draw_fig_10_2():
    W, H = 1400, 480
    im = Image.new("RGB", (W, H), color=(255, 255, 255))
    draw = ImageDraw.Draw(im)

    draw.text((25, 10), "Table 1: MEAL (Atomic Meal Event Header)", fill=(20, 70, 120), font=FONT_BOLD)
    h1 = ["meal_id (PK)", "user_id (FK)", "meal_type", "meal_date"]
    w1 = [150, 150, 200, 200]
    r1 = [
        ["1", "1", "Lunch", "2026-10-18"],
        ["2", "1", "Dinner", "2026-10-18"]
    ]
    y1 = draw_table(draw, 25, 35, w1, h1, r1, header_bg=(230, 245, 235))

    draw.text((25, y1 + 25), "Table 2: MEAL_ITEM (Atomic Food Entries Resolved via Foreign Key)", fill=(20, 70, 120), font=FONT_BOLD)
    h2 = ["meal_item_id (PK)", "meal_id (FK)", "food_id (FK)", "quantity"]
    w2 = [180, 160, 160, 160]
    r2 = [
        ["101", "1", "4 (Chicken)", "1.00"],
        ["102", "1", "1 (Rice)", "1.50"],
        ["103", "1", "12 (Salad)", "0.50"],
        ["104", "2", "5 (Yellow Dal)", "1.00"],
        ["105", "2", "2 (Roti)", "2.00"],
        ["106", "2", "6 (Paneer)", "0.75"]
    ]
    y2 = draw_table(draw, 25, y1 + 50, w2, h2, r2, header_bg=(230, 245, 235))

    draw.text((W//2, H - 20), "Figure 10.2: After 1NF — Decomposed into meal and meal_item with 100% atomic attribute values", fill=(50, 60, 70), font=FONT_CAPTION, anchor="mm")
    path = os.path.join(OUTPUT_DIR, "figure_10_2_after_1nf.png")
    im.save(path)
    print("Saved:", path)

# ==============================================================================
# Figure 10.3: Before 2NF
# ==============================================================================
def draw_fig_10_3():
    W, H = 1400, 340
    im = Image.new("RGB", (W, H), color=(255, 255, 255))
    draw = ImageDraw.Draw(im)

    headers = ["meal_id (Part PK)", "food_id (Part PK)", "user_id", "user_name", "meal_date", "food_name", "serving_size", "quantity"]
    widths = [140, 140, 100, 180, 130, 240, 140, 110]
    rows = [
        ["1", "4", "1", "John Doe", "2026-10-18", "Grilled Chicken", "100.00g", "1.00"],
        ["1", "1", "1", "John Doe", "2026-10-18", "Cooked Rice", "100.00g", "1.50"],
        ["2", "5", "1", "John Doe", "2026-10-18", "Yellow Dal", "100.00g", "1.00"]
    ]
    end_y = draw_table(draw, 20, 30, widths, headers, rows, header_bg=(255, 245, 230), alert_col=[3, 5, 6])
    draw.text((25, end_y + 12), "* Partial Functional Dependencies: user_name depends only on user_id; food_name & serving_size depend only on food_id.", fill=(180, 60, 20), font=FONT_SMALL)
    draw.text((W//2, H - 20), "Figure 10.3: Before 2NF — Partial dependencies on subsets of composite candidate key (meal_id, food_id)", fill=(50, 60, 70), font=FONT_CAPTION, anchor="mm")

    path = os.path.join(OUTPUT_DIR, "figure_10_3_before_2nf.png")
    im.save(path)
    print("Saved:", path)

# ==============================================================================
# Figure 10.4: After 2NF
# ==============================================================================
def draw_fig_10_4():
    W, H = 1400, 480
    im = Image.new("RGB", (W, H), color=(255, 255, 255))
    draw = ImageDraw.Draw(im)

    draw.text((25, 10), "Relation 1: MEAL_ITEM (Associative Entity — Fully dependent on key)", fill=(20, 70, 120), font=FONT_BOLD)
    h1 = ["meal_item_id (PK)", "meal_id (FK)", "food_id (FK)", "quantity (Fully Dependent)"]
    w1 = [170, 160, 160, 220]
    r1 = [
        ["101", "1", "4", "1.00"],
        ["102", "1", "1", "1.50"]
    ]
    y1 = draw_table(draw, 25, 35, w1, h1, r1, header_bg=(240, 245, 255))

    draw.text((25, y1 + 25), "Relation 2: FOOD (Separated — Dependent solely on food_id)", fill=(20, 70, 120), font=FONT_BOLD)
    h2 = ["food_id (PK)", "category_id", "food_name", "serving_size", "calories"]
    w2 = [140, 140, 240, 150, 140]
    r2 = [
        ["1", "1", "Cooked White Rice", "100.00", "130.00"],
        ["4", "2", "Grilled Chicken Breast", "100.00", "165.00"]
    ]
    y2 = draw_table(draw, 25, y1 + 50, w2, h2, r2, header_bg=(240, 245, 255))

    draw.text((W//2, H - 20), "Figure 10.4: After 2NF — Decomposed into relations where all non-key attributes are fully functionally dependent", fill=(50, 60, 70), font=FONT_CAPTION, anchor="mm")
    path = os.path.join(OUTPUT_DIR, "figure_10_4_after_2nf.png")
    im.save(path)
    print("Saved:", path)

# ==============================================================================
# Figure 10.5: Before 3NF
# ==============================================================================
def draw_fig_10_5():
    W, H = 1400, 340
    im = Image.new("RGB", (W, H), color=(255, 255, 255))
    draw = ImageDraw.Draw(im)

    headers = ["food_id (PK)", "food_name", "calories", "category_id (Non-Key)", "category_name (Transitive via category_id)"]
    widths = [140, 280, 140, 220, 480]
    rows = [
        ["1", "Cooked White Rice", "130.00", "1", "Grains"],
        ["2", "Whole Wheat Roti", "120.00", "1", "Grains"],
        ["4", "Grilled Chicken Breast", "165.00", "2", "Proteins"],
        ["5", "Yellow Dal (Cooked)", "116.00", "2", "Proteins"]
    ]
    end_y = draw_table(draw, 20, 30, widths, headers, rows, header_bg=(255, 240, 245), alert_col=[4])
    draw.text((25, end_y + 12), "* Transitive Dependency: food_id -> category_id -> category_name. category_name is determined by non-candidate key category_id.", fill=(160, 30, 80), font=FONT_SMALL)
    draw.text((W//2, H - 20), "Figure 10.5: Before 3NF — Transitive dependency creates update and deletion anomalies", fill=(50, 60, 70), font=FONT_CAPTION, anchor="mm")

    path = os.path.join(OUTPUT_DIR, "figure_10_5_before_3nf.png")
    im.save(path)
    print("Saved:", path)

# ==============================================================================
# Figure 10.6: After 3NF
# ==============================================================================
def draw_fig_10_6():
    W, H = 1400, 480
    im = Image.new("RGB", (W, H), color=(255, 255, 255))
    draw = ImageDraw.Draw(im)

    draw.text((25, 10), "Relation 1: FOOD (3NF Compliant — Category name removed)", fill=(20, 70, 120), font=FONT_BOLD)
    h1 = ["food_id (PK)", "category_id (FK)", "food_name", "serving_size", "calories", "protein"]
    w1 = [130, 160, 240, 140, 130, 130]
    r1 = [
        ["1", "1", "Cooked White Rice", "100.00", "130.00", "2.70"],
        ["4", "2", "Grilled Chicken Breast", "100.00", "165.00", "31.00"]
    ]
    y1 = draw_table(draw, 25, 35, w1, h1, r1, header_bg=(240, 255, 245))

    draw.text((25, y1 + 25), "Relation 2: FOOD_CATEGORY (Independent Lookup Entity satisfying 3NF & BCNF)", fill=(20, 70, 120), font=FONT_BOLD)
    h2 = ["category_id (PK)", "category_name (UNIQUE Candidate Key)"]
    w2 = [180, 360]
    r2 = [
        ["1", "Grains"],
        ["2", "Proteins"],
        ["3", "Dairy"],
        ["4", "Fruits"],
        ["5", "Vegetables"]
    ]
    y2 = draw_table(draw, 25, y1 + 50, w2, h2, r2, header_bg=(240, 255, 245))

    draw.text((W//2, H - 20), "Figure 10.6: After 3NF — Transitive dependency eliminated by isolating food_category reference table", fill=(50, 60, 70), font=FONT_CAPTION, anchor="mm")
    path = os.path.join(OUTPUT_DIR, "figure_10_6_after_3nf.png")
    im.save(path)
    print("Saved:", path)

draw_fig_10_1()
draw_fig_10_2()
draw_fig_10_3()
draw_fig_10_4()
draw_fig_10_5()
draw_fig_10_6()
