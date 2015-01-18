/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.server;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author animus
 */
public class ServerThreadFactory implements ThreadFactory {

    private AtomicInteger counter = new AtomicInteger(0);

    private final String prefix;

    public ServerThreadFactory(String prefixl) {
        this.prefix = prefixl;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(prefix + counter.getAndIncrement());
        return thread;
    }
}
