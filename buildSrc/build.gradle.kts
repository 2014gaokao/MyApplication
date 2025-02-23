plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("greeting") {
            id = "com.example.buildSrc.plugin"
            //Kotlin插件主类
            implementationClass = "com.example.buildSrc.MyCustomPlugin"
        }
    }
}