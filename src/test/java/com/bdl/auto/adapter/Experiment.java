package com.bdl.auto.adapter;

/**
 * Testing classes.
 *
 * @author Ben Leitner
 */
public class Experiment {

  public static void main(String[] args) throws Exception {
    com.sun.tools.javac.Main.main(new String[] {"-proc:only",
        "-processor", "com.bdl.auto.adapter.AutoAdapterAnnotationProcessor",
        "C:\\projects\\java\\auto-adapter\\src\\test\\java\\com\\bdl\\auto\\adapter\\FromInterfaces.java"});
  }
}
