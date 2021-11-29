package com.chwishay.orthrecnursing

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers

fun <T> Observable<T>.schedule(): Observable<T> {
    return compose {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun <T> Flowable<T>.schedule(): Flowable<T> {
    return compose {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun <T> Single<T>.schedule(): Single<T> {
    return compose {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun Completable.schedule(): Completable {
    return compose {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

@Synchronized
fun Disposable.addTo(vararg compositeDisposables: CompositeDisposable) {
    compositeDisposables.forEach {
        it.add(this)
    }
}

val IGNORE_ERROR_CONSUMER = Consumer<Throwable> {
    it.printStackTrace()
}

fun <T> Observable<T>.execute(
    onNext: Consumer<T> = Functions.emptyConsumer(),
    onError: Consumer<Throwable> = IGNORE_ERROR_CONSUMER,
    onComplete: Action = Functions.EMPTY_ACTION
): Disposable {
    return subscribe(onNext, onError, onComplete)
}

fun <T> Single<T>.execute(
    onNext: Consumer<T> = Functions.emptyConsumer(),
    onError: Consumer<Throwable> = IGNORE_ERROR_CONSUMER
): Disposable {
    return subscribe(onNext, onError)
}

fun Completable.execute(
    action: Action = Action {},
    onError: Consumer<Throwable> = IGNORE_ERROR_CONSUMER
): Disposable {
    return subscribe(action, onError)
}
