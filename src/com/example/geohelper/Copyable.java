package com.example.geohelper;

public interface Copyable<T> {
    T copy ();
    T createForCopy ();
    void copyTo (T dest);
}