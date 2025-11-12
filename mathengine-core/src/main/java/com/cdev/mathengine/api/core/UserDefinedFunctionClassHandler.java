package com.cdev.mathengine.api.core;

import com.cdev.mathengine.api.core.exceptions.DynamicCompileTimeException;
import com.cdev.mathengine.api.core.utils.UserFunctionLogger;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.SimpleCompiler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

public class UserDefinedFunctionClassHandler {
    ArrayList<String> calledFunctions = new ArrayList<>();
    private SimpleCompiler compiler;

    private static Object parseInputs(Class<?> type, ArrayList<String> args, int index) {
        if (type == boolean.class) return Boolean.parseBoolean(args.get(index));
        else if (type == int.class) return Integer.parseInt(args.get(index));
        else if (type == long.class) return Long.parseLong(args.get(index));
        else if (type == float.class) return Float.parseFloat(args.get(index));
        else if (type == double.class) return Double.parseDouble(args.get(index));
        else if (type == char.class) return args.get(index).charAt(0);
        else if (type == BigNumber.class) return BigNumber.of(args.get(index));
        else if (type == String.class) return String.valueOf(args.get(index));
        else return null;
    }

    public ClassLoader getClassLoader() {
        return compiler.getClassLoader();
    }

    public void compile(String source) {
        try {
            compiler = new SimpleCompiler();
            compiler.cook(modifySourceCode(source));
        } catch (CompileException e) {
            throw new RuntimeException(e);
        }
    }

    public String modifySourceCode(String source) {
        StringBuilder updatedSourceCode = new StringBuilder();
        updatedSourceCode.insert(0, "import java.util.*; \n");
        updatedSourceCode.insert(0, "import java.math.*; \n");
        updatedSourceCode.insert(0, "import com.cdev.mathengine.api.core.BigNumber; \n");
        updatedSourceCode.insert(0, "import com.cdev.mathengine.api.core.utils.UserFunctionLogger; \n");
        updatedSourceCode.insert(0, "import com.cdev.mathengine.api.core.Functions; \n");
        for (String line : source.split("\n")) {
            if (line.matches(".*[(].*[)].*\\{.*")) {
                updatedSourceCode.append(line.replaceFirst("[)]", ", UserFunctionLogger logger)"));
            } else updatedSourceCode.append(line);
        }

        return updatedSourceCode.toString();
    }

    public String executeUserDefinedFunction(String className, String functionName, ArrayList<String> args, UserFunctionLogger userFunctionLogger) {
        System.out.println("Call to user defined function: " + className + "." + (functionName + args).replace("[", "(").replace("]", ")"));
        calledFunctions.add(functionName);

        try {

            Class<?> dymamicClass = getClassLoader().loadClass(className);
            Method method = Arrays.stream(dymamicClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals(functionName))
                    .findFirst()
                    .orElseThrow(() ->
                            new NoSuchMethodException(functionName));

            // Prepare target instance (or null for static)
            Object instance = Modifier.isStatic(method.getModifiers())
                    ? null
                    : dymamicClass.getDeclaredConstructor().newInstance();

            // Build default args for each parameter
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] argValues = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                argValues[i] = parseInputs(paramTypes[i], args, i);
            }

            argValues[argValues.length - 1] = userFunctionLogger;
            Object result = method.invoke(instance, argValues);
            return String.valueOf(result);

        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | IndexOutOfBoundsException e) {
            throw new DynamicCompileTimeException(e);
        }
    }
}


