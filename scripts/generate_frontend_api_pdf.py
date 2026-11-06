#!/usr/bin/env python3
from pathlib import Path
import argparse
import textwrap

from fpdf import FPDF
from fpdf.enums import XPos, YPos


def add_wrapped(pdf: FPDF, text: str, indent: int = 0, mono: bool = False):
    if mono:
        pdf.set_font("NotoSansMono", size=9)
    else:
        pdf.set_font("NotoSans", size=11)

    effective_width = 180 - indent
    wrapped = textwrap.wrap(text, width=max(20, int(effective_width / 2.2))) or [""]
    for line in wrapped:
        pdf.set_x(15 + indent)
        pdf.cell(0, 6, text=line, new_x=XPos.LMARGIN, new_y=YPos.NEXT)


def markdown_to_pdf(md_path: Path, pdf_path: Path):
    lines = md_path.read_text(encoding="utf-8").splitlines()

    pdf = FPDF()
    pdf.set_auto_page_break(auto=True, margin=12)
    pdf.add_font("NotoSans", "", "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf")
    pdf.add_font("NotoSans", "B", "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf")
    pdf.add_font("NotoSansMono", "", "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf")
    pdf.add_page()

    in_code_block = False

    for raw in lines:
        line = raw.rstrip("\n")

        if line.startswith("```"):
            in_code_block = not in_code_block
            if in_code_block:
                pdf.ln(1)
            else:
                pdf.ln(1)
            continue

        if in_code_block:
            add_wrapped(pdf, line if line else " ", indent=4, mono=True)
            continue

        if not line.strip():
            pdf.ln(2)
            continue

        if line.startswith("# "):
            pdf.set_font("NotoSans", "B", 18)
            pdf.multi_cell(0, 9, line[2:].strip())
            pdf.ln(2)
            continue

        if line.startswith("## "):
            pdf.set_font("NotoSans", "B", 14)
            pdf.multi_cell(0, 8, line[3:].strip())
            pdf.ln(1)
            continue

        if line.startswith("### "):
            pdf.set_font("NotoSans", "B", 12)
            pdf.multi_cell(0, 7, line[4:].strip())
            continue

        if line.startswith("|"):
            add_wrapped(pdf, line.replace("|", " | "), mono=True)
            continue

        if line.startswith("- [") or line.startswith("- "):
            add_wrapped(pdf, "• " + line[2:].strip())
            continue

        add_wrapped(pdf, line)

    pdf_path.parent.mkdir(parents=True, exist_ok=True)
    pdf.output(str(pdf_path))


def main():
    parser = argparse.ArgumentParser(description="Generate PDF from NoteMind frontend API markdown guide")
    parser.add_argument(
        "--input",
        default="docs/NoteMind-Frontend-API.md",
        help="Input markdown path",
    )
    parser.add_argument(
        "--output",
        default="docs/NoteMind-Frontend-API.pdf",
        help="Output PDF path",
    )
    args = parser.parse_args()

    md_path = Path(args.input)
    pdf_path = Path(args.output)

    if not md_path.exists():
        raise FileNotFoundError(f"Input markdown file not found: {md_path}")

    markdown_to_pdf(md_path, pdf_path)
    print(f"Generated {pdf_path}")


if __name__ == "__main__":
    main()
