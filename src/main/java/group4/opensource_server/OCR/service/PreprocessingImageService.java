package group4.opensource_server.OCR.service;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

@Service
public class PreprocessingImageService {

    static {
        nu.pattern.OpenCV.loadLocally();
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public BufferedImage preprocess(BufferedImage image) {
        System.out.println("OpenCV 실행 시작");

        Mat mat = bufferedImageToMat(image);

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);

        Imgproc.GaussianBlur(mat, mat, new Size(3, 3), 0);

        Imgproc.threshold(mat, mat, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        return matToBufferedImage(mat);
    }

    private Mat bufferedImageToMat(BufferedImage bi) {
        BufferedImage convertedImg = new BufferedImage(
                bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        convertedImg.getGraphics().drawImage(bi, 0, 0, null);

        byte[] pixels = ((DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(convertedImg.getHeight(), convertedImg.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        return mat;
    }


    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }

        byte[] data = new byte[mat.rows() * mat.cols() * mat.channels()];
        mat.get(0, 0, data);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        return image;
    }
}
