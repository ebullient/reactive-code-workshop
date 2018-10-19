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
