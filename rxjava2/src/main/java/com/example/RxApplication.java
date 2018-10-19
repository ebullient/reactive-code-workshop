package com.example;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public final class RxApplication {

    private Observable<String> lines() {
        return Observable.fromIterable(() -> Jabberwocky.lines().iterator());
    }

    private Observable<String> words() {
        return Observable.fromIterable(() -> Jabberwocky.words().iterator());
    }

    private void dumpObservableToStdOut(Observable<String> f){
        f.subscribe(s->System.out.println(s));
    }

    private List<String> getAsList(Observable<String> f) {
        return f.toList().blockingGet();
    }

    private void run(){

        System.out.println("=== Exercise 1");
        Observable<String> result_b_g = words()
            .map(s -> s.replaceAll("[^a-zA-Z]","").toLowerCase())
            .filter(s -> s.startsWith("b") || s.startsWith("g"));

        dumpObservableToStdOut(result_b_g);
        result_b_g.count().subscribe(i -> System.out.println(i));
        result_b_g.distinct().count().subscribe(i -> System.out.println(i));

        System.out.println("---");
        Observable<String> result_b = words()
            .map(s -> s.replaceAll("[^a-zA-Z]","").toLowerCase())
            .filter(s -> s.startsWith("b"));

        dumpObservableToStdOut(result_b);
        result_b.count().subscribe(i -> System.out.println(i));
        result_b.distinct().count().subscribe(i -> System.out.println(i));

        System.out.println("---");
        Observable<String> result_g = words()
            .map(s -> s.replaceAll("[^a-zA-Z]","").toLowerCase())
            .filter(s -> s.startsWith("g"));

        dumpObservableToStdOut(result_g);
        result_g.count().subscribe(i -> System.out.println(i));
        result_g.distinct().count().subscribe(i -> System.out.println(i));

        System.out.println("---");
        dumpObservableToStdOut(Observable.merge(result_b.distinct(), result_g.distinct()));

        System.out.println("=== Exercise 2");
        Observable<String> group_firstLetter = words()
            .map(s -> s.replaceAll("[^a-zA-Z]","").toLowerCase())
            .filter(s -> s.startsWith("b") || s.startsWith("g"))
            .groupBy(s -> s.charAt(0))
            .doOnNext(getDebugConsumer())
            .flatMap(v -> v);

        List<String> b_g = getAsList(result_b_g);
        List<String> group = getAsList(group_firstLetter);

        System.out.println(b_g);
        System.out.println(group);
        System.out.println(b_g.equals(group));

        System.out.println("============== ");

        Observable<String> filtered = lines()
            .filter(x -> x.length() > 0)
            .flatMap(x -> Observable.fromArray(x.split("\\s+")))
            .map(x -> x.toLowerCase().replaceAll("[^a-zA-Z]",""))
            .distinct();

        // dumpObservableToStdOut(filtered);

        Observable<Integer> length = filtered
            .map(x -> x.length());
            // .doOnNext(getDebugConsumer());

        Observable<String> zip = length
            .zipWith(filtered, Pair::new)
            .groupBy(x -> x.getA())
            .concatMap(x -> x.map(y -> x.getKey() + ": " + y.getB()));

        dumpObservableToStdOut(zip);
    }

    public static void main(String[] args) throws Exception{
      RxApplication app = new RxApplication();
      app.run();
    }

    /**
     * .doOnNext(getDebugConsumer())
     * @return a debug consumer that prints the element it is passed
     */
    private <T> Consumer<T> getDebugConsumer() {
        return new Consumer<T>() {
            @Override
            public void accept(T t) throws Exception {
                System.out.println(t);
            }
        };
    }
}
