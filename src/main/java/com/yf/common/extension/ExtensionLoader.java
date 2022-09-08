package com.yf.common.extension;

import com.yf.common.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/4 15:41
 * @version: 1.0.0
 * @url:
 */
@Slf4j
public class ExtensionLoader<T> {
    private static final String SERVICE_DICTORY = "META-INF/extensions/";

    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    private final Class<?> type;

    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private ExtensionLoader(Class<?> type){
        this.type = type;
    }

    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type){
        if (type == null){
            throw new IllegalArgumentException("Extension type should not be null.");
        }
        if (! type.isInterface()){
            throw new IllegalArgumentException("Extension type must be an interface.");
        }
        if ( type.getAnnotation(SPI.class) == null){
            throw new IllegalArgumentException("Extension type must be annotated by @SPI");
        }

        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null){
            EXTENSION_LOADERS.putIfAbsent(type,new ExtensionLoader<>(type));
            extensionLoader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    public T getExtension(String name){
        if (null == name || name.equals("")) {
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }
        if (StringUtil.isBlank(name)){
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }

        Holder<Object> objectHolder = cachedInstances.get(name);
        if (objectHolder == null){
            // create a objectHolder
            cachedInstances.putIfAbsent(name,new Holder<>());
            objectHolder = cachedInstances.get(name);
        }
        Object instance = objectHolder.get();
        if (instance == null){
            synchronized (objectHolder){
                instance = objectHolder.get();
                if (instance == null){
                    // create an instance and put it into objectHolder
                    instance = createExtension(name);
                    objectHolder.set(instance);
                }
            }
        }

        return (T) instance;

    }

    private T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("No such extension of name " + name);
        }
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if (instance == null){
            try{
                synchronized (EXTENSION_INSTANCES){
                    instance = (T) EXTENSION_INSTANCES.get(clazz);
                    if (instance == null){
                        EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                        instance = (T) EXTENSION_INSTANCES.get(clazz);
                    }
                }
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    private Map<String, Class<?>> getExtensionClasses(){
        Map<String, Class<?>> stringClassMap = cachedClasses.get();
        if (stringClassMap == null){
            synchronized (cachedClasses){
                stringClassMap = cachedClasses.get();
                if (stringClassMap == null){
                    stringClassMap = new ConcurrentHashMap<>();
                    // load classes
                    loadDirectory(stringClassMap);
                    cachedClasses.set(stringClassMap);
                }
            }
        }
        return stringClassMap;
    }

    private void loadDirectory(Map<String, Class<?>> stringClassMap) {
        String fileName = ExtensionLoader.SERVICE_DICTORY + type.getName();

        ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
        Enumeration<URL> resources = null;
        try {
            resources = classLoader.getResources(fileName);
            if (resources != null){
                while (resources.hasMoreElements()){
                    URL url = resources.nextElement(); // 文件中的每条记录
                    loadResource(stringClassMap,url,classLoader); // 真正负责装载类的函数
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadResource(Map<String, Class<?>> stringClassMap, URL url, ClassLoader classLoader) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = bufferedReader.readLine()) != null){
                // 处理字符串格式的每条记录
                final int ci = line.indexOf('#');
                if (ci > 0){
                    line = line.substring(0,ci);
                }
                line = line.trim();

                if (line.length() > 0){
                    try {
                        final int ei = line.indexOf('=');
                        String name = line.substring(0,ei).trim();
                        String clazzName = line.substring(ei + 1).trim();
                        Class<?> clazz = classLoader.loadClass(clazzName);
                        stringClassMap.putIfAbsent(name,clazz);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
