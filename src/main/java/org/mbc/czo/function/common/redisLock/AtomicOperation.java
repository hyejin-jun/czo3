package org.mbc.czo.function.common.redisLock;

import com.fasterxml.jackson.core.JsonProcessingException;

@FunctionalInterface
public interface AtomicOperation<T> {
    T executeAtomicOperation() throws JsonProcessingException;
}
