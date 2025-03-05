package com.example.myapplication

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding

    companion object {
        /** Combination of all flags required to put activity into immersive mode */
        const val FLAGS_FULLSCREEN =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        /** Milliseconds used for UI animations */
        const val ANIMATION_FAST_MILLIS = 50L
        const val ANIMATION_SLOW_MILLIS = 100L
        private const val IMMERSIVE_FLAG_TIMEOUT = 500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
    }

    override fun onResume() {
        super.onResume()
        // Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
        // be trying to set app to immersive mode before it's ready and the flags do not stick
        activityMainBinding.fragmentContainer.postDelayed({
            activityMainBinding.fragmentContainer.systemUiVisibility = FLAGS_FULLSCREEN
        }, IMMERSIVE_FLAG_TIMEOUT)

        HttpExample().testFunction()
        testCoroutineFunction()
    }

    private fun testCoroutineFunction() {
//        val context = newFixedThreadPoolContext(20, "MyThread")
//        //var context = newSingleThreadContext("MyThread")
//        context.close()
//        val scope = CoroutineScope(Dispatchers.Default)
//        scope.launch {
//            println("Coroutine thread11: ${Thread.currentThread().name}")
//        }

        //won't block thread
//        println("Coroutine thread before: ${Thread.currentThread().name}")
//        lifecycleScope.launch {
//            println("Coroutine thread: ${Thread.currentThread().name}")
//            val data = getData()
//            val processedData = processData(data)
//            println("Coroutine thread: ${Thread.currentThread().name} + $processedData")
//        }
//        println("Coroutine thread after: ${Thread.currentThread().name}")
        /*
        2025-03-06 00:11:24.562 18263-18263 System.out              com.example.myapplication            I  Coroutine thread before: main
        2025-03-06 00:11:24.562 18263-18263 System.out              com.example.myapplication            I  Coroutine thread: main
        2025-03-06 00:11:24.563 18263-18263 System.out              com.example.myapplication            I  Coroutine thread after: main
        2025-03-06 00:11:26.564 18263-18301 System.out              com.example.myapplication            I  withContext Coroutine thread: DefaultDispatcher-worker-1
        2025-03-06 00:11:28.568 18263-18301 System.out              com.example.myapplication            I  withContext Coroutine thread: DefaultDispatcher-worker-1 + network
        2025-03-06 00:11:28.572 18263-18263 System.out              com.example.myapplication            I  Coroutine thread: main + processedData
         */


//        lifecycleScope.launch {
//            coroutineScope {
//                println("Coroutine thread: ${Thread.currentThread().name}")
//                val deferred1 = async {
//                    getData()
//                }
//                val deferred2 = async {
//                    getData()
//                }
//                val con1 = deferred1.await()
//                val con2 = deferred2.await()
//                println("await Coroutine thread: $con1 + $con2")
//            }
//        }
        /*
        2025-03-06 00:13:29.014 18707-18707 System.out              com.example.myapplication            I  Coroutine thread: main
        2025-03-06 00:13:31.016 18707-18752 System.out              com.example.myapplication            I  withContext Coroutine thread: DefaultDispatcher-worker-2
        2025-03-06 00:13:31.017 18707-18751 System.out              com.example.myapplication            I  withContext Coroutine thread: DefaultDispatcher-worker-1
        2025-03-06 00:13:31.020 18707-18707 System.out              com.example.myapplication            I  await Coroutine thread: network + network
         */


//        println("Coroutine thread before: ${Thread.currentThread().name}")
//        runBlocking {
//            getData()
//        }
//        println("Coroutine thread after: ${Thread.currentThread().name}")
        /*
        2025-03-06 00:15:50.748 19002-19002 System.out              com.example.myapplication            I  Coroutine thread before: main
        2025-03-06 00:15:52.752 19002-19054 System.out              com.example.myapplication            I  withContext Coroutine thread: DefaultDispatcher-worker-1
        2025-03-06 00:15:52.755 19002-19002 System.out              com.example.myapplication            I  Coroutine thread after: main
         */


        lifecycleScope.launch {
            val initJob = launch {
                getData()
            }
            println("Coroutine thread before: ${Thread.currentThread().name}")
            initJob.join()
            println("Coroutine thread after: ${Thread.currentThread().name}")
            //do something before wait initJob, without care about result
        }
        /*
        2025-03-06 00:25:21.959 20715-20715 System.out              com.example.myapplication            I  Coroutine thread before: main
        2025-03-06 00:25:23.965 20715-20745 System.out              com.example.myapplication            I  withContext Coroutine thread: DefaultDispatcher-worker-1
        2025-03-06 00:25:23.974 20715-20715 System.out              com.example.myapplication            I  Coroutine thread after: main
        */

    }

    private suspend fun getData() = withContext(Dispatchers.IO) {
        Thread.sleep(2000)
        println("withContext Coroutine thread: ${Thread.currentThread().name}")
        "network"
    }

    private suspend fun processData(network: String) = withContext(Dispatchers.Default) {
        Thread.sleep(2000)
        println("withContext Coroutine thread: ${Thread.currentThread().name} + $network")
        "processedData"
    }

}