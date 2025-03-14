package com.example.buildSrc

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import java.io.File

//https://juejin.cn/post/7371028010067771429#heading-17
//https://juejin.cn/column/7123935861976072199  Gradle基础到进阶
class MyCustomPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("abandon") {
            enabled = true
            dependsOn("abort")
            doFirst {
                println("abandon doFirst")
            }
            doLast {
                println("abandon doLast")
            }
        }

        project.tasks.register("abort") {
            onlyIf {
                project.providers.gradleProperty("android.useAndroidX").isPresent
            }
            doFirst {
                println("abort doFirst")
            }
            doLast {
                println("abort doLast")
            }
        }

        project.tasks.register("writeFileTask", WriteFileTask::class) {
            text = "why shall we need to work?"
            outputFile = File(project.projectDir, "myFile.txt")
        }
    }
}