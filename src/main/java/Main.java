import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Mono.empty();
        Flux.empty();
        Mono<Integer> mono = Mono.just(1);
        Flux<Integer> flux = Flux.just(1,2,3);
        Flux<Integer> fluxFromMono = mono.flux();
        Mono<Boolean> any = flux.any(s -> s.equals(1));
        Mono<Integer> integerMono = flux.elementAt(1);


        Flux.range(1, 5).subscribe(System.out::println);
        Flux.fromIterable(Arrays.asList(1,2,3)).subscribe(System.out::println);

//        Flux.generate(sink->{
//            sink.next("hello");
//        })
//                .delayElements(Duration.ofMillis(500))
//                .take(4)
//                .subscribe(System.out::println);


//        Flux.generate(
//                ()->2354,
//                (state,sink)->{
//                    if (state>2366){
//                        sink.complete();
//                    }else {
//                        sink.next("Step: "+ state);
//                    }
//                    return state+3;
//                }
//        ).subscribe(System.out::println);

        Flux<Object> producer = Flux.generate(
                () -> 2354,
                (state, sink) -> {
                    if (state > 2366) {
                        sink.complete();
                    } else {
                        sink.next("Step: " + state);
                    }
                    return state + 3;
                }
        );


        Flux    //push - однопоточный
                .create(sink -> {
                        producer.subscribe(new BaseSubscriber<Object>() {
                    @Override
                    protected void hookOnNext(Object value) {
                        sink.next(value);
                    }

                    @Override
                    protected void hookOnComplete() {
                        sink.complete();
                    }
                });
                        sink.onRequest(r-> {
                            sink.next("DB returns: "+ producer.blockFirst());
                        });
                });

        Flux<String> repeat = Flux.just("World", "coder").repeat();
        Flux<String> stringFlux = Flux.just("hello", "man", "java").zipWith(repeat, (f, s) -> String.format("%s %s", f, s));
        Flux<String> stringFlux1 = stringFlux
                .delayElements(Duration.ofMillis(1300))
                .timeout(Duration.ofSeconds(1))
//                .retry(3)
//                .onErrorReturn("Too slow")
                .onErrorResume(throwable ->
                        Flux.interval(Duration.ofMillis(300))
                                .map(String::valueOf)
                )
                .skip(2)
                .take(3);


        stringFlux1.subscribe(
                v-> System.out.println(v),
                e ->System.err.println(e),
                ()-> System.out.println("finished")


        );

        Iterable<String> strings = stringFlux1.toIterable();
        Thread.sleep(9000l);


    }
}
