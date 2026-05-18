# Comparison: PDF Generation Libraries for Android

**Context:** Efficiently generating PDF documents from OCR-extracted text.
**Recommendation:** **Native Android `PdfDocument` API** combined with **`StaticLayout`** for text wrapping.

## Quick Comparison

| Criterion | iText 7/8 | PdfBox-Android | Native `PdfDocument` |
|-----------|-----------|----------------|----------------------|
| **License** | AGPL / Commercial | Apache 2.0 | Free (Native) |
| **Ease of Use** | High (Object-oriented) | Medium (Manual Wrap) | Medium (via `StaticLayout`) |
| **Searchability** | Yes | Yes | Yes |
| **APK Size** | Large (+5MB) | Medium (+2MB) | Zero |
| **Layout** | Automatic | Manual | Native Skia/Canvas |

## Detailed Analysis

### Native Android `PdfDocument` (with `StaticLayout`)
**Strengths:**
- **Zero Overhead:** No extra libraries added to the APK.
- **Searchable Text:** Drawing directly to the PDF canvas preserves text data.
- **Native Performance:** Uses the system's underlying printing/PDF engine (Skia).
- **Text Wrapping:** When used with `StaticLayout`, it leverages the same engine Android uses for UI text, handling line breaks and hyphenation perfectly.

**Weaknesses:**
- **Basic Feature Set:** No built-in support for merging PDFs, encryption, or digital signatures.
- **Manual Page Management:** Developers must manually calculate when a page is full and start a new one.

**Best for:** Standard OCR text export where searchability and small APK size are priorities.

### PdfBox-Android
**Strengths:**
- **Apache 2.0 License:** Permissive for any commercial use.
- **Rich Features:** Supports merging, splitting, and low-level PDF manipulation.
- **Community Support:** Port of the well-known Apache PDFBox.

**Weaknesses:**
- **Low-level API:** Does not handle text wrapping by default (requires manual coordinate math).
- **Performance:** Can be memory-intensive compared to the native API.

**Best for:** Apps needing to modify existing PDFs or perform complex PDF-specific tasks beyond simple generation.

### iText 7/8
**Strengths:**
- **Enterprise Grade:** Supports PDF/A, digital signatures, and advanced typography.
- **High-level API:** Very easy to use for complex layouts (Tables, Lists, Paragraphs).

**Weaknesses:**
- **Licensing:** The AGPL license requires the host app to be open-source. Commercial licenses are expensive and often geared toward enterprise budgets.
- **Bloat:** Adds significant weight to the application.

**Best for:** Large-scale enterprise applications with complex formatting requirements and a budget.

## Recommendation

For the **Android_OCR** project, the **Native `PdfDocument` API** is recommended.

**Why:**
1. **Searchability:** It ensures extracted OCR text remains searchable.
2. **Text Wrapping:** `StaticLayout` provides robust multi-line support with minimal effort.
3. **No License Risk:** Avoids the legal complexity of AGPL (iText).
4. **App Size:** Keeps the OCR app lightweight.

**Implementation Strategy:**
- Use `PdfDocument` for page management.
- Use `TextPaint` and `StaticLayout.Builder` to handle text wrapping within page margins.
- Track `StaticLayout.getHeight()` to detect page overflows.

## Sources
- [Android Developers: PdfDocument](https://developer.android.com/reference/android/graphics/pdf/PdfDocument)
- [Android Developers: StaticLayout](https://developer.android.com/reference/android/text/StaticLayout)
- [PdfBox-Android GitHub](https://github.com/TomRoush/PdfBox-Android)
- [iText Licensing Overview](https://itextpdf.com/en/how-it-works/licensing)
