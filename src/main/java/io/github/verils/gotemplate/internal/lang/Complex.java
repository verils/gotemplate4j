package io.github.verils.gotemplate.internal.lang;

public class Complex {

    private final double real;
    private final double imaginary;

    public Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    public static Complex parseComplex(String text) throws NumberFormatException {
        String trim = text.trim();
        int len = trim.length();
        if (trim.charAt(len - 1) != 'i') {
            throw new NumberFormatException("invalid complex number");
        }

        int operatorPos = trim.indexOf('+');
        if (operatorPos != -1) {
            String realOperand = trim.substring(0, operatorPos);
            String imaginaryOperand = trim.substring(operatorPos + 1, len - 1);

            double real = Double.parseDouble(realOperand);
            double imaginary = Double.parseDouble(imaginaryOperand);
            return new Complex(real, imaginary);
        }


        double imaginary = Double.parseDouble(trim.substring(0, len - 1));
        return new Complex(0, imaginary);
    }

    public double getReal() {
        return real;
    }

    public double getImaginary() {
        return imaginary;
    }

}
