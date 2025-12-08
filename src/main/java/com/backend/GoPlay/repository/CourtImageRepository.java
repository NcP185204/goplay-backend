package com.backend.GoPlay.repository;

import com.backend.GoPlay.model.CourtImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourtImageRepository extends JpaRepository<CourtImage, Integer> {
}
