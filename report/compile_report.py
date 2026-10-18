# -*- coding: utf-8 -*-
r"""
NutriGuide Report Compiler
Combines Parts 1, 2, and 3 into:
- D:\NutriGuide\report\NutriGuide_DBMS_Project_Report.md
- D:\NutriGuide\report\NutriGuide_DBMS_Project_Report.html
"""

import os
import re
import base64
import sys

from report_sections_part1 import get_front_matter_md
from report_sections_part2 import get_part2_md
from report_sections_part3 import get_part3_md

REPORT_DIR = r"D:\NutriGuide\report"
IMG_DIR = os.path.join(REPORT_DIR, "images")

# 1. Combine Markdown
full_md = get_front_matter_md() + "\n" + get_part2_md() + "\n" + get_part3_md()

md_path = os.path.join(REPORT_DIR, "NutriGuide_DBMS_Project_Report.md")
with open(md_path, "w", encoding="utf-8") as f:
    f.write(full_md)
print(f"Generated Markdown Report: {md_path} ({len(full_md)} chars)")

# 2. Build HTML with embedded styles and base64 images
def get_b64(fname):
    p = os.path.join(IMG_DIR, fname)
    if os.path.exists(p):
        ext = fname.split(".")[-1].lower()
        mime = "image/jpeg" if ext in ["jpg", "jpeg"] else "image/png"
        with open(p, "rb") as f:
            data = base64.b64encode(f.read()).decode("utf-8")
        return f"data:{mime};base64,{data}"
    return ""

b64_symbiosis = get_b64("symbiosis_logo.png")

# Convert markdown tables, headers, and code blocks to HTML
import html

def markdown_to_html(md_text):
    lines = md_text.split("\n")
    html_lines = []
    in_code_block = False
    in_table = False
    table_lines = []

    def flush_table():
        nonlocal in_table, table_lines
        if not table_lines:
            in_table = False
            return ""
        out = ["<div class='table-container'><table>"]
        # Filter separator row
        sep_idx = -1
        for i, tl in enumerate(table_lines):
            if re.match(r"^\|?\s*[:\-]+\s*\|", tl):
                sep_idx = i
                break
        
        headers = []
        if sep_idx > 0:
            h_row = table_lines[0].strip().strip("|").split("|")
            out.append("<thead><tr>")
            for h in h_row:
                out.append(f"<th>{h.strip()}</th>")
            out.append("</tr></thead>")
            body_rows = table_lines[sep_idx+1:]
        else:
            body_rows = table_lines

        out.append("<tbody>")
        for r in body_rows:
            r_str = r.strip()
            if not r_str or not ("|" in r_str):
                continue
            cols = r_str.strip("|").split("|")
            out.append("<tr>")
            for c in cols:
                cell_text = c.strip()
                # support bold/italic in table
                cell_text = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", cell_text)
                cell_text = re.sub(r"\*(.+?)\*", r"<em>\1</em>", cell_text)
                cell_text = re.sub(r"<u>(.+?)</u>", r"<u>\1</u>", cell_text)
                out.append(f"<td>{cell_text}</td>")
            out.append("</tr>")
        out.append("</tbody></table></div>")
        table_lines = []
        in_table = False
        return "\n".join(out)

    for line in lines:
        # Code block toggle
        if line.startswith("```"):
            if in_code_block:
                html_lines.append("</code></pre>")
                in_code_block = False
            else:
                lang = line[3:].strip()
                html_lines.append(f"<pre><code class='language-{lang}'>")
                in_code_block = True
            continue

        if in_code_block:
            html_lines.append(html.escape(line))
            continue

        # Table detection
        if "|" in line and (line.strip().startswith("|") or line.strip().endswith("|")):
            in_table = True
            table_lines.append(line)
            continue
        elif in_table:
            html_lines.append(flush_table())

        # Headers
        if line.startswith("# "):
            html_lines.append(f"<h1>{line[2:].strip()}</h1>")
            continue
        elif line.startswith("## "):
            html_lines.append(f"<h2>{line[3:].strip()}</h2>")
            continue
        elif line.startswith("### "):
            html_lines.append(f"<h3>{line[4:].strip()}</h3>")
            continue
        elif line.startswith("#### "):
            html_lines.append(f"<h4>{line[5:].strip()}</h4>")
            continue
        elif line.startswith("##### "):
            html_lines.append(f"<h5>{line[6:].strip()}</h5>")
            continue

        # Horizontal Rule
        if line.strip() == "---":
            html_lines.append("<hr class='page-divider'>")
            continue

        # Images
        img_match = re.match(r"!\[(.*?)\]\((.*?)\)", line)
        if img_match:
            alt = img_match.group(1)
            src = img_match.group(2)
            fname = os.path.basename(src)
            b64_src = get_b64(fname)
            if b64_src:
                html_lines.append(f"<div class='figure-container'><img src='{b64_src}' alt='{alt}' class='diagram-img'><p class='figure-caption'>{alt}</p></div>")
            else:
                html_lines.append(f"<div class='figure-container'><img src='{src}' alt='{alt}' class='diagram-img'><p class='figure-caption'>{alt}</p></div>")
            continue

        # Lists
        if line.strip().startswith("- ") or line.strip().startswith("* "):
            content = line.strip()[2:]
            content = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", content)
            content = re.sub(r"\*(.+?)\*", r"<em>\1</em>", content)
            content = re.sub(r"`(.+?)`", r"<code>\1</code>", content)
            html_lines.append(f"<li>{content}</li>")
            continue

        if re.match(r"^\d+\.\s", line.strip()):
            content = re.sub(r"^\d+\.\s", "", line.strip())
            content = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", content)
            content = re.sub(r"\*(.+?)\*", r"<em>\1</em>", content)
            content = re.sub(r"`(.+?)`", r"<code>\1</code>", content)
            html_lines.append(f"<li>{content}</li>")
            continue

        # Regular paragraph
        if line.strip():
            content = line.strip()
            content = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", content)
            content = re.sub(r"\*(.+?)\*", r"<em>\1</em>", content)
            content = re.sub(r"`(.+?)`", r"<code>\1</code>", content)
            html_lines.append(f"<p>{content}</p>")
        else:
            html_lines.append("")

    if in_table:
        html_lines.append(flush_table())

    return "\n".join(html_lines)

