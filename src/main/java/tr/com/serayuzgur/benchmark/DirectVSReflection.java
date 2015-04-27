package tr.com.serayuzgur.benchmark;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import org.abstractmeta.reflectify.MethodInvoker;
import org.abstractmeta.reflectify.Reflectify;
import org.abstractmeta.reflectify.ReflectifyRegistry;
import org.abstractmeta.reflectify.runtime.ReflectifyRuntimeRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class invokes same class same method multiple times
 *
 *
 */
public class DirectVSReflection {


    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {

        Class aClass = Executable.class;
        String methodname = "execute";
        Method method = aClass.getDeclaredMethod(methodname, Integer.class);
        method.setAccessible(true);
        ReflectifyRegistry registry = new ReflectifyRuntimeRegistry();
        Reflectify<Executable> reflectify = registry.get(Executable.class);
        Reflectify.Provider provider = reflectify.getProvider();


        Integer repeat = 100000;

        //For reflectify
        MethodInvoker<Executable, Integer> invoker = reflectify.getMethodInvoker(Integer.class, methodname, Integer.class);

        //For reflectasm
        MethodAccess access = MethodAccess.get(Executable.class);


        //Warm up
        System.out.println("---Warming up---");
        for (int i = 0; i < 11; i++) {
            test(aClass, method, repeat, invoker,provider,access);
        }

        System.out.println("---Starting  up---");
        for (int i = 1; i < 21; i++) {
            System.out.println("----Pass" + i + "---");
            test(aClass, method, repeat, invoker,provider,access);
        }

    }

    private static void test(Class aClass, Method method, int repeat, MethodInvoker<Executable, Integer> invoker,Reflectify.Provider provider,MethodAccess access) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        long start = System.nanoTime();
        Integer result = 0;
        for (int i = 0; i < repeat; i++) {
            result  = (Integer) method.invoke(aClass.newInstance(), repeat);
        }
        System.out.println(result + " Reflection " + (System.nanoTime() - start) / 1000);

        start = System.nanoTime();
        for (int i = 0; i < repeat; i++) {
            result  = (Integer) new Executable().execute(repeat);
        }
        System.out.println(result + " Direct     " + (System.nanoTime() - start) / 1000);

        invoker.getParameterSetter(0).set(repeat);
        start = System.nanoTime();
        for (int i = 0; i < repeat; i++) {
            result  = (Integer) invoker.invoke((Executable) provider.get());
        }
        System.out.println(result + " Reflectify " + (System.nanoTime() - start) / 1000);


        ConstructorAccess<Executable> clazzz = ConstructorAccess.get(Executable.class);
        start = System.nanoTime();
        for (int i = 0; i < repeat; i++) {
            Executable someObject = clazzz.newInstance();
            result = (Integer) access.invoke(someObject, "execute", repeat);
        }
        System.out.println(result + " Reflectasm " + (System.nanoTime() - start) / 1000);

    }

    public static class Executable {

        public Executable() {

        }

        public Integer execute(Integer a) {
            int i = 0;
            for (i = 0; i < a; i++) {

            }
            return i;

        }
    }

}
