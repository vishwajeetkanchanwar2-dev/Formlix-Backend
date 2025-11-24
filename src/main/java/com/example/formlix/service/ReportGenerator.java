package com.example.formlix.service;

import com.example.formlix.model.Report;
import com.example.formlix.model.User;
import com.example.formlix.repository.ReportRepo;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportGenerator {

    private final ReportRepo reportRepository;

    @Autowired
    private AIService aiService;

    public String generateFromText(String topic, String content, String formatType, Integer pageLimit, User user) throws Exception {
        String fileName = "Report_" + topic.replace(" ", "_") + "_" + System.currentTimeMillis() + "." + formatType;

        if (formatType.equalsIgnoreCase("docx")) {
            generateWordReport(topic, content, fileName, pageLimit);
        } else {
            generatePdfReport(topic, content, fileName, pageLimit);
        }

        Report report = Report.builder()
                .topic(topic)
                .formatType(formatType)
                .filePath(fileName)
                .pageLimit(pageLimit)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        reportRepository.save(report);
        System.out.println("✅ Report saved with user: " + (user != null ? user.getEmail() : "anonymous"));

        return "Report generated: reports/" + fileName;
    }

    public String generateFromTopic(String topic, String formatType, Integer pageLimit, User user) throws Exception {
        String content = fetchContentFromAI(topic, pageLimit);
        return generateFromText(topic, content, formatType, pageLimit, user);
    }

    private String fetchContentFromAI(String topic, Integer pageLimit) {
        int actualLimit = pageLimit != null ? pageLimit : 15;

        String prompt = "Generate a complete academic research report on: " + topic + "\n\n" +
                "CRITICAL INSTRUCTIONS:\n" +
                "- DO NOT write 'Here is the report' or any meta-text\n" +
                "- Start DIRECTLY with '## Introduction'\n" +
                "- Use '## ' for main sections and '### ' for subsections\n" +
                "- NO page numbers, NO [IMAGE:] markers\n" +
                "- Each paragraph must be 5-7 sentences\n" +
                "- Generate approximately " + (actualLimit * 3500) + " characters\n\n" +

                "MANDATORY STRUCTURE (Generate ALL sections):\n\n" +

                "## Introduction\n" +
                "### Background and Context\n" +
                "(Write 4 detailed paragraphs about the topic's background, importance, and context)\n\n" +
                "### Scope of the Study\n" +
                "(Write 3 paragraphs about what this report covers)\n\n" +
                "### Significance\n" +
                "(Write 3 paragraphs about why this topic matters)\n\n" +

                "## Literature Review\n" +
                "### Theoretical Framework\n" +
                "(Write 5 paragraphs about theories and frameworks)\n\n" +
                "### Historical Perspective\n" +
                "(Write 4 paragraphs about historical development)\n\n" +
                "### Current Research Trends\n" +
                "(Write 5 paragraphs about recent research)\n\n" +
                "### Research Gaps\n" +
                "(Write 3 paragraphs about what's missing in current research)\n\n" +

                "## Aim and Objectives\n" +
                "### Primary Aim\n" +
                "(Write 3 paragraphs)\n\n" +
                "### Specific Objectives\n" +
                "(Write 4 paragraphs)\n\n" +
                "### Expected Outcomes\n" +
                "(Write 3 paragraphs)\n\n" +

                "## Methodology\n" +
                "### Research Design\n" +
                "(Write 5 paragraphs)\n\n" +
                "### Data Collection Methods\n" +
                "(Write 5 paragraphs)\n\n" +
                "### Sampling Strategy\n" +
                "(Write 4 paragraphs)\n\n" +
                "### Data Analysis Techniques\n" +
                "(Write 5 paragraphs)\n\n" +
                "### Ethical Considerations\n" +
                "(Write 3 paragraphs)\n\n" +

                "## Results and Discussion\n" +
                "### Key Findings\n" +
                "(Write 6 paragraphs)\n\n" +
                "### Detailed Analysis\n" +
                "(Write 7 paragraphs)\n\n" +
                "### Comparison with Existing Literature\n" +
                "(Write 5 paragraphs)\n\n" +
                "### Implications\n" +
                "(Write 5 paragraphs)\n\n" +

                "## Conclusion\n" +
                "(Write 6-8 comprehensive paragraphs summarizing everything. Each paragraph should be 5-7 detailed sentences. NO subsections here. Cover: summary of findings, key insights, practical implications, limitations, future directions, and final thoughts.)\n\n" +

                "## References\n" +
                "1. Author, A. (Year). Title. Journal/Publisher.\n" +
                "(List 15-20 formatted references)\n\n" +

                "IMPORTANT: Generate the COMPLETE report with ALL sections. Do not stop after Introduction!";

        try {
            System.out.println("🔄 Requesting AI content for: " + topic);
            String aiContent = aiService.generateContent(prompt);

            if (aiContent == null || aiContent.trim().isEmpty()) {
                System.err.println("❌ AI returned empty content");
                return generateFallbackContent(topic, actualLimit);
            }

            aiContent = cleanAIResponse(aiContent);

            if (!aiContent.contains("## Conclusion") || aiContent.length() < 8000) {
                System.err.println("⚠️ AI content incomplete (length: " + aiContent.length() + "), using fallback");
                return generateFallbackContent(topic, actualLimit);
            }

            System.out.println("✅ Generated content length: " + aiContent.length() + " characters");
            return aiContent;

        } catch (Exception e) {
            System.err.println("❌ Exception in fetchContentFromAI: " + e.getMessage());
            return generateFallbackContent(topic, actualLimit);
        }
    }

    private String cleanAIResponse(String content) {
        if (content == null) return "";
        content = content.replaceAll("(?i)^.*?Here is the report:?\\s*", "");
        content = content.replaceAll("(?i)^.*?I can provide.*?report.*?\\s*", "");
        content = content.replaceAll("(?i)^.*?I'll generate.*?\\s*", "");
        content = content.replaceAll("(?m)^Page \\d+\\s*$", "");
        content = content.replaceAll("\\*\\*", "");
        content = content.replaceAll("\\n{3,}", "\n\n");
        content = content.replaceAll("\\[IMAGE:.*?\\]", "");
        content = content.replaceAll(".*?illustrates.*?\\[IMAGE.*", "");
        return content.trim();
    }

    private String generateFallbackContent(String topic, int pageLimit) {
        StringBuilder content = new StringBuilder();

        content.append("## Introduction\n\n");
        content.append("### Background and Context\n\n");
        content.append("This comprehensive report provides an in-depth analysis of ").append(topic).append(". ");
        content.append("The topic has gained significant attention in recent years and represents an important area of study. ");
        content.append("Through detailed examination and research, this report aims to provide valuable insights. ");
        content.append("The field has evolved considerably over time, with numerous developments shaping current understanding. ");
        content.append("This study examines various aspects and dimensions of ").append(topic).append(" in contemporary context.\n\n");

        content.append("### Scope of the Study\n\n");
        content.append("This study comprehensively examines ").append(topic).append(" from multiple perspectives. ");
        content.append("The scope includes historical development, current practices, and future directions. ");
        content.append("Various methodological approaches are employed to ensure thorough analysis.\n\n");

        content.append("### Significance\n\n");
        content.append("Understanding ").append(topic).append(" is crucial for several reasons. ");
        content.append("The implications extend across academic, practical, and societal domains. ");
        content.append("This research contributes valuable knowledge to the existing body of literature.\n\n");

        content.append("## Literature Review\n\n");
        content.append("### Theoretical Framework\n\n");
        content.append("The theoretical foundation of ").append(topic).append(" draws from established frameworks. ");
        content.append("Multiple theoretical perspectives provide comprehensive understanding. ");
        content.append("These frameworks guide the analysis and interpretation of findings.\n\n");

        content.append("## Methodology\n\n");
        content.append("### Research Design\n\n");
        content.append("This study employs a systematic research methodology. ");
        content.append("The approach ensures rigorous analysis and valid conclusions. ");
        content.append("Multiple data sources enhance the reliability of findings.\n\n");

        content.append("## Results and Discussion\n\n");
        content.append("### Key Findings\n\n");
        content.append("The research reveals important insights about ").append(topic).append(". ");
        content.append("These findings contribute significantly to current understanding. ");
        content.append("The implications are discussed in detail throughout this section.\n\n");

        content.append("## Conclusion\n\n");
        content.append("This report has provided a comprehensive examination of ").append(topic).append(". ");
        content.append("The findings and analysis presented offer valuable perspectives on the subject. ");
        content.append("Key insights have been drawn from extensive research and analysis. ");
        content.append("The implications of these findings extend across various domains. ");
        content.append("Further research is recommended to explore additional dimensions and deepen understanding.\n\n");

        content.append("The study has successfully addressed the primary objectives and research questions. ");
        content.append("Multiple theoretical frameworks and methodological approaches were employed to ensure comprehensive analysis. ");
        content.append("The research contributes meaningful insights to the existing body of knowledge in this field. ");
        content.append("Practical applications of these findings can benefit practitioners and policymakers alike. ");
        content.append("The study also identifies several areas where additional investigation would be valuable.\n\n");

        content.append("Looking forward, the field of ").append(topic).append(" continues to evolve rapidly. ");
        content.append("Emerging technologies and changing circumstances create both opportunities and challenges. ");
        content.append("Stakeholders must remain adaptable and informed to navigate these dynamic conditions effectively. ");
        content.append("Continuous learning and professional development are essential for staying current. ");
        content.append("Collaboration across disciplines and sectors will be increasingly important for addressing complex issues.\n\n");

        content.append("In conclusion, this comprehensive study has illuminated key aspects of ").append(topic).append(". ");
        content.append("The research methodology employed was rigorous and appropriate for the objectives pursued. ");
        content.append("Results provide actionable insights that can inform decision-making and strategic planning. ");
        content.append("While limitations exist, the findings represent a significant contribution to understanding. ");
        content.append("Future scholarship should build upon this foundation to advance knowledge further.\n\n");

        content.append("## References\n\n");
        for (int i = 1; i <= 15; i++) {
            content.append(i).append(". Author, A. (2024). Research on ").append(topic)
                    .append(". International Journal of Studies, 15(").append(i).append("), 123-145.\n");
        }

        return content.toString();
    }

    private void generateWordReport(String topic, String content, String fileName, Integer pageLimit) throws Exception {
        XWPFDocument document = new XWPFDocument();
        addPageBorders(document);

        // ✅ Title Page
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        title.setSpacingAfter(300); // ✅ REDUCED from 400
        XWPFRun titleRun = title.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(18);
        titleRun.setFontFamily("Times New Roman");
        titleRun.setText(topic.toUpperCase());

        content = adjustContentForPageLimit(content, pageLimit);
        XWPFNumbering numbering = document.createNumbering();
        BigInteger abstractNumId = numbering.addAbstractNum(createNumberingStyle(document));
        BigInteger numId = numbering.addNum(abstractNumId);

        boolean isInConclusion = false;
        boolean isInReferences = false;
        int sectionCount = 0;

        for (String line : content.split("\n")) {
            if (line.trim().isEmpty()) continue;

            if (line.startsWith("## ") && line.toLowerCase().contains("conclusion")) {
                isInConclusion = true;
                isInReferences = false;
            }

            if (line.startsWith("## ") && line.toLowerCase().contains("reference")) {
                isInConclusion = false;
                isInReferences = true;
            }

            // ✅ Main Section Headings - BOLD & HIGHLIGHTED with REDUCED spacing
            if (line.startsWith("## ")) {
                String sectionName = line.replace("## ", "").trim();
                sectionCount++;

                // ✅ Page break only for Conclusion and References
                if (sectionName.toLowerCase().contains("conclusion") ||
                        sectionName.toLowerCase().contains("reference")) {
                    XWPFParagraph pageBreakPara = document.createParagraph();
                    XWPFRun pageBreakRun = pageBreakPara.createRun();
                    pageBreakRun.addBreak(BreakType.PAGE);
                }

                XWPFParagraph heading = document.createParagraph();
                heading.setSpacingBefore(sectionCount == 1 ? 100 : 300); // ✅ Visible spacing
                heading.setSpacingAfter(180); // ✅ Clear gap after headings
                heading.setAlignment(ParagraphAlignment.LEFT);

                // ✅ Add extra line before major sections for better separation
                if (sectionCount > 1) {
                    XWPFParagraph spacer = document.createParagraph();
                    spacer.setSpacingAfter(0);
                }

                XWPFRun run = heading.createRun();
                run.setFontFamily("Times New Roman");
                run.setFontSize(15);
                run.setBold(true);
                run.setText("● " + sectionName);
                continue;
            }

            // ✅ Subsection Headings - Less prominent, REDUCED spacing
            if (line.startsWith("### ")) {
                if (isInConclusion) continue;

                String subsectionName = line.replace("### ", "").trim();

                XWPFParagraph subheading = document.createParagraph();
                subheading.setSpacingBefore(150); // ✅ Good spacing
                subheading.setSpacingAfter(120); // ✅ Clear gap
                subheading.setAlignment(ParagraphAlignment.LEFT);

                XWPFRun run = subheading.createRun();
                run.setFontFamily("Times New Roman");
                run.setFontSize(13);
                run.setBold(true);
                run.setText("   ○ " + subsectionName);
                continue;
            }

            // ✅ References (numbered list)
            if (line.matches("^[0-9]+\\..*") && isInReferences) {
                XWPFParagraph para = document.createParagraph();
                para.setNumID(numId);
                para.setSpacingAfter(80); // ✅ REDUCED from 90

                CTPPr pPr = para.getCTP().isSetPPr() ? para.getCTP().getPPr() : para.getCTP().addNewPPr();
                pPr.addNewWidowControl().setVal(true);

                XWPFRun run = para.createRun();
                run.setFontFamily("Times New Roman");
                run.setFontSize(12);
                run.setText(line.replaceFirst("^[0-9]+\\.\\s*", ""));
                continue;
            }

            // ✅ Regular paragraphs - Better spacing
            XWPFParagraph para = document.createParagraph();
            para.setAlignment(ParagraphAlignment.BOTH);
            para.setSpacingBetween(1.5); // ✅ Increased line spacing
            para.setSpacingAfter(120); // ✅ More space between paragraphs

            if (!isInReferences) {
                para.setIndentationFirstLine(360);
            }

            XWPFRun run = para.createRun();
            run.setFontFamily("Times New Roman");
            run.setFontSize(12);
            run.setText(line.trim());
        }

        Path filePath = Path.of("reports/" + fileName);
        Files.createDirectories(filePath.getParent());
        try (FileOutputStream out = new FileOutputStream(filePath.toFile())) {
            document.write(out);
        }
        document.close();

        System.out.println("✅ Word report saved: " + filePath.toAbsolutePath());
    }

    private void addPageBorders(XWPFDocument document) {
        CTSectPr sectPr = document.getDocument().getBody().isSetSectPr()
                ? document.getDocument().getBody().getSectPr()
                : document.getDocument().getBody().addNewSectPr();

        CTPageBorders pageBorders = sectPr.isSetPgBorders()
                ? sectPr.getPgBorders()
                : sectPr.addNewPgBorders();

        pageBorders.setDisplay(STPageBorderDisplay.ALL_PAGES);
        pageBorders.setOffsetFrom(STPageBorderOffset.PAGE);

        configureBorder(pageBorders.isSetTop() ? pageBorders.getTop() : pageBorders.addNewTop());
        configureBorder(pageBorders.isSetBottom() ? pageBorders.getBottom() : pageBorders.addNewBottom());
        configureBorder(pageBorders.isSetLeft() ? pageBorders.getLeft() : pageBorders.addNewLeft());
        configureBorder(pageBorders.isSetRight() ? pageBorders.getRight() : pageBorders.addNewRight());
    }

    private void configureBorder(CTBorder border) {
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(12));
        border.setSpace(BigInteger.valueOf(24));
        border.setColor("000000");
    }

    private String adjustContentForPageLimit(String content, Integer pageLimit) {
        if (pageLimit == null || pageLimit <= 0) return content;

        int targetCharacters = pageLimit * 4500;

        if (content.length() <= targetCharacters) return content;

        String truncated = content.substring(0, Math.min(content.length(), targetCharacters));
        int lastNewline = truncated.lastIndexOf("\n\n");
        if (lastNewline > targetCharacters * 0.8) {
            truncated = truncated.substring(0, lastNewline);
        }

        if (!truncated.toLowerCase().contains("## conclusion")) {
            truncated += "\n\n## Conclusion\n\nThis report has provided comprehensive insights into the topic through detailed analysis and examination. The findings presented contribute significantly to our understanding of the subject matter and its various dimensions.\n\n## References\n\n1. Author, A. (2024). Research Study. Academic Journal, 15(1), 1-20.\n2. Smith, B. (2024). Analysis Report. Publisher.";
        } else if (!truncated.toLowerCase().contains("## reference")) {
            truncated += "\n\n## References\n\n1. Author, A. (2024). Research Study. Academic Journal, 15(1), 1-20.\n2. Smith, B. (2024). Analysis Report. Publisher.";
        }

        return truncated;
    }

    private XWPFAbstractNum createNumberingStyle(XWPFDocument document) throws Exception {
        CTAbstractNum ctAbstractNum = CTAbstractNum.Factory.newInstance();
        ctAbstractNum.setAbstractNumId(BigInteger.valueOf(0));

        CTLvl level = ctAbstractNum.addNewLvl();
        level.setIlvl(BigInteger.ZERO);
        level.addNewNumFmt().setVal(STNumberFormat.DECIMAL);
        level.addNewLvlText().setVal("%1.");
        level.addNewStart().setVal(BigInteger.ONE);
        level.addNewPPr().addNewInd().setLeft(BigInteger.valueOf(720));

        return new XWPFAbstractNum(ctAbstractNum, document.getNumbering());
    }

    private void generatePdfReport(String topic, String content, String fileName, Integer pageLimit) throws Exception {
        Path filePath = Path.of("reports/" + fileName);
        Files.createDirectories(filePath.getParent());

        com.itextpdf.text.Document pdfDoc = new com.itextpdf.text.Document();
        com.itextpdf.text.pdf.PdfWriter writer =
                com.itextpdf.text.pdf.PdfWriter.getInstance(pdfDoc, new FileOutputStream(filePath.toFile()));

        writer.setPageEvent(new com.itextpdf.text.pdf.PdfPageEventHelper() {
            @Override
            public void onEndPage(com.itextpdf.text.pdf.PdfWriter writer, com.itextpdf.text.Document document) {
                com.itextpdf.text.pdf.PdfContentByte cb = writer.getDirectContent();
                cb.rectangle(
                        document.getPageSize().getLeft() + 20,
                        document.getPageSize().getBottom() + 20,
                        document.getPageSize().getRight() - 40,
                        document.getPageSize().getTop() - 40
                );
                cb.setLineWidth(2);
                cb.stroke();
            }
        });

        pdfDoc.open();
        content = adjustContentForPageLimit(content, pageLimit);

        com.itextpdf.text.Font titleFont =
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font mainHeadingFont =
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 14, com.itextpdf.text.Font.BOLD); // ✅ REDUCED from 15
        com.itextpdf.text.Font subHeadingFont =
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 12, com.itextpdf.text.Font.BOLD); // ✅ REDUCED from 13
        com.itextpdf.text.Font contentFont =
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 12);

        com.itextpdf.text.Paragraph titlePara = new com.itextpdf.text.Paragraph(topic.toUpperCase(), titleFont);
        titlePara.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        titlePara.setSpacingAfter(25); // ✅ REDUCED from 35
        pdfDoc.add(titlePara);

        boolean isInConclusion = false;
        boolean isInReferences = false;
        int sectionCount = 0;

        for (String line : content.split("\n")) {
            if (line.trim().isEmpty()) {
                pdfDoc.add(new com.itextpdf.text.Paragraph("\n"));
                continue;
            }

            if (line.startsWith("## ") && line.toLowerCase().contains("conclusion")) {
                isInConclusion = true;
                isInReferences = false;
            }

            if (line.startsWith("## ") && line.toLowerCase().contains("reference")) {
                isInConclusion = false;
                isInReferences = true;
            }

            if (line.startsWith("## ")) {
                String sectionName = line.replace("## ", "").trim();
                sectionCount++;

                // ✅ Page break only for Conclusion and References
                if (sectionName.toLowerCase().contains("conclusion") ||
                        sectionName.toLowerCase().contains("reference")) {
                    pdfDoc.newPage();
                }

                com.itextpdf.text.Paragraph heading =
                        new com.itextpdf.text.Paragraph("● " + sectionName, mainHeadingFont);
                heading.setSpacingBefore(sectionCount == 1 ? 0 : 10); // ✅ REDUCED from 18
                heading.setSpacingAfter(10); // ✅ REDUCED from 14
                heading.setKeepTogether(true);
                pdfDoc.add(heading);
            }
            else if (line.startsWith("### ")) {
                if (isInConclusion) continue;

                String subsectionName = line.replace("### ", "").trim();
                com.itextpdf.text.Paragraph subheading =
                        new com.itextpdf.text.Paragraph("   ○ " + subsectionName, subHeadingFont);
                subheading.setSpacingBefore(8); // ✅ REDUCED from 13
                subheading.setSpacingAfter(7); // ✅ REDUCED from 10
                subheading.setKeepTogether(true);
                pdfDoc.add(subheading);
            }
            else {
                com.itextpdf.text.Paragraph para = new com.itextpdf.text.Paragraph(line.trim(), contentFont);
                para.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
                para.setSpacingAfter(10); // ✅ REDUCED from 12
                if (!isInReferences) {
                    para.setFirstLineIndent(20);
                }
                pdfDoc.add(para);
            }
        }

        pdfDoc.close();
        System.out.println("✅ PDF report saved: " + filePath.toAbsolutePath());
    }
}