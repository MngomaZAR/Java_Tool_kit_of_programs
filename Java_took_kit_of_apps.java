import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Controller
public class ConversionController {

    @PostMapping("/calculate")
    public String calculate(@RequestParam("principal") double principal,
                            @RequestParam("rate") double rate,
                            @RequestParam("time") double time,
                            Model model) {
        double interest = calculateInterest(principal, rate, time);
        model.addAttribute("interest", interest);
        return "calculate";
    }

    @PostMapping("/convert")
    public String convert(@RequestParam("pdf") MultipartFile pdfFile, Model model) {
        if (pdfFile.isEmpty()) {
            model.addAttribute("error", "Please upload a PDF file.");
            return "convert";
        }

        try {
            byte[] pdfBytes = pdfFile.getBytes();
            BufferedImage image = convertPdfToImage(pdfBytes);
            String base64Image = convertImageToBase64(image);
            model.addAttribute("base64Image", base64Image);
            return "convert";
        } catch (IOException e) {
            model.addAttribute("error", "An error occurred while converting the PDF file.");
            return "convert";
        }
    }

    private double calculateInterest(double principal, double rate, double time) {
        return principal * rate * time / 100;
    }

    private BufferedImage convertPdfToImage(byte[] pdfBytes) throws IOException {
        try (InputStream pdfInputStream = new ByteArrayInputStream(pdfBytes);
             PDDocument pdfDocument = PDDocument.load(pdfInputStream)) {
            PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
            return pdfRenderer.renderImageWithDPI(0, 300);
        }
    }

    private String convertImageToBase64(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        }
    }
}
