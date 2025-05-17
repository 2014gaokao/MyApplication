package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.example.myapplication.databinding.FragmentCaixukunBinding

class CaiXuKunFragment : Fragment() {

    companion object {
        fun newInstance() = CaiXuKunFragment()
    }

    private var _fragmentOpenglBinding: FragmentCaixukunBinding? = null

    private val fragmentOpenglBinding get() = _fragmentOpenglBinding!!

    private val viewModel: CaiXuKunViewModel by viewModels()
    //private lateinit var viewModel: OpenGLViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentOpenglBinding = FragmentCaixukunBinding.inflate(inflater, container, false)
        return fragmentOpenglBinding.root
        //return inflater.inflate(R.layout.fragment_opengl, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //viewModel = ViewModelProvider(this).get(OpenGLViewModel::class.java)
        viewModel.user.observe(viewLifecycleOwner, Observer { newData -> Log.d("zcc", newData.name) })

        fragmentOpenglBinding.viewModel = viewModel
        fragmentOpenglBinding.lifecycleOwner = this

        val src: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.cxk);
        val copy: Bitmap = src.copy(Bitmap.Config.ARGB_8888, false)
        JNILoader().stringFromJNI(copy)
        fragmentOpenglBinding.image.setImageBitmap(copy)
        fragmentOpenglBinding.image.setOnClickListener {
            viewModel.updateUser("蔡徐坤")
        }
    }
}