package com.example.buildSrc

import org.gradle.api.Plugin
import org.gradle.api.Project

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
    }
}