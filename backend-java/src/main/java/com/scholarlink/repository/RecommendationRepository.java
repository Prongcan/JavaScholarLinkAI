package com.scholarlink.repository;

import com.scholarlink.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Integer> {
    
    List<Recommendation> findByUserId(Integer userId);
    
    List<Recommendation> findByPaperId(Integer paperId);
    
    void deleteByUserId(Integer userId);
}

