package com.cdev.mathengine.api.core;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class BigNumber implements Comparable {

    static MathContext defaultMathContext;
    BigDecimal value;
    MathContext commonMathContext;


    public BigNumber(String value, MathContext... mathContext) {
        this.value = new BigDecimal(value, (mathContext.length > 0) ? mathContext[0] : MathContext.UNLIMITED);
        commonMathContext = getMathContext(mathContext);
    }

    public static BigNumber pi(MathContext... mathContext) {
        if (mathContext.length == 0) {
            mathContext = new MathContext[1];
            mathContext[0] = new MathContext(100);
        }
        return new BigNumber(BigDecimalMath.pi(mathContext[0]).toPlainString());
    }

    public static void setDefaultMathContext(MathContext mathContext) {
        defaultMathContext = mathContext;
    }

    public static void setDefaultMathContext(Integer precision) {
        defaultMathContext = new MathContext(precision);
    }

    public static void removeDefaultMathContext() {
        defaultMathContext = null;
    }

    public static BigNumber of(Object value, MathContext... mathContext) {
        List<String> inputs = parseInputs(value);
        return new BigNumber(inputs.get(0), mathContext);
    }

    public static List<String> parseInputs(Object... objects) {
        List<String> inputs = new ArrayList<>();

        for (Object obj : objects) {
            if (obj instanceof BigNumber val)
                inputs.add(val.toString());
            else if (obj instanceof BigDecimal val)
                inputs.add(val.toPlainString());
            else inputs.add(String.valueOf(obj));
        }

        return inputs;
    }

    public BigNumber add(Object addend, MathContext... mathContext) {
        List<String> inputs = parseInputs(addend);

        BigDecimal bd = new BigDecimal(inputs.get(0));
        MathContext mc = getMathContext(mathContext);

        return new BigNumber(value.add(bd, mc).toPlainString(), commonMathContext);
    }

    public BigNumber subtract(Object subtrahend, MathContext... mathContext) {
        List<String> inputs = parseInputs(subtrahend);

        BigDecimal bd = new BigDecimal(inputs.get(0));
        MathContext mc = getMathContext(mathContext);

        return new BigNumber(value.subtract(bd, mc).toPlainString(), commonMathContext);
    }

    public BigNumber multiply(Object multiplier, MathContext... mathContext) {
        List<String> inputs = parseInputs(multiplier);

        BigDecimal bd = new BigDecimal(inputs.get(0));
        MathContext mc = getMathContext(mathContext);

        return new BigNumber(value.multiply(bd, mc).toPlainString(), commonMathContext);
    }

    public BigNumber divide(Object divisor, MathContext... mathContext) {
        List<String> inputs = parseInputs(divisor);

        BigDecimal bd = new BigDecimal(inputs.get(0));
        MathContext mc = getMathContext(mathContext);

        return new BigNumber(value.divide(bd, mc).toPlainString(), commonMathContext);
    }

    public BigNumber modulo(Object modulo, MathContext... mathContext) {
        List<String> inputs = parseInputs(modulo);

        BigDecimal bd = new BigDecimal(inputs.get(0));
        MathContext mc = getMathContext(mathContext);

        return new BigNumber(value.remainder(bd, mc).toPlainString(), commonMathContext);
    }

    public BigNumber power(Object exponent, MathContext... mathContext) {
        List<String> inputs = parseInputs(exponent);
        String exponentVal = inputs.get(0);

        MathContext mc = getMathContext(mathContext);
        return new BigNumber(BigDecimalMath.pow(value, new BigDecimal(exponentVal), mc).toPlainString(), commonMathContext);
    }

    public BigNumber squareRoot(MathContext... mathContext) {
        MathContext mc = getMathContext(mathContext);
        return new BigNumber(value.sqrt(mc).toPlainString(), commonMathContext);
    }

    public BigNumber cubeRoot(MathContext... mathContext) {
        MathContext mc = getMathContext(mathContext);
        return new BigNumber(BigDecimalMath.root(value, BigDecimal.valueOf(3), mc).toPlainString(), commonMathContext);
    }

    public BigNumber nthRoot(Object degree, MathContext... mathContext) {
        List<String> inputs = parseInputs(degree);
        String degreeVal = inputs.get(0);

        MathContext mc = getMathContext(mathContext);
        return new BigNumber(BigDecimalMath.root(value, new BigDecimal(degreeVal), mc).toPlainString(), commonMathContext);
    }

    public BigNumber absolute(MathContext... mathContext) {
        MathContext mc = getMathContext(mathContext);
        return new BigNumber(value.abs(mc).toPlainString(), commonMathContext);
    }

    public BigNumber factorial() {
        BigInteger n = value.toBigInteger();

        if (n.compareTo(BigInteger.ZERO) < 0)
            throw new ArithmeticException("Value should be greater than 0.");

        BigInteger iteration = BigInteger.ZERO;
        BigInteger result = BigInteger.ONE;

        while (iteration.compareTo(n) < 0) {
            result = result.multiply(iteration.add(BigInteger.ONE));
            iteration = iteration.add(BigInteger.ONE);
        }

        return new BigNumber(result.toString(), commonMathContext);
    }

    public BigNumber percentage(Object percent, MathContext... mathContext) {
        List<String> inputs = parseInputs(percent);
        String percentVal = inputs.get(0);

        MathContext mc = getMathContext(mathContext);
        return new BigNumber(value.multiply(new BigDecimal(percentVal).divide(BigDecimal.valueOf(100), MathContext.UNLIMITED), mc).toPlainString(), commonMathContext);
    }

    public BigNumber reciprocal(MathContext... mathContext) {
        MathContext mc = getMathContext(mathContext);
        return new BigNumber(BigDecimal.ONE.divide(value, mc).toPlainString(), commonMathContext);
    }

    public BigNumber logN(Object base, MathContext... mathContext) {
        List<String> inputs = parseInputs(base);
        String baseVal = inputs.get(0);

        MathContext mc = getMathContext(mathContext);
        return new BigNumber(BigDecimalMath.log(value, mc).divide(BigDecimalMath.log(new BigDecimal(baseVal, mc), mc), mc).toPlainString(), commonMathContext);
    }

    public BigNumber exp(MathContext... mathContext) {
        MathContext mc = getMathContext(mathContext);
        return new BigNumber(BigDecimalMath.exp(value, mc).toPlainString(), commonMathContext);
    }

    public MathContext getMathContext(MathContext... mathContext) {
        return mathContext.length > 0 ? mathContext[0] : (commonMathContext != null ? commonMathContext : (defaultMathContext != null ? defaultMathContext : MathContext.UNLIMITED));
    }

    public BigNumber setMathContext(MathContext mathContext) {
        this.commonMathContext = mathContext;
        return this;
    }

    public BigNumber setMathContext(Integer precision) {
        this.commonMathContext = new MathContext(precision);
        return this;
    }

    public BigNumber removeMathContext() {
        this.commonMathContext = null;
        return this;
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || getClass() != other.getClass()) return false;
        return this.value.compareTo(((BigNumber) other).value) == 0;
    }

    @Override
    public int compareTo(Object other) {
        return this.value.compareTo(((BigNumber) other).value);
    }
}
