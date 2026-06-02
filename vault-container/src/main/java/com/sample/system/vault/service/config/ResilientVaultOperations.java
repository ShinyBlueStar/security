package com.sample.system.vault.service.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.springframework.vault.core.VaultOperations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

/**
 * Wraps VaultOperations so every network call to Vault goes through a circuit breaker
 * and a bounded retry.
 */
final class ResilientVaultOperations implements InvocationHandler {

    private final VaultOperations delegate;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    private ResilientVaultOperations(VaultOperations delegate, CircuitBreaker circuitBreaker, Retry retry) {
        this.delegate = delegate;
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
    }

    static VaultOperations wrap(VaultOperations delegate, CircuitBreaker circuitBreaker, Retry retry) {
        return (VaultOperations) Proxy.newProxyInstance(
                VaultOperations.class.getClassLoader(),
                new Class<?>[]{VaultOperations.class},
                new ResilientVaultOperations(delegate, circuitBreaker, retry));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Supplier<Object> call = () -> invokeDelegate(method, args);
        Supplier<Object> decorated = CircuitBreaker.decorateSupplier(circuitBreaker, Retry.decorateSupplier(retry, call));
        return decorated.get();
    }

    private Object invokeDelegate(Method method, Object[] args) {
        try {
            return method.invoke(delegate, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException(cause);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
