package com.learning.jpa.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "like_id")
    private Like like;

    protected Comment() {
    }

    public Comment(String content) {
        this.content = content;
    }

    public void toPost(Post post) {
        this.post = post;
    }

    public String getContent() {
        return content;
    }

    public Post getPost() {
        return post;
    }

    public Like getLike() {
        return like;
    }
}
