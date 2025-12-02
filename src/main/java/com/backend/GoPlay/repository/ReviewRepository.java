package com.backend.GoPlay.repository;



import com.backend.GoPlay.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Page<Review> findByCourtId(Integer courtId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.court.id = :courtId")
    Double calculateAverageRating(Integer courtId);
}
