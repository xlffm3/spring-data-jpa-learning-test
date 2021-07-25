package com.learning.jpa.domain.article;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "subject", fetch = FetchType.EAGER)
    private List<Article> articles = new ArrayList<>();

    protected Subject() {
    }

    public void hi() {
        System.out.println("hi");
    }

    public Long getId() {
        return id;
    }
}
