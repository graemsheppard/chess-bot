package com.graemsheppard.chessbot;

import lombok.Getter;

public class Location {
    @Getter
    private char file;
    @Getter
    private char rank;

    public Location (String coords) {
        file = coords.charAt(0);
        rank = coords.charAt(1);
    }

    public Location (char file, char rank) {
        this.rank = rank;
        this.file = file;
    }

    public Location addRanks(int distance) {
        Location newLocation = new Location(this.file, (char) (this.rank + distance));
        return newLocation;
    }

    public Location addFiles(int distance) {
        Location newLocation = new Location((char) (this.file + distance), this.rank);
        return newLocation;
    }

    public boolean equals(Location location) {
        return this.rank == location.rank && this.file == location.file;
    }

    public boolean isValid() {
        return this.rank >= '1' && this.rank <= '8'
                && this.file >= 'a' && this.file <= 'h';
    }

    public int[] asIndex() {
        return new int[] { this.file - 'a', this.rank - '1' };
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Location) {
            Location other = (Location) object;
            return this.file == other.getFile() && this.rank == other.getRank();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.file + 2 * this.rank;
    }

    @Override
    public String toString() {
        return "" + this.getFile() + this.getRank();
    }

}