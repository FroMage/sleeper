package fr.epardaud.stephane;

import java.lang.instrument.Instrumentation;

public class SleeperAgent {
    public static void premain(String args, Instrumentation instrumentation){
        ClassSleeper transformer = new ClassSleeper();
        System.err.println("premain!");
        instrumentation.addTransformer(transformer);
      }

}
