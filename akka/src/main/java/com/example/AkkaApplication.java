package com.example;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import scala.collection.mutable.HashSet;

public class AkkaApplication {

    private Source<String,NotUsed> lines() {
        return Source.fromIterator(() -> Jabberwocky.lines().iterator());
    }

    private Source<String,NotUsed> words() {
        return Source.fromIterator(() -> Jabberwocky.words().iterator())
        .map(s -> s.toLowerCase().replaceAll("[^a-zA-Z]*",""));
    }

    private void dumpSourceToStdOut(Source<?,NotUsed> src) throws InterruptedException, ExecutionException {
        final ActorSystem system = ActorSystem.create("QuickStart");
        final Materializer materializer = ActorMaterializer.create(system);

        final CompletionStage<Done> done = src.runWith(Sink.foreach(System.out::println),materializer);
        done.thenRun(()->system.terminate());

        // Make it happen
        done.toCompletableFuture().get();
    }

    private void run() throws Exception {
        System.out.println("=== exercise 1.1");
        Source<String, NotUsed> src_1_1 = words();
        dumpSourceToStdOut(src_1_1);

        System.out.println("=== exercise 1.2");
        Source<String, NotUsed> src_1_2 = words()
        .filter(s -> s.startsWith("b"));
        dumpSourceToStdOut(src_1_2);

        System.out.println("=== exercise 1.3");
        Source<String, NotUsed> src_1_3 = words()
        .filter(s -> s.startsWith("g"));
        dumpSourceToStdOut(src_1_3);

        System.out.println("=== exercise 1.4");
        Source<String, NotUsed> src_1_4 = src_1_3.merge(src_1_2);
        dumpSourceToStdOut(src_1_4);

        System.out.println("=== exercise 1.5");
        Source<String, NotUsed> src_1_5 = src_1_3.merge(src_1_2)
        .map(x -> "" + x.length());
        dumpSourceToStdOut(src_1_5);

        System.out.println("=== exercise 1.6");
        Source<String, NotUsed> src_1_6 = words()
        .filter(s -> s.startsWith("b"))
        .fold(0, (acc, next) -> acc + next.length())
        .map(x -> ""+x);
        dumpSourceToStdOut(src_1_6);

        System.out.println("=== exercise 1.7");
        final HashSet<String> seen = new HashSet<>();
        Source<String, NotUsed> src_1_7 = words()
        .filter(s -> s.startsWith("g"))
        .takeWhile(x -> seen.add(x))
        .alsoTo(getDebugSink())
        .fold(0, (acc, next) -> acc + next.length())
        .map(x -> ""+x);
        dumpSourceToStdOut(src_1_7);

        System.out.println("=== exercise 2a.1");
        Source<String, NotUsed> src_2a_1 = lines()
        .map(line -> Source.from(Arrays.asList(line.split("\\s+"))))
        .map(x -> ""+x);
        dumpSourceToStdOut(src_2a_1);

        System.out.println("=== exercise 2a.2");
        Source<String, NotUsed> src_2a_2 = lines()
        .map(line -> Source.from(Arrays.asList(line.split("\\s+"))))
        .map(x -> ""+x);
        dumpSourceToStdOut(src_2a_2);

        System.out.println("=== exercise 2b");
        Source<String, NotUsed> src_2b = lines()
        .filter(s -> s.length() > 0)
        .map(line -> Source.from(Arrays.asList(line.split("\\s+"))))
        .flatMapConcat( x -> x.map(y -> y.toLowerCase().replaceAll("[^a-zA-Z]*","")) );
        dumpSourceToStdOut(src_2b);

        System.out.println("=== exercise 2c --> adjust to take first word of each letter");
        Source<String, NotUsed> src_2c = src_2b
        .groupBy(26, x -> x.charAt(0))
        .take(1)
        .mergeSubstreams();
        dumpSourceToStdOut(src_2c);

        System.out.println("=== exercise 2d");
        Source<String, NotUsed> distinct = words()
        .groupBy(250, x -> x)
        .take(1)
        .mergeSubstreams();

        Source<Integer, NotUsed> lengths = distinct.map(s -> s.length());
        Source<Pair<Integer, String>, NotUsed> pairs = lengths.zip(distinct);

        Source<String, NotUsed> src_2d = pairs
        .alsoTo(getDebugSink())
        .groupBy(30, pair -> pair.first())
        .take(1)
        .mergeSubstreams()
        .map(pair -> pair.first() + ": " + pair.second());
        dumpSourceToStdOut(src_2d);
    }

    public static void main(String[] args) throws Exception {
        AkkaApplication app = new AkkaApplication();
        app.run();
    }

    /**
     * .alsoTo(getDebugSink())
     * @return a sink that prints all elements it has been passed
     */
    private <T> Sink<T,CompletionStage<Done>> getDebugSink() {
        return Sink.foreach(System.out::println);
    }
}
