package com.example.myapplication;

import android.annotation.SuppressLint;

import org.junit.Test;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Function;

public class RxJavaExampleUnitTest {

    @SuppressLint("CheckResult")
    @Test
    public void example() {
        Single<Integer> r = Single.create(new MyObserver());
        Single<Hold> h = Single.just(new HoldImpl());
        h.zipWith(r, this::zip).map(hold -> {
            System.out.println("map apply");
            return hold;
        }).subscribe(this::success, this::fail);
    }

    private Hold zip(Hold hold, Integer integer) throws Exception {
        System.out.println("zip process");
        if (false) {
            throw new Exception();
        }
        return hold;
    }

    private void success(Hold hold) {
        System.out.println("zip return success");
    }

    private void fail(Throwable throwable) {
        System.out.println("zip return fail");
    }

    public interface Hold {

    }

    public class HoldImpl implements Hold {

    }

    public class MyObserver implements SingleOnSubscribe<Integer>, Observer<Integer> {

        @Override
        public void onSubscribe(@NonNull Disposable d) {

        }

        @Override
        public void onNext(@NonNull Integer integer) {
            System.out.println("Observer onNext integer: " + integer);
            System.out.println("mSingleEmitter.onSuccess(3)");
            mSingleEmitter.onSuccess(3);
        }

        @Override
        public void onError(@NonNull Throwable e) {

        }

        @Override
        public void onComplete() {

        }

        private SingleEmitter<Integer> mSingleEmitter;

        //SingleOnSubscribe<Result>
        @Override
        public void subscribe(@NonNull SingleEmitter<Integer> emitter) throws Throwable {
            mSingleEmitter = emitter;
            new OpenCameraManager().openCamera(this);
        }
    }

    public class MySingle extends Single<Integer> {

        @Override
        protected void subscribeActual(@NonNull SingleObserver<? super Integer> observer) {
            System.out.println("single subscribeActual observer.onSuccess(1)");
            observer.onSuccess(1);
        }
    }

    public class OpenCameraManager implements ObservableOnSubscribe<Integer> {

        public OpenCameraManager() {
        }

        @SuppressLint("CheckResult")
        public void openCamera(Observer<Integer> observer) {
            //new OpenCameraManager().openCamera(this);
            //see ObservableExample
            Observable.create(this).subscribe(observer);

            new MySingle().subscribe(this::fire);
        }

        private void fire(Integer integer) {
            System.out.println("single subscribe fire get integer: " + integer);
            System.out.println("mObservableEmitter.onNext(2)");
            mObservableEmitter.onNext(2);
        }

        private ObservableEmitter<Integer> mObservableEmitter;

        @Override
        public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Throwable {
            mObservableEmitter = emitter;
        }
    }

    //==================================================//

    @Test
    public void ObservableExample() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Throwable {
                emitter.onNext(11);
            }
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Integer integer) {
                // 11
                System.out.println(integer);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @SuppressLint("CheckResult")
    @Test
    public void singleExample() {
        Single<Integer> single = Single.create(new SingleOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Integer> emitter) throws Throwable {
                emitter.onSuccess(1);
            }
        });
        single.subscribe(new SingleObserver<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull Integer t) {
                // 1
                System.out.println(t);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });

        //==================================================//

        Single<Integer> prepareSingle = Single.just(0);
        prepareSingle.zipWith(single, new BiFunction<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer integer, Integer integer2) throws Throwable {
                // 0 1
                return 2;
            }
        }).map(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) throws Throwable {
                // 2
                return 3;
            }
        }).subscribe(new SingleObserver<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull Integer integer) {
                // 3
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });

        //==================================================//

        new Single<Integer>() {
            @Override
            protected void subscribeActual(@NonNull SingleObserver observer) {
                observer.onSuccess(12);
            }
        }.subscribe(this::fire);
    }

    private void fire(Integer integer) {
        //12
        System.out.println(integer);
    }

}
