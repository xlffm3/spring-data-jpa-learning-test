package com.learning.jpa.domain;

import static org.assertj.core.api.Assertions.*;

import com.learning.jpa.domain.post.Comment;
import com.learning.jpa.domain.post.Post;
import com.learning.jpa.domain.post.PostRepository;
import com.learning.jpa.domain.post.Tag;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.hibernate.loader.MultipleBagFetchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @BeforeEach
    void setUp() {
        Post post = new Post("hello this is jpa test!");
        Comment comment = new Comment("hi this is amazing!");
        Tag tag = new Tag("abc");
        post.addComment(comment);
        post.addTag(tag);

        Post post2 = new Post("hello this is another post!");
        Comment comment2 = new Comment("hi this is another comment!");
        Tag tag2 = new Tag("def");
        post2.addComment(comment2);
        post2.addTag(tag2);

        Post post3 = new Post("hello this is another post!");
        Comment comment3 = new Comment("hi this is another comment!");
        Tag tag3 = new Tag("gka");
        post3.addComment(comment3);
        post3.addTag(tag3);

        postRepository.save(post);
        postRepository.save(post2);
        postRepository.save(post3);

        testEntityManager.flush();
        testEntityManager.clear();
    }

    @DisplayName("글로벌 지연 전략으로 인해 N + 1 쿼리가 발생한다.")
    @Test
    void findPostsCausingNPlusOne() {
        System.out.println("========= find =========\n\n\n");
        List<Post> posts = postRepository.findAll();

        assertThat(posts).hasSize(3);

        for (Post p : posts) {
            System.out.println(p.getComments().get(0).getContent());
        }

        assertThat(posts).hasSize(3);
    }

    @DisplayName("일반적인 Fetch Join 사용시 내부 조인이 발생한다.")
    @Test
    void findPostsUsingInnerFetchJoin() {
        System.out.println("========= find =========\n\n\n");
        postRepository.save(new Post("dummy post"));
        List<Post> posts = postRepository.findAllInnerFetchJoin();

        assertThat(posts).hasSize(3);

        for (Post p : posts) {
            System.out.println(p.getComments().get(0).getContent());
        }
    }

    @DisplayName("명시함으로써 Fetch Join에 외부 조인이 발생한다.")
    @Test
    void findPostsUsingOuterFetchJoin() {
        System.out.println("========= find =========\n\n\n");
        postRepository.save(new Post("dummy post"));
        List<Post> posts = postRepository.findAllOuterFetchJoin();

        assertThat(posts).hasSize(4);
        assertThatCode(() -> {
            for (Post p : posts) {
                System.out.println(p.getComments().get(0).getContent());
            }
        }).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @DisplayName("EntityGraph를 통해 연관 엔티티들을 즉시 로딩한다.")
    @Test
    void findPostsUsingEntityGraph() {
        System.out.println("========= find =========\n\n\n");
        postRepository.save(new Post("dummy post"));
        List<Post> posts = postRepository.findAllEntityGraph();

        assertThat(posts).hasSize(4);
        assertThatCode(() -> {
            for (Post p : posts) {
                System.out.println(p.getComments().get(0).getContent());
            }
        }).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @DisplayName("EntityGraph와 Subgraph를 통해 연관 엔티티들을 즉시 로딩한다.")
    @Test
    void findPostsUsingEntityGraphWithSubGraph() {
        System.out.println("========= find =========\n\n\n");
        postRepository.save(new Post("dummy post"));
        List<Post> posts = postRepository.findAllEntityGraphWithSubGraph();

        assertThat(posts).hasSize(4);
        assertThatCode(() -> {
            for (Post p : posts) {
                System.out.println(p.getComments().get(0).getContent());
            }
        }).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @DisplayName("일반적인 Fetch Join으로 조회시 카테시안 곱이 발생한다.")
    @Test
    void findCartesianProduct() {
        postRepository.deleteAll();

        for (int i = 0; i < 10; i++) {
            Post post = new Post("dummy post");
            Comment comment1 = new Comment("hi");
            Comment comment2 = new Comment("hi2");
            post.addComment(comment1);
            post.addComment(comment2);
            postRepository.save(post);
        }

        testEntityManager.flush();
        testEntityManager.clear();

        List<Post> posts = postRepository.findAllInnerFetchJoin();

        assertThat(posts).hasSize(20);
    }

    @DisplayName("distinct 키워드를 통해 컬렉션 Fetch Join 중복을 제거한다.")
    @Test
    void removeCartesianProduct() {
        postRepository.deleteAll();

        for (int i = 0; i < 10; i++) {
            Post post = new Post("dummy post");
            Comment comment1 = new Comment("hi");
            Comment comment2 = new Comment("hi2");
            post.addComment(comment1);
            post.addComment(comment2);
            postRepository.save(post);
        }

        testEntityManager.flush();
        testEntityManager.clear();

        List<Post> posts = postRepository.findAllInnerFetchJoinWithDistinct();

        assertThat(posts).hasSize(10);
    }

    @DisplayName("2개 이상의 컬렉션은 Fetch Join할 수 없다.")
    @Test
    void cannotFetchJoinMoreThanTwoCollections() {
        EntityManager entityManager = testEntityManager.getEntityManager();

        assertThatCode(() -> {
            entityManager.createQuery("select p from Post p join fetch p.comments join fetch p.tags", Post.class);
        }).hasRootCauseInstanceOf(MultipleBagFetchException.class);
    }

    @DisplayName("컬렉션을 Fetch Join하는 경우 페이징시 경고가 발생한다.")
    @Test
    void findPagingWithFetchJoin() {
        postRepository.findAllPagingWithFetchJoin(PageRequest.of(0, 2));
    }

    /*
    application.properties 옵션의 주석을 해제해야 한다.
     */
    @DisplayName("Batch를 통해 여러 컬렉션에 대한 N + 1 쿼리를 단 3개로 줄인다.")
    @Test
    void findUsingBatch() {
        System.out.println("========= find =========\n\n\n");
        List<Post> posts = postRepository.findAllWithBatchSize();

        assertThat(posts).hasSize(3);

        for (Post p : posts) {
            System.out.println(p.getComments().get(0).getContent());
            System.out.println(p.getTags().get(0).getName());
        }

        assertThat(posts).hasSize(3);
    }

    @DisplayName("inner join일 때 우측 데이터에 값이 없으면 찾을 수 없다.")
    @Test
    void cannotFindPost() {
        postRepository.deleteAll();
        Post save = postRepository.save(new Post("hi"));

        testEntityManager.flush();
        testEntityManager.clear();

        Optional<Post> post = postRepository.findByIdWithInnerJoin(save.getId());

        assertThat(post).isEmpty();
    }
}
