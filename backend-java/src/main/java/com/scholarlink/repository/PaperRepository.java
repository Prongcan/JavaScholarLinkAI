package com.scholarlink.repository;

import com.scholarlink.entity.Paper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaperRepository extends JpaRepository<Paper, Integer> {
    
    Optional<Paper> findByTitle(String title);
    
    Page<Paper> findAllByOrderByPaperIdDesc(Pageable pageable);
}

