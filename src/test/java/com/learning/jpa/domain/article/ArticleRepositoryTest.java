package com.learning.jpa.domain.article;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @DisplayName("Optional과 Nullable 여부에 따라 즉시 로딩시 Join의 차이를 확인한다.")
    @Test
    void checkJoin() {
        Article save = articleRepository
            .save(new Article(new Category(), new Subject(), new Writer()));

        testEntityManager.flush();
        testEntityManager.clear();

        System.out.println("========= find =========");
        articleRepository.findById(save.getId());
    }

    @DisplayName("Fetch Join의 경우 명시하지 않으면 무조건 Inner Join이 발생한다.")
    @Test
    void checkFetchJoin() {
        articleRepository.findAllWithFetchJoin();
    }

    @DisplayName("일대다 관계의 컬렉션을 즉시 로딩할 때는 외부 조인이 적용된다.")
    @Test
    void checkLeftEagerJoin() {
        Subject save = subjectRepository.save(new Subject());

        testEntityManager.flush();
        testEntityManager.clear();

        System.out.println("========= find =========");
        subjectRepository.findById(save.getId());
    }
}
