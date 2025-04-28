package group4.opensource_server.OCR.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GetImageFromPdf {
    public List<byte []> extractImageFromPdf(File file) throws IOException {
        List<byte[]> imageList = new ArrayList<>();

        PDDocument document = Loader.loadPDF(file);
        PDFRenderer renderer = new PDFRenderer(document);
        int count = document.getNumberOfPages();

        for (int i = 0; i< count; i++) {
            BufferedImage image = renderer.renderImageWithDPI(i, 300);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", byteArrayOutputStream);  // PNG 포맷으로 저장
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            imageList.add(imageBytes);
        }

        return imageList;
    }
}
