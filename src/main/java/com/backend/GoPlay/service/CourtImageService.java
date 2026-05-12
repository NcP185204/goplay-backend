package com.backend.GoPlay.service;

import com.backend.GoPlay.model.CourtImage;
import com.backend.GoPlay.model.User;
import org.springframework.web.multipart.MultipartFile;

public interface CourtImageService {
    CourtImage uploadCourtImage(Integer courtId, MultipartFile file, User manager);
    void deleteCourtImage(Integer courtId, Integer imageId, User manager);
    void setThumbnail(Integer courtId, Integer imageId, User manager);
}
