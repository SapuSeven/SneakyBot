package com.sapuseven.sneakybot.plugins

import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarInputStream

object PluginLoader {
    private val log = LoggerFactory.getLogger(PluginLoader::class.java)

    // TODO: Determine which exception is thrown for which error and document the function
    @Throws(MalformedURLException::class, NullPointerException::class)
    fun loadCommands(pluginDir: File): List<PluggableCommand> {
        val pluginJars = pluginDir.listFiles(JARFileFilter())
        val cl = URLClassLoader(fileArrayToURLArray(pluginJars!!))
        val pluginClasses: List<Class<*>>
        try {
            pluginClasses = extractClassesFromJARs(pluginJars, cl, PluggableCommand::class.java)
        } catch (e: IOException) {
            log.warn("Error loading commands: ${e.message}")
            return emptyList()
        }

        val plugins = ArrayList<PluggableCommand>(pluginClasses.size)
        for (plugin in pluginClasses)
            try {
                plugins.add((plugin.getDeclaredConstructor().newInstance() as? PluggableCommand) ?: throw InstantiationException("Cast failed"))
            } catch (e: InstantiationException) {
                log.error("Can't instantiate plugin \"${plugin.name}\": ${e.message}")
            } catch (e: IllegalAccessException) {
                log.error("IllegalAccess for plugin \"${plugin.name}\": ${e.message}")
            }

        return plugins
    }

    // TODO: Determine which exception is thrown for which error and document the function
    @Throws(MalformedURLException::class, NullPointerException::class)
    fun loadServices(pluginDir: File): List<PluggableService> {
        val pluginJars = pluginDir.listFiles(JARFileFilter())
        val cl = URLClassLoader(fileArrayToURLArray(pluginJars!!))
        val pluginClasses: List<Class<*>>
        try {
            pluginClasses = extractClassesFromJARs(pluginJars, cl, PluggableService::class.java)
        } catch (e: IOException) {
            log.warn("Error loading services: ${e.message}")
            return emptyList()
        }

        val plugins = ArrayList<PluggableService>(pluginClasses.size)
        for (plugin in pluginClasses)
            try {
                plugins.add((plugin.getDeclaredConstructor().newInstance() as? PluggableService) ?: throw InstantiationException("Cast failed"))
            } catch (e: InstantiationException) {
                log.error("Can't instantiate plugin \"${plugin.name}\": ${e.message}")
            } catch (e: IllegalAccessException) {
                log.error("IllegalAccess for plugin \"${plugin.name}\": ${e.message}")
            }

        return plugins
    }

    @Throws(MalformedURLException::class, NullPointerException::class)
    private fun fileArrayToURLArray(files: Array<File>): Array<URL?> {
        val urls = arrayOfNulls<URL>(files.size)
        for (i in files.indices)
            urls[i] = files[i].toURI().toURL()
        return urls
    }

    @Throws(IOException::class)
    private fun extractClassesFromJARs(jars: Array<File>, cl: ClassLoader, pluginClass: Class<*>): List<Class<*>> {
        val classes = ArrayList<Class<*>>()
        for (jar in jars)
            classes.addAll(extractClassesFromJAR(jar, cl, pluginClass))
        return classes
    }

    @Throws(IOException::class)
    private fun extractClassesFromJAR(jar: File, cl: ClassLoader, pluginClass: Class<*>): List<Class<*>> {
        val classes = ArrayList<Class<*>>()
        val jarInputStream = JarInputStream(FileInputStream(jar))
        var ent: JarEntry? = jarInputStream.nextJarEntry
        while (ent != null) {
            if (ent.name.toLowerCase().endsWith(".class"))
                try {
                    val cls = cl.loadClass(ent.name.substring(0, ent.name.length - 6).replace('/', '.'))
                    if (isPluggableClass(cls, pluginClass))
                        classes.add(cls)
                } catch (e: ClassNotFoundException) {
                    log.error("Can't load Class ${ent.name}: ${e.message}")
                }
            ent = jarInputStream.nextJarEntry
        }

        jarInputStream.close()
        return classes
    }

    private fun isPluggableClass(cls: Class<*>, pluginClass: Class<*>): Boolean {
        for (i in cls.interfaces)
            if (i == pluginClass)
                return true
        return false
    }

    private class JARFileFilter : FileFilter {
        override fun accept(f: File): Boolean {
            return f.name.toLowerCase().endsWith(".jar")
        }
    }
}