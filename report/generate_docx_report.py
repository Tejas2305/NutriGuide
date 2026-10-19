# -*- coding: utf-8 -*-
"""
NutriGuide DBMS Project Report - DOCX Generator
Builds a publication-grade, professionally styled Microsoft Word document (.docx)
matching Symbiosis Institute of Technology (SIT), Pune standards.
Target length: 35-40 pages.
"""

import os
import re
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

REPORT_DIR = r"D:\NutriGuide\report"
IMG_DIR = os.path.join(REPORT_DIR, "images")
MD_FILE = os.path.join(REPORT_DIR, "B3_GRP-1_NutriGuide.md")
DOCX_FILE = os.path.join(REPORT_DIR, "B3_GRP-1_NutriGuide.docx")

# Color Palette
COLOR_PRIMARY_NAVY = RGBColor(0x1A, 0x36, 0x5D)   # #1A365D - Main Title & Headings
COLOR_SECONDARY_BLUE = RGBColor(0x2B, 0x6C, 0xB0) # #2B6CB0 - Subheadings & Table Headers
COLOR_DARK_TEXT = RGBColor(0x2D, 0x37, 0x48)      # #2D3748 - Body Text
COLOR_MUTED_GREY = RGBColor(0x71, 0x80, 0x96)     # #718096 - Metadata & Captions
COLOR_CODE_TEXT = RGBColor(0x1A, 0x20, 0x2C)      # #1A202C - Code Text

HEX_HEADER_BG = "2B6CB0"       # Blue header background
HEX_ALT_ROW_BG = "F7FAFC"      # Subtle light grey for alternating rows
HEX_BORDER = "CBD5E0"          # Light slate border
HEX_CODE_BG = "F8FAFC"         # Code block background

def set_cell_background(cell, fill_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:val'), 'clear')
    shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'), fill_hex)
    tcPr.append(shd)

def set_cell_margins(cell, top=100, bottom=100, left=140, right=140):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('w:top', top), ('w:bottom', bottom), ('w:left', left), ('w:right', right)]:
        node = OxmlElement(m)
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def set_table_borders(table, color_hex="CBD5E0"):
    tblPr = table._tbl.tblPr
    tblBorders = OxmlElement('w:tblBorders')
    for b_name in ['top', 'left', 'bottom', 'right', 'insideH', 'insideV']:
        border = OxmlElement(f'w:{b_name}')
        border.set(qn('w:val'), 'single')
        border.set(qn('w:sz'), '4') # 0.5 pt
        border.set(qn('w:space'), '0')
        border.set(qn('w:color'), color_hex)
        tblBorders.append(border)
    tblPr.append(tblBorders)

def make_row_header(row):
    trPr = row._tr.get_or_add_trPr()
    tblHeader = OxmlElement('w:tblHeader')
    trPr.append(tblHeader)
    cantSplit = OxmlElement('w:cantSplit')
    trPr.append(cantSplit)

def make_row_cant_split(row):
    trPr = row._tr.get_or_add_trPr()
    cantSplit = OxmlElement('w:cantSplit')
    trPr.append(cantSplit)

def add_page_number_to_run(run):
    fldChar1 = OxmlElement('w:fldChar')
    fldChar1.set(qn('w:fldCharType'), 'begin')
    instrText = OxmlElement('w:instrText')
    instrText.set(qn('xml:space'), 'preserve')
    instrText.text = "PAGE"
    fldChar2 = OxmlElement('w:fldChar')
    fldChar2.set(qn('w:fldCharType'), 'separate')
    fldChar3 = OxmlElement('w:fldChar')
    fldChar3.set(qn('w:fldCharType'), 'end')
    run._r.append(fldChar1)
    run._r.append(instrText)
    run._r.append(fldChar2)
    run._r.append(fldChar3)

