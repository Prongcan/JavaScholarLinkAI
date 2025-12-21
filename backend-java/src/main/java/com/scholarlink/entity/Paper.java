package com.scholarlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "papers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paper {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paper_id")
    private Integer paperId;
    
    @Column(name = "title", nullable = false, length = 1000)
    private String title;
    
    @Column(name = "author", length = 1000)
    private String author;
    
    @Column(name = "abstract", columnDefinition = "TEXT")
    private String abstractText;
    
    @Column(name = "pdf_url", length = 512)
    private String pdfUrl;
}

