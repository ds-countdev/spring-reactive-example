package com.springboot.reactive.mscv.app.reactor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.springboot.reactive.mscv.app.model.Comment;
import com.springboot.reactive.mscv.app.model.User;
import com.springboot.reactive.mscv.app.model.UserComments;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReactorMethodsTwo {

    private final Logger log = LoggerFactory.getLogger(ReactorMethodsTwo.class);

    public void iterable(List<?> values) {

        Flux.fromIterable(values)
                .doOnNext(value -> {
                    if (!Optional.ofNullable(value).isPresent())
                        throw new RuntimeException("thers not a value");

                    else
                        log.info(String.format("theres a value : %s", value.toString()));
                })
                .map(value -> {
                    try {
                        return value.toString();
                    } catch (Exception exception) {
                        log.error("there was an error", exception);
                        throw new RuntimeException("theres an error");
                    }
                })
                .subscribe(value -> {
                    log.info("the value is ".concat(value)
                            .concat(" and is ").concat(value.getClass().toString()));
                },
                        error -> log.error(error.getMessage()),

                        new Runnable() {
                            @Override
                            public void run() {
                                log.info("the execution has finalized");
                            }
                        });

    }

    public void iterableFlatMapName(List<?> values) {
        var userFlux = Flux.fromIterable(values)
                .map(value -> {
                    if (!Optional.ofNullable(value).isPresent())
                        throw new RuntimeException("value is null");
                    else
                        return value.toString();
                })
                .doOnNext(value -> log.info("string value one :" + value))
                .map(value -> new User(value.split(" ")[0], value.split(" ")[1]))
                .doOnNext(user -> log.info("user :" + user))
                .flatMap(user -> {
                    if (user.getName().equalsIgnoreCase("diego"))
                        return Mono.just(user);
                    else
                        return Mono.empty();
                })
                .subscribe(user -> log.info("user : " + user));

        System.out.println(userFlux.getClass());
    }

    public List<User> userConverter(List<?> values) {

        return values.stream()
                .map(value -> {
                    if (!Optional.ofNullable(value).isPresent() && !(value instanceof String))
                        throw new RuntimeException("value is null");

                    else
                        return new User(value.toString().split(" ")[0], value.toString().split(" ")[1]);
                }).toList();
    }

    public void userCommentsMonoFlatMap() {

        var monoUser = Flux.just(
                new User("Diego", "Doe"), new User("Alejandro", "Doe"));

        var comments = Mono.fromCallable(() -> {
            return new Comment()
                    .addComments("hello")
                    .addComments("whats up");
        });

        monoUser.flatMap(user -> comments.map(comment -> new UserComments(user, comment)))
                .subscribe(userComment -> log.info(userComment.toString()));
    }

    public void userCommentsZipWith() {

        var monoUser = Flux.just(
                new User("Diego", "Doe"), new User("Alejandro", "Doe"));

        var comments = Mono.fromCallable(() -> {
            return new Comment()
                    .addComments("hello")
                    .addComments("whats up");
        });


        monoUser.zipWith(comments, (user, comment) 
            ->  new UserComments(user, comment))
        .subscribe(value -> log.info(value.toString()));

    }

}
