package service;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class DocumentParsingService {
    public String extractTextFromFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IOException("File name is null");
        }

        String extension = getFileExtension(fileName).toLowerCase();

        return switch (extension) {
            case "pdf" -> extractTextFromPDF(file.getInputStream());
            case "docx" -> extractTextFromDocx(file.getInputStream());
            case "txt" -> extractTextFromTxt(file.getInputStream());
            default -> throw new IOException("Unsupported file format: " + extension);
        };
    }

    private String extractTextFromPDF(InputStream inputStream) throws IOException {
        byte[] pdfBytes = IOUtils.toByteArray(inputStream);

        RandomAccessReadBuffer rar = new RandomAccessReadBuffer(pdfBytes);

        PDFParser parser = new PDFParser(rar);
        PDDocument document  = parser.parse();
        try (document) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        } catch (IOException e) {
            log.error("Error loading PDF document: {}", e.getMessage());
            throw new IOException("Failed to load PDF document", e);
        }
    }

    private String extractTextFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractTextFromTxt(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex >= 0 ? fileName.substring(lastDotIndex + 1) : "";
    }
}
