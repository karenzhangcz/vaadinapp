package com.example.application.service;

@FunctionalInterface
public interface AsyncRestCallback<T> {
    void operationFinished(T results);
}