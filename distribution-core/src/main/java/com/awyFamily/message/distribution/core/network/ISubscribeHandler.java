package com.awyFamily.message.distribution.core.network;

/**
 * @author yhw
 * @date 2022-05-26
 */
public interface ISubscribeHandler<T> {

    default boolean handler(T t) {
        if (match(t)) {
            return apply(t);
        }
        return false;
    }

    boolean apply(T t);

    boolean match(T t);

    int getSort();
}
