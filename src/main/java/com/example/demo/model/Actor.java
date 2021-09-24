package com.example.demo.model;

import org.neo4j.ogm.annotation.NodeEntity;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.schema.Relationship.Direction;

import java.util.ArrayList;
import java.util.List;

@Node("Actor")
@NodeEntity
public class Actor {

    @Id
    @GeneratedValue
    @org.neo4j.ogm.annotation.Id
    @org.neo4j.ogm.annotation.GeneratedValue
    private Long id;

    @Property
    private String name;

    @Relationship(type = "HOBBY")
    @org.neo4j.ogm.annotation.Relationship("HOBBY")
    private List<Hobby> hobbies = new ArrayList<>();

    public Actor() {
    }

    public Actor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Hobby> getHobbies() {
        return hobbies;
    }

    public void setHobbies(List<Hobby> hobbies) {
        this.hobbies = hobbies;
    }
}