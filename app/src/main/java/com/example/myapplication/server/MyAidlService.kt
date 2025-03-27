package com.example.myapplication.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.myapplication.IMyAidlInterface
import com.example.myapplication.MyParcelable

class MyAidlService : Service() {

    private val mBinder = object : IMyAidlInterface.Stub() {
        override fun basicTypes(
            anInt: Int,
            aLong: Long,
            aBoolean: Boolean,
            aFloat: Float,
            aDouble: Double,
            aString: String?
        ) {
            TODO("Not yet implemented")
        }

        override fun getMyParcelable(): MyParcelable {
            return MyParcelable()
        }

        override fun setMyParcelable(data: MyParcelable?) {

        }

    }
    
    override fun onBind(p0: Intent?): IBinder? {
        return mBinder
    }
}