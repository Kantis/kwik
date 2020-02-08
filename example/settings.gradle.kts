rootProject.name = "example"

// This block is only useful to build example against the current version of Kiwk
// It can be removed in case published version of kwik is used
if (file("../settings.gradle.kts").exists()) {
    includeBuild("..") {
        dependencySubstitution {
            substitute(module("com.github.jcornaz.kwik:kwik-evaluator-jvm"))
                .with(project(":evaluator"))

            substitute(module("com.github.jcornaz.kwik:kwik-generator-api-jvm"))
               .with(project(":generator-api"))

            substitute(module("com.github.jcornaz.kwik:kwik-generator-stdlib-jvm"))
                .with(project(":generator-stdlib"))
        }
    }
}