body_html = markdown_to_html(full_md)

# Full styled publication HTML template
html_template = f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>NutriGuide — DBMS Mini Project Report</title>
<style>
    @page {{
        size: A4;
        margin: 20mm 15mm 20mm 15mm;
        @bottom-right {{
            content: counter(page);
        }}
    }}
    body {{
        font-family: 'Segoe UI', Calibri, Arial, sans-serif;
        line-height: 1.6;
        color: #222222;
        background-color: #f7f9fc;
        margin: 0;
        padding: 40px 20px;
    }}
    .document-wrapper {{
        max-width: 1000px;
        margin: 0 auto;
        background: #ffffff;
        padding: 60px 50px;
        box-shadow: 0 4px 20px rgba(0,0,0,0.08);
        border-radius: 8px;
    }}
    h1 {{
        font-size: 26pt;
        color: #1a365d;
        text-align: center;
        margin-top: 10px;
        margin-bottom: 15px;
        font-weight: 700;
    }}
    h2 {{
        font-size: 18pt;
        color: #2b6cb0;
        border-bottom: 2px solid #e2e8f0;
        padding-bottom: 8px;
        margin-top: 40px;
        page-break-before: auto;
    }}
    h3 {{
        font-size: 14pt;
        color: #2d3748;
        margin-top: 25px;
        text-align: center;
    }}
    h4 {{
        font-size: 12pt;
        color: #4a5568;
        margin-top: 20px;
    }}
    p {{
        font-size: 11pt;
        text-align: justify;
        margin: 10px 0;
    }}
    li {{
        font-size: 11pt;
        margin-bottom: 6px;
    }}
    .table-container {{
        width: 100%;
        overflow-x: auto;
        margin: 20px 0;
    }}
    table {{
        width: 100%;
        border-collapse: collapse;
        font-size: 10pt;
        margin: 10px 0;
    }}
    th {{
        background-color: #edf2f7;
        color: #1a202c;
        font-weight: bold;
        text-align: left;
        padding: 10px 8px;
        border: 1px solid #cbd5e0;
    }}
    td {{
        padding: 8px;
        border: 1px solid #e2e8f0;
        vertical-align: top;
    }}
    tr:nth-child(even) {{
        background-color: #f7fafc;
    }}
    pre {{
        background-color: #f1f5f9;
        border: 1px solid #cbd5e1;
        border-radius: 6px;
        padding: 14px;
        font-family: 'Consolas', 'Courier New', monospace;
        font-size: 9.5pt;
        overflow-x: auto;
        line-height: 1.45;
        margin: 15px 0;
    }}
    code {{
        font-family: 'Consolas', 'Courier New', monospace;
        background-color: #e2e8f0;
        padding: 2px 5px;
        border-radius: 4px;
        font-size: 9.5pt;
    }}
    pre code {{
        background-color: transparent;
        padding: 0;
    }}
    .figure-container {{
        text-align: center;
        margin: 30px auto;
    }}
    .diagram-img {{
        max-width: 95%;
        height: auto;
        border: 1px solid #cbd5e1;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.06);
    }}
    .figure-caption {{
        font-size: 10pt;
        font-style: italic;
        color: #4a5568;
        margin-top: 8px;
        text-align: center;
    }}
    .page-divider {{
        border: none;
        border-top: 1px solid #e2e8f0;
        margin: 40px 0;
    }}
    @media print {{
        body {{
            background: #ffffff;
            padding: 0;
        }}
        .document-wrapper {{
            box-shadow: none;
            padding: 0;
            max-width: 100%;
        }}
        h2 {{
            page-break-before: always;
        }}
        .no-break {{
            page-break-inside: avoid;
        }}
    }}
</style>
</head>
<body>
<div class="document-wrapper">
    <div style="text-align: center; margin-bottom: 20px;">
        <img src="{b64_symbiosis}" alt="Symbiosis Logo" style="max-height: 100px;">
    </div>
    {body_html}
</div>
</body>
</html>
"""

html_path = os.path.join(REPORT_DIR, "NutriGuide_DBMS_Project_Report.html")
with open(html_path, "w", encoding="utf-8") as f:
    f.write(html_template)
print(f"Generated HTML Report: {html_path} ({len(html_template)} chars)")
