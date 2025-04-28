package group4.opensource_server.OCR.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class IdentifyFileService {

    public int getType(File example) {
        if (example == null || !example.exists()) {
            throw new IllegalArgumentException("파일이 존재하지 않거나 유효하지 않습니다.");
        }

        try (FileInputStream fis = new FileInputStream(example)) {
            byte[] header = new byte[4];
            if (fis.read(header) != -1) {
                // JPEG 또는 JPG 파일 확인
                if ((header[0] == (byte) 0xFF && header[1] == (byte) 0xD8) ||
                        (header[0] == (byte) 0xFF && header[1] == (byte) 0xE0)) {
                    return 1; // JPEG 형식
                }
                // PNG 파일 확인
                if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 &&
                        header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
                    return 2; // PNG 형식
                }
                // PDF 파일 확인
                if (header[0] == (byte) 0x25 && header[1] == (byte) 0x50 &&
                        header[2] == (byte) 0x44 && header[3] == (byte) 0x46) {
                    return 3; // PDF 형식
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0; // 알 수 없는 형식
    }
}
