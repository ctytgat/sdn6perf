package com.example.demo.model;

import org.neo4j.ogm.annotation.NodeEntity;
import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
import java.util.List;

@Node("Movie")
@NodeEntity
public class Movie {

    @Id
    @GeneratedValue
    @org.neo4j.ogm.annotation.Id
    @org.neo4j.ogm.annotation.GeneratedValue
    private Long id;

    @Property("name")
    private String name;

    @Property("description")
    private String description;

    @Relationship(type = "ACTOR")
    @org.neo4j.ogm.annotation.Relationship("ACTOR")
    private List<Actor> actors = new ArrayList<>();

    public Movie() {
    }

    public Movie(String name, String description) {
        this.name = name;
        this.name = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Actor> getActors() {
        return actors;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }
}