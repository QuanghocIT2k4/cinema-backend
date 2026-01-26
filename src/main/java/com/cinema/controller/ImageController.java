package com.cinema.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller để serve ảnh actor từ folder local cinema-data/actor_photos
 */
@Slf4j
@RestController
@RequestMapping("/api/images")
public class ImageController {
    
    /**
     * Tìm folder actor_photos từ nhiều vị trí có thể
     */
    private Path findActorPhotosDirectory() {
        Path[] possiblePaths = {
            Paths.get("cinema-data", "actor_photos"),  // Từ project root
            Paths.get("..", "cinema-data", "actor_photos"),  // Từ cinema-backend folder
            Paths.get(System.getProperty("user.dir"), "cinema-data", "actor_photos"),  // Absolute từ working dir
            Paths.get(System.getProperty("user.dir"), "..", "cinema-data", "actor_photos")  // Absolute từ parent
        };
        
        for (Path path : possiblePaths) {
            if (Files.exists(path) && Files.isDirectory(path)) {
                log.debug("Tìm thấy folder actor_photos tại: {}", path.toAbsolutePath());
                return path;
            }
        }
        
        // Return default path nếu không tìm thấy (sẽ fail sau)
        return Paths.get("cinema-data", "actor_photos");
    }
    
    /**
     * Serve ảnh actor từ folder local
     * URL: /api/images/actors/{filename}
     */
    @GetMapping("/actors/{filename:.+}")
    public ResponseEntity<Resource> getActorImage(@PathVariable String filename) {
        try {
            Path actorPhotosDir = findActorPhotosDirectory();
            Path imagePath = actorPhotosDir.resolve(filename);
            File imageFile = imagePath.toFile();
            
            if (!imageFile.exists() || !imageFile.isFile()) {
                log.warn("Không tìm thấy ảnh: {} (tìm trong: {})", filename, actorPhotosDir.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
            // Kiểm tra file có phải là ảnh không
            String contentType = Files.probeContentType(imagePath);
            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = "image/jpeg"; // Default
            }
            
            Resource resource = new FileSystemResource(imageFile);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Lỗi khi serve ảnh actor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

