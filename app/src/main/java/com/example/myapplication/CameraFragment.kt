package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.FragmentCameraBinding

class CameraFragment : Fragment() {

    /** Android ViewBinding */
    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding get() = _fragmentCameraBinding!!

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

//        runBlocking {
//            val job = launch {
//                delay(3000)
//            }
//            job.join()
//            Log.d("zcc", "join")
//        }
    }

}