package com.learning.jpa.domain.article;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "writer_id", nullable = false)
    private Writer writer;

    protected Article() {
    }

    public Article(Category category, Subject subject, Writer writer) {
        this.category = category;
        this.subject = subject;
        this.writer = writer;
    }

    public Long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public Subject getSubject() {
        return subject;
    }

    public Writer getWriter() {
        return writer;
    }
}
