package com.springboot.reactive.mscv.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.springboot.reactive.mscv.app.model.Comment;
import com.springboot.reactive.mscv.app.model.User;
import com.springboot.reactive.mscv.app.model.UserComments;
import com.springboot.reactive.mscv.app.reactor.ReactorMethods;

import lombok.experimental.var;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactiveApplication implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(SpringBootReactiveApplication.class);

	@Autowired
	private ReactorMethods reactorMethods;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactiveApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		backPressureExample();
	}

	public void backPressureExample(){
		Flux.range(1, 10)
		.log()
		.limitRate(2) //short method
		.subscribe(/*new Subscriber<Integer>() { long method

			private Subscription sub;
			private Integer limit = 2;
			private Integer consume = 0;

			@Override
			public void onSubscribe(Subscription s) {
				this.sub = s;
				s.request(limit);
			}

			@Override
			public void onNext(Integer t) {
				LOG.info(t.toString());
				consume++;
				if (consume == limit) {
					consume = 0;
					sub.request(limit);
				}
			}

			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException("Unimplemented method 'onError'");
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException("Unimplemented method 'onComplete'");
			}
			
		} */);
	}

	public void intervalFromCreate(){
		Flux.create(emitter-> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				
				private int counter = 0;

				@Override
				public void run(){
					emitter.next(++counter);
					if (counter == 10) {
						timer.cancel();
						emitter.complete();
					}
					if (counter == 5){
						timer.cancel();
						emitter.error(new InterruptedException("error the flux was stopped"));
					}
				}
			}, 1000, 1000);
		})
		.subscribe(data -> LOG.info(data.toString()), error -> LOG.error(error.getMessage()),() -> LOG.info("it's done"));
	}

	public void infiniteInterval() throws InterruptedException{
		CountDownLatch latch = new CountDownLatch(1);
		Flux.interval(Duration.ofSeconds(1))
		.doOnTerminate(latch::countDown)
		.flatMap(value -> {
			if (value >= 5) return Flux.error(new InterruptedException("No more than 5"));

			return Flux.just(value);
			
		})
		.retry(2)
		.map(value -> "hello" + value)
		.subscribe(info -> LOG.info(info), exception -> LOG.error(exception.getMessage()));

		latch.await();
	}

	public void intervalDelayElements(){
		Flux<Integer> range = Flux.range(1, 12)
		.delayElements(Duration.ofSeconds(1))
		.doOnNext(value -> LOG.info(value.toString()));

		range.blockLast();
	}

	public void interval(){
		Flux<Integer> range = Flux.range(1, 12);
		Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));

		range.zipWith(delay , (ran, del) -> ran)
		.doOnNext(value -> LOG.info(value.toString()))
		.subscribe();
	}

	public void userCommentZipWithRange() {

		Flux.just(1, 2, 3, 4, 5)
		.map(num -> num * 2)
		.zipWith(Flux.range(0, 4), (one, two) -> String.format("First flux : %d , Second flux : %d", one, two))
		.subscribe(value -> LOG.info(value));


	}

	public void userCommentZipWithForm2Example() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("Diego", "Joe"));
		Mono<Comment> commentMono = Mono.fromCallable(() -> {
			return new Comment()
					.addComments("hello Diego")
					.addComments("whats up")
					.addComments("what do you want");

		});

		Mono<UserComments> userComments = userMono
				.zipWith(commentMono)
				.map(tuple -> {
					User user = tuple.getT1();
					Comment comment = tuple.getT2();
					return new UserComments(user, comment);
				});

		userComments.subscribe(userComment -> LOG.info(userComment.toString()));
	}

	public void userCommentZipWithExample() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("Diego", "Joe"));
		Mono<Comment> commentMono = Mono.fromCallable(() -> {
			return new Comment()
					.addComments("hello Diego")
					.addComments("whats up")
					.addComments("what do you want");

		});

		Mono<UserComments> userComments = userMono.zipWith(commentMono,
				(user, comment) -> new UserComments(user, comment));
		userComments.subscribe(userWithComment -> LOG.info(userWithComment.toString()));

	}

	public void userCommentsFlatMapExample() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("Diego", "Joe"));
		Mono<Comment> commentMono = Mono.fromCallable(() -> {
			return new Comment()
					.addComments("hello Diego")
					.addComments("whats up")
					.addComments("what do you want");

		});

		userMono.flatMap(user -> commentMono.map(comment -> new UserComments(user, comment)))
				.subscribe(userComment -> LOG.info(userComment.toString()));
	}

	public void collectListExample() {
		var userList = new ArrayList<User>();
		userList.add(new User("Diego", "Doe"));
		userList.add(new User("Alejndro", "Doe"));
		userList.add(new User("Sara", "Doe"));
		userList.add(new User("Pedri", "Doe"));
		userList.add(new User("Jhon", "Doe"));
		userList.add(new User("Diego", "Alejandro"));

		Flux.fromIterable(userList)
				.collectList()
				.subscribe(list -> {
					list.forEach(user -> LOG.info(user.toString()));
				});

	}

	public void toStringExample() {
		var userList = new ArrayList<User>();
		userList.add(new User("Diego", "Doe"));
		userList.add(new User("Alejndro", "Doe"));
		userList.add(new User("Sara", "Doe"));
		userList.add(new User("Pedri", "Doe"));
		userList.add(new User("Jhon", "Doe"));
		userList.add(new User("Diego", "Alejandro"));

		Flux.fromIterable(userList)
				.map(user -> user.getName().concat(" ").concat(user.getLastName()))
				.flatMap(name -> {
					if (name.contains("Diego")) {
						return Mono.just(name);
					}
					{
						return Mono.empty();
					}
				})
				.map(name -> {
					var nameToUpper = name.toUpperCase();
					return nameToUpper;
				})
				.subscribe(name -> System.out.println(name.toString()));
	}

	public void IterableFlatMapExample() {
		Flux.just("Diego Doe", "Alejandro Doe", "Sara Doe", "Pedro Doe", "Jhon Doe", "Diego Alejandro")
				.map(value -> new User(value.split(" ")[0], value.split(" ")[1]))
				.flatMap(user -> {
					if (user.getName().equalsIgnoreCase("diego")) {
						return Mono.just(user);
					}
					{
						return Mono.empty();
					}
				})
				.map(user -> {
					var name = user.getName().toUpperCase();
					var newUser = user.builder().name(name).build();
					return newUser;
				})
				.subscribe(user -> System.out.println(user.toString()));
	}

	public void IterableExample() throws Exception {

		final Flux<User> flux = Flux
				// .fromIterable(List.of("Diego Doe", "Alejandro Doe", "Sara Doe", "Pedro Doe",
				// "Jhon Doe", "Diego Alejandro"))
				.just("Diego Doe", "Alejandro Doe", "Sara Doe", "Pedro Doe", "Jhon Doe", "Diego Alejandro")
				.map(value -> new User(value.split(" ")[0], value.split(" ")[1]))
				.filter(user -> user.getName().trim().toLowerCase().equals("diego"))
				.doOnNext(user -> {
					if (!Optional.ofNullable(user).isPresent()) {
						throw new RuntimeException("name could not be empty");
					}
					{
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println(user.toString());
					}
				})
				.map(user -> {
					var name = user.getName().toUpperCase();
					var newUser = user.builder().name(name).build();
					return newUser;
				});

		flux.subscribe(user -> LOG.info(user.getName()),
				error -> LOG.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						LOG.info("the execution has finalized");
					}
				});
	}

}
