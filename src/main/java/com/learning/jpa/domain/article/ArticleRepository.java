package com.learning.jpa.domain.article;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("select a from Article a join fetch a.category join fetch a.subject join fetch a.writer")
    List<Article> findAllWithFetchJoin();
}
