package org.example.qweralarm.service;

import lombok.RequiredArgsConstructor;
import org.example.qweralarm.entity.AudioFile;
import org.example.qweralarm.repository.AudioFileRepository;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AudioService {

    private final AudioFileRepository audioFileRepository;
    private final String uploadUrl = "D:/uploads/audio/";

    // 오디오 파일 저장 로직
    public AudioFile saveAudioFile(MultipartFile file) throws IOException {
        File dir = new File(uploadUrl);
        if (!dir.exists()) dir.mkdirs();

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File destination = new File(uploadUrl, fileName);
        file.transferTo(destination);

        AudioFile audioFile = new AudioFile();
        audioFile.setFilename(file.getOriginalFilename());
        audioFile.setFile_path(destination.getAbsolutePath());
        audioFile.setContent_type(file.getContentType());

        return audioFileRepository.save(audioFile);
    }

    // 기본 오디오 가져오기
    public AudioFile getDefaultAudio() {
        return audioFileRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("기본 노래를 찾을 수 없습니다"));
    }

    // 오디오 파일 리소스 가져오기 (스트리밍용)
    public AudioFile getAudioFileById(Long audioId) {
        return audioFileRepository.findById(audioId)
                .orElseThrow(() -> new RuntimeException("오디오 파일을 찾을 수 없습니다"));
    }

    public UrlResource getAudioResource(AudioFile audioFile) throws MalformedURLException {
        Path path = Paths.get(audioFile.getFile_path());
        return new UrlResource(path.toUri());
    }
}