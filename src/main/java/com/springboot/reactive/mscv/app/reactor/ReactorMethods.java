package com.springboot.reactive.mscv.app.reactor;

import java.util.List;
import java.util.Optional;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.springboot.reactive.mscv.app.model.Comment;
import com.springboot.reactive.mscv.app.model.User;
import com.springboot.reactive.mscv.app.model.UserComments;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReactorMethods {

    private final Logger log = LoggerFactory.getLogger(ReactorMethods.class);

    public void Iterable(List<?> values) {

        Flux.fromIterable(values)
                .doOnNext(value -> {
                    if (!Optional.ofNullable(value).isPresent())
                        throw new RuntimeException("an value is empty");

                    else
                        log.info("theres a value");
                })
                .map(value -> {
                    String newValue = null;
                    try {
                        newValue = value.toString();
                    } catch (Exception exception) {
                        log.error("there was an error");
                    }
                    return newValue;
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

    public void IterableFlatMapName(List<?> values) {

        Flux.fromIterable(values)
                .map(value -> {
                    if (!Optional.ofNullable(value).isPresent())
                        throw new RuntimeException("is a null value");
                    else
                        return value.toString();
                })
                .doOnNext(value -> log.info("string value " + value))
                .map(value -> new User(value.split(" ")[0], value.split(" ")[1]))
                .doOnNext(value -> log.info("user value " + value))
                .flatMap(user -> {
                    if (user.getName().equalsIgnoreCase("diego")) return Mono.just(user);
                    else return Mono.empty(); })
                .subscribe(user -> log.info("user : " +  user));
    }       
    
    public void collectList(List<?> values){

        Flux.fromIterable(values)
            .collectList()
            .subscribe(list -> {
                list.forEach(user -> log.info(user.toString()));
            });
    }

    public void userCommentsFlatMap(){
        Mono<User> userMono = Mono.just(new User("Diego", "Joe"));
        Mono<Comment> commentMono = Mono.fromCallable(() -> {
            return new Comment()
                .addComments("Hello Diego")
                .addComments("whats up")
                .addComments("what do you want");
        });

        userMono.flatMap(user -> commentMono.map(comment -> new UserComments(user, comment)))
        .subscribe(userComment -> log.info(userComment.toString()));
    }
}
