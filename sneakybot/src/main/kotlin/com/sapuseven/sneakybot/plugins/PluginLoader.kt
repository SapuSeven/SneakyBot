package com.sapuseven.sneakybot.plugins

import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.net.URLClassLoader
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarInputStream

object PluginLoader {
    private val log = LoggerFactory.getLogger(PluginLoader::class.java)

    fun <T> loadPlugins(pluginDir: File, pluginClass: Class<*>): List<T> {
        val pluginJars: Array<File>
        val classLoader: URLClassLoader
        val pluginClasses: List<Class<*>>

        try {
            pluginJars = pluginDir.listFiles(JARFileFilter()) ?: emptyArray()
            classLoader = URLClassLoader(pluginJars.map { it.toURI().toURL() }.toTypedArray())
        } catch (e: SecurityException) {
            log.error("Plugin directory is unreadable! (SecurityException: ${e.message})")
            return emptyList()
        }

        try {
            pluginClasses = extractClassesFromJARs(pluginJars, classLoader, pluginClass)
        } catch (e: IOException) {
            log.error("IOException while loading plugins: ${e.message}")
            return emptyList()
        }

        val plugins = ArrayList<T>(pluginClasses.size)
        for (plugin in pluginClasses)
            try {
                @Suppress("UNCHECKED_CAST") // already checked in isPluggableClass
                plugins.add(plugin.getDeclaredConstructor().newInstance() as T)
            } catch (e: NoSuchMethodException) {
                log.error("Plugin \"${plugin.name}\" is missing a constructor! (${e.message})")
            } catch (e: IllegalAccessException) {
                log.error("Plugin \"${plugin.name}\" has no accessible constructor! (${e.message})")
            } catch (e: IllegalArgumentException) {
                log.error("Plugin \"${plugin.name}\" has unexpected constructor parameters! (${e.message})")
            } catch (e: InstantiationException) {
                log.error("Plugin \"${plugin.name}\" has an abstract constructor! (${e.message})")
            } catch (e: InvocationTargetException) {
                log.error("Plugin \"${plugin.name}\" constructor threw an exception! (${e.message})")
            } catch (e: ExceptionInInitializerError) {
                log.error("Plugin \"${plugin.name}\" could not be initialized! (${e.message})")
            }

        return plugins
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