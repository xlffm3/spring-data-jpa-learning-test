package com.learning.jpa.domain.family;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Parent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Child> children = new ArrayList<>();

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<Orphan> orphans = new ArrayList<>();

    protected Parent() {
    }

    public Parent(String name) {
        this.name = name;
    }

    public void add(Child child) {
        children.add(child);
    }

    public void remove(Child child) {
        children.remove(child);
    }

    public void add(Orphan orphan) {
        orphans.add(orphan);
    }

    public void remove(Orphan orphan) {
        orphans.remove(orphan);
    }

    public Long getId() {
        return id;
    }

    public List<Child> getChildren() {
        return children;
    }

    public List<Orphan> getOrphans() {
        return orphans;
    }
}
