package com.graemsheppard.chessbot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocationTests {

    @Test
    public void sameDiagonalShouldReturnTrue() {
        Location loc1 = new Location('e', '2');
        Location loc2 = new Location('d', '3');
        Location loc3 = new Location('g', '4');

        assertTrue(loc1.onSameDiagonal(loc2));
        assertTrue(loc1.onSameDiagonal(loc3));
    }


    @Test
    public void sameDiagonalShouldReturnFalse() {
        Location loc1 = new Location('e', '3');
        Location loc2 = new Location('d', '5');
        Location loc3 = new Location('g', '3');

        assertFalse(loc1.onSameDiagonal(loc2));
        assertFalse(loc1.onSameDiagonal(loc3));
        assertFalse(loc2.onSameDiagonal(loc3));
    }
}
