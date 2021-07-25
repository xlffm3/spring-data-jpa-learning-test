package com.learning.jpa.domain;

import java.util.List;
import java.util.Optional;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select p from Post p join fetch p.comments")
    List<Post> findAllInnerFetchJoin();

    @Query("select distinct p from Post p join fetch p.comments")
    List<Post> findAllInnerFetchJoinWithDistinct();

    @Query("select p from Post p left join fetch p.comments")
    List<Post> findAllOuterFetchJoin();

    @EntityGraph(attributePaths = "comments")
    @Query("select p from Post p")
    List<Post> findAllEntityGraph();

    @EntityGraph(attributePaths = {"comments", "comments.like"})
    @Query("select p from Post p")
    List<Post> findAllEntityGraphWithSubGraph();

    @Query("select p from Post p join fetch p.comments")
    List<Post> findAllPagingWithFetchJoin(Pageable pageable);

    @BatchSize(size = 1000)
    @Query("select p from Post p join fetch p.comments")
    List<Post> findAllWithBatchSize();

    @Query("select p from Post p join fetch p.comments where p.id = :id")
    Optional<Post> findByIdWithInnerJoin(@Param("id") Long id);
}
