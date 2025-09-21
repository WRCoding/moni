package com.longjunwang.moni.agent;

public interface MoniAgent<P,R> {
    R submitAgent(P p);
    void executeAgent(P p);
}
