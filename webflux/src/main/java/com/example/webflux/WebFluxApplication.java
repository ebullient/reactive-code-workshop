package com.example.webflux;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class WebFluxApplication {

    private Flux<String> lines(){
        return Flux.fromIterable(() -> Jabberwocky.lines().iterator());
    }

    private Flux<String> words(){
        return Flux.fromIterable(() -> Jabberwocky.words().iterator());
    }

    private void dumpFluxToStdOut(Flux<String> f){
        f.subscribe(s->System.out.println(s));
    }

    private List<String> getAsList(Flux<String> f) {
        return f.collectList().block();
    }

    @Bean
    public CommandLineRunner myCommandLineRunner() {
        return args -> {
            System.out.println("==== exercise 1.1");
            Flux<String> src_1_1 = words()
            .map(s -> s.toLowerCase().replaceAll("[^a-zA-Z]", ""));
            dumpFluxToStdOut(src_1_1);

            System.out.println("==== exercise 1.2");
            Flux<String> src_1_2 = src_1_1
            .filter(s -> s.startsWith("b"));
            dumpFluxToStdOut(src_1_2);

            System.out.println("==== exercise 1.3");
            Flux<String> src_1_3 = src_1_1
            .filter(s -> s.startsWith("g"));
            dumpFluxToStdOut(src_1_3);

            System.out.println("==== exercise 1.4");
            Flux<String> src_1_4 = Flux.merge(src_1_2, src_1_3);
            dumpFluxToStdOut(src_1_4);

            System.out.println("==== exercise 1.5");
            Flux<String> src_1_5a = src_1_4
            .map(s -> s.length())
            .map(x -> "" + x);
            dumpFluxToStdOut(src_1_5a);

            Flux<String> src_1_5b = src_1_2
            .doOnNext(getDebugConsumer())
            .map(s -> s.length())
            .scan(0, (acc, next) -> acc + next)
            .map(x -> "" + x);
            dumpFluxToStdOut(src_1_5b);

            Flux<String> src_1_5c = src_1_3
            .distinct()
            .doOnNext(getDebugConsumer())
            .map(s -> s.length())
            .scan(0, (acc, next) -> acc + next)
            .map(x -> "" + x);
            dumpFluxToStdOut(src_1_5c);

            System.out.println("==== exercise 2a");
            Flux<String> src_2a_1 = lines()
            .map(s -> s.split("\\s+"))
            .map(x -> "" + x);
            dumpFluxToStdOut(src_2a_1);

            Flux<String> src_2a_2 = lines()
            .map(s -> Flux.fromArray(s.split("\\s+")))
            .map(x -> "" + x);
            dumpFluxToStdOut(src_2a_2);

            System.out.println("==== exercise 2b");
            Flux<String> src_2b_1 = lines()
            .map(s -> s.split("\\s+"))
            .flatMap(x -> Flux.fromArray(x));
            dumpFluxToStdOut(src_2b_1);

            Flux<String> src_2b_2 = lines()
            .map(s -> Flux.fromArray(s.split("\\s+")))
            .flatMap(x -> x);
            dumpFluxToStdOut(src_2b_2);

            System.out.println("==== exercise 2c-1");
            Flux<String> src_2c_1 = words()
            .map(s -> s.toLowerCase().replaceAll("[^a-zA-Z]", ""))
            .groupBy(s -> s.charAt(0))
            .map(x -> "" + x);
            dumpFluxToStdOut(src_2c_1);

            System.out.println("==== exercise 2c-2");
            Flux<String> src_2c_2 = words()
            .map(s -> s.toLowerCase().replaceAll("[^a-zA-Z]", ""))
            .groupBy(s -> s.charAt(0))
            .flatMap(group -> group);
            dumpFluxToStdOut(src_2c_2);

            System.out.println("==== exercise 2c-3");
            Flux<String> src_2c_3 = words()
            .map(s -> s.toLowerCase().replaceAll("[^a-zA-Z]", ""))
            .distinct()
            .groupBy(s -> s.charAt(0))
            .flatMap(group -> group);
            dumpFluxToStdOut(src_2c_3);

            System.out.println("==== exercise 2c-4");
            Mono<Long> src_2c_4a = words()
            .map(s -> s.toLowerCase().replaceAll("[^a-zA-Z]", ""))
            .distinct()
            .groupBy(s -> s.charAt(0))
            .count();
            src_2c_4a.subscribe(System.out::println);

            Flux<String> src_2c_4b = words()
            .map(s -> s.toLowerCase().replaceAll("[^a-zA-Z]", ""))
            .distinct()
            .groupBy(s -> s.charAt(0))        // groups
            .flatMap(group -> group.count())  // flatten Flux<Mono<Long>>
            .map(x -> "" + x);                // convert Long to String
            dumpFluxToStdOut(src_2c_4b);

            Flux<String> src_2d_1_words = words()
            .map(s -> s.toLowerCase().replaceAll("[^a-zA-Z]", ""))
            .distinct();

            Flux<Integer> src_2d_1_lengths = src_2d_1_words
            .map(s -> s.length());

            System.out.println("==== exercise 2d-1");
            Flux<String> src_2d_1 = src_2d_1_lengths
            .map(x -> "" + x);
            dumpFluxToStdOut(src_2d_1);

            System.out.println("==== exercise 2d-2");
            Flux<String> src_2d_2 = src_2d_1_lengths
            .zipWith(src_2d_1_words)
            .map(x -> "" + x);
            dumpFluxToStdOut(src_2d_2);

            System.out.println("==== exercise 2d-3");
            Flux<String> src_2d_3 = src_2d_1_lengths
            .zipWith(src_2d_1_words)
            .map(x -> x.getT1() + ": " + x.getT2());
            dumpFluxToStdOut(src_2d_3);

            System.out.println("==== exercise 2d-4");
            Flux<String> src_2d_4 = src_2d_1_lengths
            .zipWith(src_2d_1_words)
            .groupBy(x -> x.getT1())
            .flatMap(group -> group.map(x -> x.getT1() + ": " + x.getT2()));
            dumpFluxToStdOut(src_2d_4);

            System.out.println("==== exercise 2d-5");
            Flux<String> src_2d_5 = src_2d_1_lengths
            .zipWith(src_2d_1_words)
            .groupBy(x -> x.getT1())
            .concatMap(group -> group.map(x -> x.getT1() + ": " + x.getT2()));
            dumpFluxToStdOut(src_2d_5);
        };
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(WebFluxApplication.class);
        // prevent SpringBoot from starting a web server
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }

    /**
     * .doOnNext(getDebugConsumer())
     * @return a debug consumer that prints the element it is passed
     */
    private <T> Consumer<T> getDebugConsumer() {
        return new Consumer<T>() {
            @Override
            public void accept(T t) {
                System.out.println(t);
            }
        };
    }
}
