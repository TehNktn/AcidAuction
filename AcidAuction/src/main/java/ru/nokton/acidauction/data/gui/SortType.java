package ru.nokton.acidauction.data.gui;

public enum SortType {
    NONE,
    PRICE,
    DATE;

    private SortType() {
    }

    public SortType next() {
        if (this == NONE) {
            return PRICE;
        } else {
            return this == PRICE ? DATE : PRICE;
        }
    }
}
