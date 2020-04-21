package com.chensong.main.support;

import com.chensong.main.exception.SQLBadGrammarException;

@FunctionalInterface
public interface ExucuteSQLTimeoutFunction<T,R> {

    /**
     *
     * @param t
     * @return
     * @throws SQLBadGrammarException
     */
    R apply(T t)throws SQLBadGrammarException;
}