def add_formatted_text(paragraph, text, base_font_size=10.5, is_bullet=False):
    """Parses markdown inline tokens: **bold**, *italic*, `code`, <u>underline</u>"""
    # Tokenize by markdown markers
    pattern = re.compile(r"(\*\*.*?\*\*|\*.*?\*|`.*?`|<u>.*?</u>)")
    tokens = pattern.split(text)
    
    for token in tokens:
        if not token:
            continue
        if token.startswith("**") and token.endswith("**") and len(token) >= 4:
            r = paragraph.add_run(token[2:-2])
            r.bold = True
            r.font.name = "Calibri"
            r.font.size = Pt(base_font_size)
            r.font.color.rgb = COLOR_DARK_TEXT
        elif token.startswith("*") and token.endswith("*") and len(token) >= 2:
            r = paragraph.add_run(token[1:-1])
            r.italic = True
            r.font.name = "Calibri"
            r.font.size = Pt(base_font_size)
            r.font.color.rgb = COLOR_DARK_TEXT
        elif token.startswith("`") and token.endswith("`") and len(token) >= 2:
            r = paragraph.add_run(token[1:-1])
            r.font.name = "Consolas"
            r.font.size = Pt(base_font_size - 1)
            r.font.color.rgb = RGBColor(0x9B, 0x2C, 0x2C) # Dark red for inline code
        elif token.startswith("<u>") and token.endswith("</u>"):
            r = paragraph.add_run(token[3:-4])
            r.underline = True
            r.font.name = "Calibri"
            r.font.size = Pt(base_font_size)
            r.font.color.rgb = COLOR_DARK_TEXT
        else:
            r = paragraph.add_run(token)
            r.font.name = "Calibri"
            r.font.size = Pt(base_font_size)
            r.font.color.rgb = COLOR_DARK_TEXT

