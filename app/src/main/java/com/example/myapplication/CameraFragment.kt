package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.myapplication.databinding.FragmentCameraBinding
import kotlinx.coroutines.suspendCancellableCoroutine

class CameraFragment : Fragment() {

    /** Android ViewBinding */
    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding get() = _fragmentCameraBinding!!

    private val navController : NavController by lazy {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }

    private val cameraManager: CameraManager by lazy {
        val context = requireContext().applicationContext
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private val characteristics: CameraCharacteristics by lazy {
        cameraManager.getCameraCharacteristics(arguments?.getString("camera_id").toString())
    }

    private lateinit var imageReader: ImageReader

    private val cameraThread = HandlerThread("CameraThread").apply { start() }

    private val cameraHandler = Handler(cameraThread.looper)

    private val imageReaderThread = HandlerThread("imageReaderThread").apply { start() }

    private val imageReaderHandler = Handler(imageReaderThread.looper)

    private lateinit var camera: CameraDevice

    private lateinit var session: CameraCaptureSession

    private lateinit var viewModel: UserViewModel
    //private val viewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return fragmentCameraBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        fragmentCameraBinding.viewFinder.holder.addCallback(object : SurfaceHolder.Callback {
//            override fun surfaceCreated(p0: SurfaceHolder) {
//                TODO("Not yet implemented")
//            }
//
//            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
//                TODO("Not yet implemented")
//            }
//
//            override fun surfaceDestroyed(p0: SurfaceHolder) {
//                TODO("Not yet implemented")
//            }
//
//        })

        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        viewModel.user.observe(viewLifecycleOwner, Observer { newData -> Log.d("zcc", newData.name) })

        fragmentCameraBinding.viewModel = viewModel
        fragmentCameraBinding.lifecycleOwner = this

        val src: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.cxk);
        val copy: Bitmap = src.copy(Bitmap.Config.ARGB_8888, false)
        JNILoader().stringFromJNI(copy)
        fragmentCameraBinding.image.setImageBitmap(copy)
        fragmentCameraBinding.image.setOnClickListener {
            viewModel.updateUser("蔡徐坤")
        }

        var camera_id = getArguments()?.getString("camera_id")
        Log.d("zcc", camera_id.toString())

//        runBlocking {
//            val job = launch {
//                delay(3000)
//            }
//            job.join()
//            Log.d("zcc", "join")
//        }
    }

    private suspend fun openCamera(
        manager: CameraManager,
        cameraId: String,
        handler: Handler? = null
    ) : CameraDevice = suspendCancellableCoroutine { cont ->
//        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
//            override fun onOpened(p0: CameraDevice) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onDisconnected(p0: CameraDevice) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onError(p0: CameraDevice, p1: Int) {
//                TODO("Not yet implemented")
//            }
//
//        }, handler)
    }

}