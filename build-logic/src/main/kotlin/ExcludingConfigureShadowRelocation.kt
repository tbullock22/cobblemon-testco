import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.util.jar.JarFile

abstract class ExcludingConfigureShadowRelocation : DefaultTask() {

    @get:Input
   abstract var target: ShadowJar

    @get:Input
    abstract var prefix: String

    @TaskAction
    fun configureRelocation() {
        val packages = mutableSetOf<String>()
        this.target.configurations.forEach { configuration ->
            configuration.files.forEach { file ->
                val jarFile = JarFile(file)
                jarFile.entries().iterator().forEachRemaining { entry ->
                    if (entry.name.endsWith(".class") && entry.name != "module-info.class") {
                        packages += entry.name.substring(0, entry.name.lastIndexOf("/") - 1).replace("/", ".")
                    }
                }
                jarFile.close()
            }
        }
        packages.forEach { packagePath ->
            this.target.relocate(packagePath, "${this.prefix}.$packagePath") {
                exclude("com/cobblemon/**")
            }
        }
    }


}