def build_docx_report():
    print(f"Reading markdown source: {MD_FILE}")
    with open(MD_FILE, "r", encoding="utf-8") as f:
        md_text = f.read()

    doc = docx.Document()
    
    # Page setup: Standard Letter/A4, 0.8 in margins to fit cleanly in 35-40 pages
    for sec in doc.sections:
        sec.top_margin = Inches(0.8)
        sec.bottom_margin = Inches(0.8)
        sec.left_margin = Inches(0.85)
        sec.right_margin = Inches(0.85)
        sec.page_width = Inches(8.5)
        sec.page_height = Inches(11.0)
        sec.different_first_page_header_footer = True
        
        # Subsequent page header
        header = sec.header
        hp = header.paragraphs[0]
        hp.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        hrun = hp.add_run("SIT Pune | B3_GRP-1 | NutriGuide DBMS Project Report")
        hrun.font.name = "Calibri"
        hrun.font.size = Pt(8.5)
        hrun.font.italic = True
        hrun.font.color.rgb = COLOR_MUTED_GREY
        
        # Subsequent page footer
        footer = sec.footer
        fp = footer.paragraphs[0]
        fp.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        frun = fp.add_run("Page ")
        frun.font.name = "Calibri"
        frun.font.size = Pt(9.0)
        frun.font.color.rgb = COLOR_MUTED_GREY
        add_page_number_to_run(frun)

    # -------------------------------------------------------------
    # 1. COVER PAGE (Page 1)
    # -------------------------------------------------------------
    logo_path = os.path.join(IMG_DIR, "symbiosis_logo.png")
    if os.path.exists(logo_path):
        p_logo = doc.add_paragraph()
        p_logo.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_logo.paragraph_format.space_before = Pt(10)
        p_logo.paragraph_format.space_after = Pt(4)
        run_logo = p_logo.add_run()
        run_logo.add_picture(logo_path, width=Inches(1.25))

    p_inst = doc.add_paragraph()
    p_inst.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_inst.paragraph_format.space_after = Pt(2)
    r_inst = p_inst.add_run("॥ वसुधैव कुटुम्बकम् ॥")
    r_inst.font.name = "Calibri"
    r_inst.font.size = Pt(10)
    r_inst.font.bold = True
    r_inst.font.color.rgb = COLOR_MUTED_GREY

    p_head = doc.add_paragraph()
    p_head.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_head.paragraph_format.space_before = Pt(8)
    p_head.paragraph_format.space_after = Pt(2)
    r_head = p_head.add_run("A DBMS MINI PROJECT REPORT ON")
    r_head.font.name = "Calibri"
    r_head.font.size = Pt(13)
    r_head.font.bold = True
    r_head.font.color.rgb = COLOR_MUTED_GREY

    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(2)
    p_title.paragraph_format.space_after = Pt(6)
    r_title = p_title.add_run("NutriGuide — Your Personal Nutrition Companion")
    r_title.font.name = "Calibri"
    r_title.font.size = Pt(20)
    r_title.font.bold = True
    r_title.font.color.rgb = COLOR_PRIMARY_NAVY

    cover_ill = os.path.join(IMG_DIR, "cover_illustration.jpg")
    if os.path.exists(cover_ill):
        p_ill = doc.add_paragraph()
        p_ill.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_ill.paragraph_format.space_before = Pt(2)
        p_ill.paragraph_format.space_after = Pt(6)
        run_ill = p_ill.add_run()
        run_ill.add_picture(cover_ill, width=Inches(2.5))

    p_batch = doc.add_paragraph()
    p_batch.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_batch.paragraph_format.space_before = Pt(4)
    p_batch.paragraph_format.space_after = Pt(2)
    r_batch = p_batch.add_run("Batch: B3  |  Group: GRP-1")
    r_batch.font.name = "Calibri"
    r_batch.font.size = Pt(11.5)
    r_batch.font.bold = True
    r_batch.font.color.rgb = COLOR_SECONDARY_BLUE

    p_sub = doc.add_paragraph()
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sub.paragraph_format.space_before = Pt(4)
    p_sub.paragraph_format.space_after = Pt(2)
    r_sub = p_sub.add_run("Submitted by:")
    r_sub.font.name = "Calibri"
    r_sub.font.size = Pt(11)
    r_sub.font.bold = True
    r_sub.font.color.rgb = COLOR_DARK_TEXT

    members = [
        ("Tejas Jagdish Patil", "26070122510"),
        ("Veer Parmar", "26070122508"),
        ("Postiwala Vatsal Dipakbhai", "26070122509"),
        ("Parth Bhayani", "26070122512")
    ]
    for name, prn in members:
        pm = doc.add_paragraph()
        pm.alignment = WD_ALIGN_PARAGRAPH.CENTER
        pm.paragraph_format.space_after = Pt(1)
        rm = pm.add_run(f"{name} ({prn})")
        rm.font.name = "Calibri"
        rm.font.size = Pt(10.5)
        rm.font.bold = True
        rm.font.color.rgb = COLOR_PRIMARY_NAVY

    p_guide = doc.add_paragraph()
    p_guide.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_guide.paragraph_format.space_before = Pt(10)
    p_guide.paragraph_format.space_after = Pt(2)
    rg_lbl = p_guide.add_run("Under the Guidance of\n")
    rg_lbl.font.size = Pt(10.5)
    rg_lbl.font.italic = True
    rg_name = p_guide.add_run("Dr. Balkrishna Patil")
    rg_name.font.size = Pt(12)
    rg_name.font.bold = True
    rg_name.font.color.rgb = COLOR_PRIMARY_NAVY

    p_dept = doc.add_paragraph()
    p_dept.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_dept.paragraph_format.space_before = Pt(12)
    p_dept.paragraph_format.space_after = Pt(2)
    rd = p_dept.add_run("Department of Computer Science\nSymbiosis Institute of Technology (SIT), Pune\nSymbiosis International (Deemed University)\nAcademic Year 2026–2027")
    rd.font.name = "Calibri"
    rd.font.size = Pt(10)
    rd.font.color.rgb = COLOR_DARK_TEXT

    doc.add_page_break()

    # -------------------------------------------------------------
    # 2. PARSE MARKDOWN BODY
    # -------------------------------------------------------------
    lines = md_text.split("\n")
    
    # Skip until after the initial metadata (start from ## INDEX)
    idx_start = 0
    for i, line in enumerate(lines):
        if line.strip().startswith("## INDEX"):
            idx_start = i
            break

    in_code_block = False
    code_lines = []
    in_table = False
    table_lines = []

    def flush_table_to_docx(t_lines):
        if not t_lines:
            return
        # Parse table cells
        rows_data = []
        sep_idx = -1
        for idx, row_str in enumerate(t_lines):
            stripped = row_str.strip()
            if not stripped.startswith("|"):
                continue
            if re.match(r"^\|?\s*[:\-]+\s*\|", stripped):
                sep_idx = idx
                continue
            cells = [c.strip() for c in stripped.strip("|").split("|")]
            rows_data.append(cells)

        if not rows_data:
            return

        num_cols = max(len(r) for r in rows_data)
        tbl = doc.add_table(rows=len(rows_data), cols=num_cols)
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        set_table_borders(tbl, HEX_BORDER)

        for r_idx, r_data in enumerate(rows_data):
            row = tbl.rows[r_idx]
            is_header = (r_idx == 0)
            if is_header:
                make_row_header(row)
            else:
                make_row_cant_split(row)

            bg_color = HEX_HEADER_BG if is_header else (HEX_ALT_ROW_BG if r_idx % 2 == 1 else "FFFFFF")

            for c_idx in range(num_cols):
                cell = row.cells[c_idx]
                cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
                set_cell_background(cell, bg_color)
                set_cell_margins(cell, top=80, bottom=80, left=120, right=120)
                
                raw_cell_text = r_data[c_idx] if c_idx < len(r_data) else ""
                # Clean up leaked HTML breaks and backticks
                cleaned_cell_text = raw_cell_text.replace("<br>•", "\n• ").replace("<br/>•", "\n• ").replace("<br>", "\n").replace("<br/>", "\n").replace("`", "")
                
                cp = cell.paragraphs[0]
                cp.paragraph_format.space_after = Pt(1)
                cp.paragraph_format.space_before = Pt(1)
                cp.paragraph_format.line_spacing = 1.05

                if is_header:
                    cp.alignment = WD_ALIGN_PARAGRAPH.CENTER
                    r_cell = cp.add_run(cleaned_cell_text)
                    r_cell.font.name = "Calibri"
                    r_cell.font.size = Pt(9.5)
                    r_cell.font.bold = True
                    r_cell.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
                else:
                    lines_in_cell = cleaned_cell_text.split("\n")
                    for l_idx, line_item in enumerate(lines_in_cell):
                        if l_idx > 0:
                            p_next = cell.add_paragraph()
                            p_next.paragraph_format.space_after = Pt(1)
                            p_next.paragraph_format.space_before = Pt(1)
                            p_next.paragraph_format.line_spacing = 1.05
                            add_formatted_text(p_next, line_item, base_font_size=9.0)
                        else:
                            add_formatted_text(cp, line_item, base_font_size=9.0)

        p_spacer = doc.add_paragraph()
        p_spacer.paragraph_format.space_after = Pt(4)

    def flush_code_to_docx(c_lines):
        if not c_lines:
            return
        code_text = "\n".join(c_lines).strip()
        # Create a single-cell callout box for the code block
        tbl = doc.add_table(rows=1, cols=1)
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        set_table_borders(tbl, HEX_BORDER)
        cell = tbl.cell(0, 0)
        set_cell_background(cell, HEX_CODE_BG)
        set_cell_margins(cell, top=100, bottom=100, left=140, right=140)
        
        cp = cell.paragraphs[0]
        cp.paragraph_format.space_before = Pt(0)
        cp.paragraph_format.space_after = Pt(0)
        cp.paragraph_format.line_spacing = 1.0

        for line_idx, cl in enumerate(c_lines):
            if line_idx > 0:
                cp = cell.add_paragraph()
                cp.paragraph_format.space_before = Pt(0)
                cp.paragraph_format.space_after = Pt(0)
                cp.paragraph_format.line_spacing = 1.0
            r = cp.add_run(cl)
            r.font.name = "Consolas"
            r.font.size = Pt(7.5)
            r.font.color.rgb = COLOR_CODE_TEXT

        p_spacer = doc.add_paragraph()
        p_spacer.paragraph_format.space_after = Pt(2)

    i = idx_start
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        # Handle Code Blocks
        if stripped.startswith("```"):
            if in_code_block:
                in_code_block = False
                flush_code_to_docx(code_lines)
                code_lines = []
            else:
                if in_table:
                    in_table = False
                    flush_table_to_docx(table_lines)
                    table_lines = []
                in_code_block = True
                code_lines = []
            i += 1
            continue

        if in_code_block:
            code_lines.append(line)
            i += 1
            continue

        # Handle Tables
        if stripped.startswith("|") and stripped.endswith("|"):
            in_table = True
            table_lines.append(stripped)
            i += 1
            continue
        elif in_table:
            in_table = False
            flush_table_to_docx(table_lines)
            table_lines = []

        # Empty line
        if not stripped:
            i += 1
            continue

        # Horizontal separator
        if stripped == "---":
            i += 1
            continue

        # Images: ![Caption](images/filename.ext)
        img_match = re.match(r"!\[(.*?)\]\((.*?)\)", stripped)
        if img_match:
            caption = img_match.group(1)
            img_rel = img_match.group(2).replace("/", os.sep)
            img_path = os.path.join(REPORT_DIR, img_rel)
            
            if os.path.exists(img_path):
                # Choose image size appropriately (compact, neat, fits 35-40 page target)
                fname = os.path.basename(img_path).lower()
                if "architecture" in fname:
                    w = Inches(4.3)
                elif "eer" in fname:
                    w = Inches(4.6)
                elif "er_" in fname:
                    w = Inches(4.3)
                elif "figure_10" in fname:
                    w = Inches(3.8)
                elif "cover" in fname:
                    w = Inches(2.2)
                elif "logo" in fname:
                    w = Inches(1.2)
                else:
                    w = Inches(4.0)

                p_img = doc.add_paragraph()
                p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
                p_img.paragraph_format.space_before = Pt(4)
                p_img.paragraph_format.space_after = Pt(1)
                r_img = p_img.add_run()
                r_img.add_picture(img_path, width=w)
            i += 1
            continue

        # Image captions: *Figure X.X: ...*
        if stripped.startswith("*Figure") and stripped.endswith("*"):
            p_cap = doc.add_paragraph()
            p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p_cap.paragraph_format.space_before = Pt(1)
            p_cap.paragraph_format.space_after = Pt(4)
            r_cap = p_cap.add_run(stripped.strip("*"))
            r_cap.font.name = "Calibri"
            r_cap.font.size = Pt(8.5)
            r_cap.font.italic = True
            r_cap.font.color.rgb = COLOR_MUTED_GREY
            i += 1
            continue

        # Headings
        if stripped.startswith("## INDEX"):
            p = doc.add_paragraph()
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p.paragraph_format.space_before = Pt(6)
            p.paragraph_format.space_after = Pt(6)
            r = p.add_run("INDEX")
            r.font.name = "Calibri"
            r.font.size = Pt(15)
            r.font.bold = True
            r.font.color.rgb = COLOR_PRIMARY_NAVY
            i += 1
            continue

        if stripped.startswith("## "):
            heading_text = stripped[3:].strip().replace("`", "")
            p = doc.add_paragraph()
            p.paragraph_format.space_before = Pt(9)
            p.paragraph_format.space_after = Pt(2)
            p.paragraph_format.keep_with_next = True
            r = p.add_run(heading_text)
            r.font.name = "Calibri"
            r.font.size = Pt(12.5)
            r.font.bold = True
            r.font.color.rgb = COLOR_PRIMARY_NAVY
            i += 1
            continue

        if stripped.startswith("### "):
            heading_text = stripped[4:].strip().replace("`", "")
            p = doc.add_paragraph()
            p.paragraph_format.space_before = Pt(7)
            p.paragraph_format.space_after = Pt(1.5)
            p.paragraph_format.keep_with_next = True
            r = p.add_run(heading_text)
            r.font.name = "Calibri"
            r.font.size = Pt(11.0)
            r.font.bold = True
            r.font.color.rgb = COLOR_SECONDARY_BLUE
            i += 1
            continue

        if stripped.startswith("#### "):
            heading_text = stripped[5:].strip().replace("`", "")
            p = doc.add_paragraph()
            p.paragraph_format.space_before = Pt(5)
            p.paragraph_format.space_after = Pt(1)
            p.paragraph_format.keep_with_next = True
            r = p.add_run(heading_text)
            r.font.name = "Calibri"
            r.font.size = Pt(10.0)
            r.font.bold = True
            r.font.italic = True
            r.font.color.rgb = COLOR_DARK_TEXT
            i += 1
            continue

        # Bullet list items
        if stripped.startswith("- ") or stripped.startswith("* "):
            bullet_text = stripped[2:].strip()
            p = doc.add_paragraph(style='List Bullet')
            p.paragraph_format.space_before = Pt(1)
            p.paragraph_format.space_after = Pt(2)
            p.paragraph_format.line_spacing = 1.15
            add_formatted_text(p, bullet_text, base_font_size=10.0, is_bullet=True)
            i += 1
            continue

        # Numbered list items (Use manual hanging indent instead of Word's List Number to prevent continuous global numbering across sections)
        num_match = re.match(r"^(\d+)\.\s+(.*)", stripped)
        if num_match:
            prefix = num_match.group(1) + ". "
            item_text = num_match.group(2)
            p = doc.add_paragraph()
            p.paragraph_format.left_indent = Inches(0.28)
            p.paragraph_format.first_line_indent = Inches(-0.28)
            p.paragraph_format.space_before = Pt(1)
            p.paragraph_format.space_after = Pt(2)
            p.paragraph_format.line_spacing = 1.15
            r_num = p.add_run(prefix)
            r_num.font.name = "Calibri"
            r_num.font.size = Pt(10.0)
            r_num.font.bold = True
            r_num.font.color.rgb = COLOR_DARK_TEXT
            add_formatted_text(p, item_text, base_font_size=10.0)
            i += 1
            continue

        # Regular Paragraph
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(1)
        p.paragraph_format.space_after = Pt(2.5)
        p.paragraph_format.line_spacing = 1.1
        add_formatted_text(p, stripped, base_font_size=10.0)
        i += 1

    # End of document flush
    if in_table:
        flush_table_to_docx(table_lines)
    if in_code_block:
        flush_code_to_docx(code_lines)

    print(f"Saving Word document: {DOCX_FILE}")
    doc.save(DOCX_FILE)
    print(f"Successfully generated: {DOCX_FILE}")

if __name__ == "__main__":
    build_docx_report()
