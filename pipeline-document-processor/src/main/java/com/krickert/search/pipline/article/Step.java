package com.krickert.search.pipline.article;

interface Step<I, O> {
    O execute(I value);
    default <R> Step<I, R> pipe(Step<O, R> source) {
        return value -> source.execute(execute(value));
    }
    static <I, O> Step<I, O> of(Step<I, O> source) {
        return source;
    }
}