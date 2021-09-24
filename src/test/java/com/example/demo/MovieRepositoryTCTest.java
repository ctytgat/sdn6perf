package com.example.demo;

import com.example.demo.model.Actor;
import com.example.demo.model.Hobby;
import com.example.demo.model.Movie;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.MountableFile;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataNeo4jTest
@Slf4j
class MovieRepositoryTCTest {

    private static Neo4jContainer<?> neo4jContainer;

    Faker faker = new Faker();
    private int numMovies;
    private int numHobbies;
    private int numActors;
    private int numHobbiesPerActor;
    private int numActorsPerMovie;

    @BeforeAll
    static void initializeNeo4j() {
        neo4jContainer = new org.testcontainers.containers.Neo4jContainer<>("neo4j:4.2.11").
                withoutAuthentication();
        neo4jContainer.setPortBindings(List.of("7688:7687", "7475:7474"));
        neo4jContainer.withPlugins(MountableFile.forClasspathResource("/apoc-4.2.0.0.jar"));

        log.info("Starting Neo4j");
        neo4jContainer.start();
        log.info("Started Neo4j");
    }

    @AfterAll
    static void stopNeo4j() {
        neo4jContainer.close();
    }

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
    }

    @BeforeEach
    public void populate(@Autowired Neo4jTemplate template) {

        numHobbies = 100;
        numActors = 100;
        numHobbiesPerActor = 20;
        numMovies = 500;
        numActorsPerMovie = 20;

        log.info("Creating hobbies...");

        List<Hobby> hobbies = new ArrayList<>();

        for (int i = 0; i < numHobbies; i++) {
            Hobby hobby = new Hobby(faker.job().title(), faker.lorem().sentence());
            hobbies.add(hobby);
        }

        template.saveAll(hobbies);

        log.info("Creating actors...");

        List<Actor> actors = new ArrayList<>();

        for (int i = 0; i < numActors; i++) {
            Actor actor = new Actor(faker.name().name());
            Collections.shuffle(hobbies);
            actor.getHobbies().addAll(hobbies.subList(0, numHobbiesPerActor));
            actors.add(actor);
        }

        template.saveAll(actors);

        log.info("Creating movies...");

        List<Movie> movies = new ArrayList<>();

        for (int i = 0; i < numMovies; i++) {
            Movie movie = new Movie(faker.funnyName().name(), faker.lorem().sentence());
            Collections.shuffle(actors);
            movie.getActors().addAll(actors.subList(0, numActorsPerMovie));
            movies.add(movie);
        }

        template.saveAll(movies);
        log.info("Setup done");

        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    @Test
    public void testSDN(@Autowired Neo4jTemplate template) {
        log.info("Start fetching movies");
        List<Movie> result = template.findAll("MATCH p=(m:Movie)-[*0..]->() RETURN m, collect(nodes(p)), collect(relationships(p))", Movie.class);
        log.info("Done fetching movies");
        validate(result);
    }

    @Test
    public void testSDNUsingApoc(@Autowired Neo4jTemplate template) {
        log.info("Start fetching movies");
        List<Movie> result = template.findAll("MATCH (m:Movie) CALL apoc.path.subgraphAll(m, {relationshipFilter:'>'}) YIELD nodes, relationships RETURN m, collect(nodes), collect(relationships)", Movie.class);
        log.info("Done fetching movies");
        validate(result);
    }

    @Test
    public void testOGM() {
        Configuration configuration = new Configuration.Builder()
                .uri(neo4jContainer.getBoltUrl())
                .build();

        SessionFactory sessionFactory = new SessionFactory(configuration, "com.example.demo.model");
        Session session = sessionFactory.openSession();

        log.info("Start fetching movies");

        Iterable<Movie> result = session.query(Movie.class, "MATCH p=(m:Movie)-[*0..]->() RETURN m, collect(nodes(p)), collect(relationships(p))", Map.of());
        log.info("Done fetching movies");
        validate(result);
    }

    private void validate(Iterable<Movie> result) {
        assertThat(result).hasSize(numMovies);
        result.forEach(m -> {
            assertThat(m.getActors()).hasSize(numActorsPerMovie);
            m.getActors().forEach(a -> {
                assertThat(a.getHobbies()).hasSize(numHobbiesPerActor);
            });
        });
    }
}