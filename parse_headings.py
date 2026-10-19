import docx

doc_path = r"e:\300软件开发\04战略管理体系\战略管理需求规格书（整合版）.docx"
doc = docx.Document(doc_path)

for i, para in enumerate(doc.paragraphs):
    style = para.style.name if para.style else ""
    text = para.text.strip()
    if not text:
        continue
    if style in ("Heading 1", "Heading 2"):
        print(f"[{i}] [{style}] {text}")
