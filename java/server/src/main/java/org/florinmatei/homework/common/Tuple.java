/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.common;

/**
 *
 * @author Florin Matei
 */
public class Tuple<K, V> {

    K parameter1;
    V parameter2;

    public Tuple(K parameter1, V parameter2) {
        this.parameter1 = parameter1;
        this.parameter2 = parameter2;
    }

    public K getParameter1() {
        return parameter1;
    }

    public V getParameter2() {
        return parameter2;
    }
}
