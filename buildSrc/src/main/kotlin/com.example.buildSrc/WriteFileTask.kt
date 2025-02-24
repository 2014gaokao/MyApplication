package com.example.buildSrc

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class WriteFileTask : DefaultTask() {
    //任务输入参数
    @Input
    var text = ""

    //任务输出文件
    @OutputFile
    var outputFile: File? = null

    //任务运行时调用的方法
    @TaskAction
    fun writeText() {
        outputFile?.createNewFile()
        outputFile?.writeText(text)
    }
}