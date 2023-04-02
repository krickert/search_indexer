package com.krickert.search;

interface Step<I, O> {

    static <I, O> Step<I, O> of(Step<I, O> source) {
        return source;
    }

    O execute(I value);

    default <R> Step<I, R> pipe(Step<O, R> source) {
        return value -> source.execute(execute(value));
    }
}