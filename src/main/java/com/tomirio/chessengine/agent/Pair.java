/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tomirio.chessengine.agent;

import com.google.common.base.Objects;

/**
 * Container to ease passing around a tuple of two objects. This object provides
 * a sensible implementation of equals(), returning true if equals() is true on
 * each of the contained objects.
 *
 * @param <F> Type of the first element in the pair.
 * @param <S> Type of the second element in the pair.
 */
public class Pair<F, S> {

    public final F first;
    public final S second;

    /**
     * Constructor for a Pair.
     *
     * @param first the first object in the Pair
     * @param second the second object in the pair
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Checks the two objects for equality by delegating to their respective
     * {@link Object#equals(Object)} methods.
     *
     * @param o the {@link Pair} to which this one is to be checked for equality
     * @return true if the underlying objects of the Pair are both considered
     * equal
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> p = (Pair<?, ?>) o;
        return Objects.equal(p.first, first) && Objects.equal(p.second, second);
    }

    /**
     * Compute a hash code using the hash codes of the underlying objects
     *
     * @return a hashcode of the Pair
     */
    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
    }

    /**
     * Convenience method for creating an appropriately typed pair.
     *
     * @param <A> The type of the first element in the pair.
     * @param <B> The type of the second element in the pair.
     * @param a the first object in the Pair
     * @param b the second object in the pair
     * @return a Pair that is templatized with the types of a and b
     */
    public static <A, B> Pair<A, B> create(A a, B b) {
        return new Pair<>(a, b);
    }
}
