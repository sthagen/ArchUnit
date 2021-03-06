package com.tngtech.archunit.core.importer.testexamples.integration;

public class ClassCDependingOnClassB_SuperclassOfX {
    private ClassBDependingOnClassA classB;
    private int cState;

    public ClassCDependingOnClassB_SuperclassOfX(int a, int b, int c) {
        this.cState = a + b + c;
    }

    public int getState() {
        return cState;
    }

    public String getStateFromB() {
        return classB.getStateFromA();
    }

    protected void setStateFrom(String toParse) {
        cState = Integer.parseInt(toParse);
    }
}
