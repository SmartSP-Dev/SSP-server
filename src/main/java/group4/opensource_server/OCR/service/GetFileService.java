package group4.opensource_server.OCR.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class GetFileService {

    private static final String temp_dir = "./temp_for_upload/";

    public File uploadFile(MultipartFile file) throws IOException {
        // 파일이 비어 있는지 확인
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }

        // 원본 파일명 추출
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일 이름을 가져올 수 없습니다.");
        }

        // 파일 확장자 추출
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        // UUID를 사용하여 고유한 파일명 생성
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // 상대경로를 절대경로로 변환
        String absolutePath = Paths.get(temp_dir).toAbsolutePath().toString();

        // 저장할 디렉토리 객체 생성
        File upload_dir = new File(absolutePath);

        // 디렉토리 존재 여부 확인
        if (!upload_dir.exists()) {
            boolean wasSuccessful = upload_dir.mkdirs();
            if (!wasSuccessful) {
                throw new IOException("파일 저장 디렉토리를 생성하지 못했습니다. 디렉토리 경로: " + absolutePath);
            }
        }

        // 저장할 파일 객체 생성
        File uploadFile = new File(upload_dir, uniqueFilename);

        // 파일 저장 경로 출력
        System.out.println("파일 저장 경로: " + uploadFile.getAbsolutePath());

        // 파일 저장
        try {
            file.transferTo(uploadFile);
        } catch (IOException e) {
            throw new IOException("파일 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }

        return uploadFile;
    }
}
