package com.example.demo.model;

import org.neo4j.ogm.annotation.NodeEntity;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Hobby")
@NodeEntity
public class Hobby {

    @Id
    @GeneratedValue
    @org.neo4j.ogm.annotation.Id
    @org.neo4j.ogm.annotation.GeneratedValue
    private Long id;

    @Property
    private String name;

    @Property
    private String description;

    public Hobby() {
    }

    public Hobby(String name, String description) {
        this.name = name;
        this.description = description;
    }
}