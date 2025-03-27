// IMyAidlInterface.aidl
package com.example.myapplication;

import com.example.myapplication.MyParcelable;
// Declare any non-default types here with import statements

interface IMyAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    MyParcelable getMyParcelable();

    void setMyParcelable(in MyParcelable data);
}