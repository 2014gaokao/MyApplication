package com.example.myapplication

import android.os.Parcel
import android.os.Parcelable

class MyParcelable() : Parcelable {

    private var id: Int = 0

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(123)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyParcelable> {
        override fun createFromParcel(parcel: Parcel): MyParcelable {
            return MyParcelable(parcel)
        }

        override fun newArray(size: Int): Array<MyParcelable?> {
            return arrayOfNulls(size)
        }
    }

    fun getId(): Int {
        return id;
    }
}