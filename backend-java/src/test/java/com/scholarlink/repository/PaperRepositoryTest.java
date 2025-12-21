package com.scholarlink.repository;

import com.scholarlink.entity.Paper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PaperRepositoryTest {

    @Autowired
    private PaperRepository paperRepository;

    @BeforeEach
    void setUp() {
        paperRepository.deleteAll();
    }

    @Test
    void testSavePaper() {
        Paper paper = new Paper();
        paper.setTitle("Test Paper");
        paper.setAuthor("Test Author");
        paper.setAbstractText("Test Abstract");
        paper.setPdfUrl("http://example.com/paper.pdf");

        Paper saved = paperRepository.save(paper);

        assertNotNull(saved.getPaperId());
        assertEquals("Test Paper", saved.getTitle());
    }

    @Test
    void testFindByTitle() {
        Paper paper = new Paper();
        paper.setTitle("Unique Title");
        paper.setAuthor("Author");
        paperRepository.save(paper);

        Optional<Paper> found = paperRepository.findByTitle("Unique Title");

        assertTrue(found.isPresent());
        assertEquals("Unique Title", found.get().getTitle());
    }

    @Test
    void testFindAllByOrderByPaperIdDesc() {
        // 创建多个论文
        for (int i = 1; i <= 5; i++) {
            Paper paper = new Paper();
            paper.setTitle("Paper " + i);
            paper.setAuthor("Author " + i);
            paperRepository.save(paper);
        }

        Page<Paper> page = paperRepository.findAllByOrderByPaperIdDesc(
                PageRequest.of(0, 10));

        assertEquals(5, page.getTotalElements());
        // 验证排序（最新的在前）
        assertTrue(page.getContent().get(0).getPaperId() > 
                   page.getContent().get(4).getPaperId());
    }
}

