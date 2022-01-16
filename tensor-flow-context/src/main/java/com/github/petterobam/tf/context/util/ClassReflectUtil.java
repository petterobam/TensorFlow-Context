package com.github.petterobam.tf.context.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类工具类
 *
 * @author 欧阳洁
 * @date 2020/10/15 21:55
 */
public class ClassReflectUtil {

    private static final Logger logger = LoggerFactory.getLogger(ClassReflectUtil.class);

    /**
     * 执行类静态方法
     *
     * @param cls            类
     * @param methodName     方法名
     * @param parameterTypes 参数类型数组
     * @param args           参数
     * @return 结果
     * @throws NoSuchMethodException     没有找到方法异常
     * @throws InvocationTargetException 调用方法异常
     * @throws IllegalAccessException    不能访问异常
     */
    public static Object invokeStaticMethod(Class<?> cls, String methodName, Class<?>[] parameterTypes, Object... args)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = cls.getMethod(methodName, parameterTypes);
        if (method == null) {
            logger.info("execute static method {} Not Found", methodName);
            return null;
        }
        return invokeMethod(null, method, args);
    }

    /**
     * 执行对象方法
     *
     * @param obj            对象
     * @param methodName     方法名
     * @param parameterTypes 参数类型数组
     * @param args           参数
     * @return 结果
     * @throws NoSuchMethodException     没有找到方法异常
     * @throws InvocationTargetException 调用方法异常
     * @throws IllegalAccessException    不能访问异常
     */
    public static Object invokeMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object... args)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (null == obj) {
            return null;
        }
        Method method = obj.getClass().getMethod(methodName, parameterTypes);
        if (method == null) {
            logger.info("execute method {} Not Found", methodName);
            return null;
        }
        return invokeMethod(obj, method, args);
    }

    /**
     * 执行方法
     *
     * @param obj    对象
     * @param method 方法
     * @param args   参数
     * @return 结果
     * @throws IllegalAccessException    不能访问异常
     * @throws InvocationTargetException 调用方法异常
     */
    public static Object invokeMethod(Object obj, Method method, Object... args)
            throws IllegalAccessException, InvocationTargetException {
        logger.info("execute method {}", null != method ? method.toString() : "Not Found");

        if (null == method) {
            return null;
        }

        if (null == obj && !Modifier.isStatic(method.getModifiers())) {
            return null;
        }

        if (null == args || args.length == 0) {
            return method.invoke(obj);
        }

        Class[] paramsClass = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            paramsClass[i] = args[i].getClass();
        }
        return method.invoke(obj, args);
    }

    /**
     * 获取同一路径下所有子类或接口实现类
     *
     * @param cls
     * @return
     * @throws IOException            IO异常
     * @throws ClassNotFoundException 类没找到异常
     */
    public static List<Class<?>> getAllAssignedClass(Class<?> cls) throws IOException,
            ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        for (Class<?> c : getClasses(cls)) {
            if (cls.isAssignableFrom(c) && !cls.equals(c)) {
                classes.add(c);
            }
        }
        return classes;
    }

    /**
     * 获取同一路径下所有子类或接口实现类
     *
     * @param cls 类
     * @return 类集合
     * @throws IOException            IO异常
     * @throws ClassNotFoundException 类没找到异常
     */
    public static <T> List<Class<? extends T>> getAllSubClass(Class<T> cls) {
        List<Class<? extends T>> classes = new ArrayList<>();
        List<Class<?>> subClasses = null;
        try {
            subClasses = getAllAssignedClass(cls);
            for (Class<?> subClass : subClasses) {
                classes.add((Class<? extends T>) subClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * 取得当前类路径下的所有类
     *
     * @param cls 类
     * @return 类集合
     * @throws IOException            IO异常
     * @throws ClassNotFoundException 类没找到异常
     */
    public static List<Class<?>> getClasses(Class<?> cls) throws IOException,
            ClassNotFoundException {
        String pk = cls.getPackage().getName();
        String path = pk.replace('.', '/');
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = classloader.getResource(path);
        return getClasses(new File(url.getFile()), pk);
    }

    /**
     * 迭代查找类
     *
     * @param dir 文件夹路径
     * @param pk  包路径
     * @return 类集合
     * @throws ClassNotFoundException 类没找到异常
     */
    private static List<Class<?>> getClasses(File dir, String pk) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (dir.exists()) {
            System.out.println("-----------------!! 开发环境 !!---------------------------");
        }
        List<String> classNames = getClassName(pk);
        for (String className : classNames) {
            classes.add(Class.forName(className));
        }
        return classes;
    }

    /**
     * 获取某包下（包括该包的所有子包）所有类
     *
     * @param packageName 包名
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName) {
        return getClassName(packageName, true);
    }

    /**
     * 获取某包下所有类
     *
     * @param packageName  包名
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName, boolean childPackage) {
        List<String> fileNames = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String type = url.getProtocol();
            if (type.equals("file")) {
                fileNames = getClassNameByFile(url.getPath(), null, childPackage);
            } else if (type.equals("jar")) {
                fileNames = getClassNameByJar(url.getPath(), childPackage);
            }
        } else {
            fileNames = getClassNameByJars(((URLClassLoader) loader).getURLs(), packagePath, childPackage);
        }
        return fileNames;
    }

    /**
     * 从项目文件获取某包下所有类
     *
     * @param filePath     文件路径
     * @param className    类名集合
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByFile(String filePath, List<String> className, boolean childPackage) {
        List<String> myClassName = new ArrayList<String>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                if (childPackage) {
                    myClassName.addAll(getClassNameByFile(childFile.getPath(), myClassName, childPackage));
                }
            } else {
                String childFilePath = childFile.getPath();
                if (childFilePath.endsWith(".class")) {
                    childFilePath = childFilePath.substring(childFilePath.indexOf("\\classes") + 9, childFilePath.lastIndexOf("."));
                    childFilePath = childFilePath.replace("\\", ".");
                    myClassName.add(childFilePath);
                }
            }
        }

        return myClassName;
    }

    /**
     * 从jar获取某包下所有类
     *
     * @param jarPath      jar文件路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJar(String jarPath, boolean childPackage) {
        List<String> myClassName = new ArrayList<String>();
        String[] jarInfo = jarPath.split("!");
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
        String packagePath = jarInfo[1].substring(1);
        try {
            JarFile jarFile = new JarFile(jarFilePath);
            Enumeration<JarEntry> entrys = jarFile.entries();
            while (entrys.hasMoreElements()) {
                JarEntry jarEntry = entrys.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.endsWith(".class")) {
                    if (childPackage) {
                        if (entryName.startsWith(packagePath)) {
                            entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                            myClassName.add(entryName);
                        }
                    } else {
                        int index = entryName.lastIndexOf("/");
                        String myPackagePath;
                        if (index != -1) {
                            myPackagePath = entryName.substring(0, index);
                        } else {
                            myPackagePath = entryName;
                        }
                        if (myPackagePath.equals(packagePath)) {
                            entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                            myClassName.add(entryName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myClassName;
    }

    /**
     * 从所有jar中搜索该包，并获取该包下所有类
     *
     * @param urls         URL集合
     * @param packagePath  包路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJars(URL[] urls, String packagePath, boolean childPackage) {
        List<String> myClassName = new ArrayList<String>();
        if (urls != null) {
            for (int i = 0; i < urls.length; i++) {
                URL url = urls[i];
                String urlPath = url.getPath();
                // 不必搜索classes文件夹
                if (urlPath.endsWith("classes/")) {
                    continue;
                }
                String jarPath = urlPath + "!/" + packagePath;
                myClassName.addAll(getClassNameByJar(jarPath, childPackage));
            }
        }
        return myClassName;
    }
}
