package edu.zju.noframework;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import soot.*;

public class MyMain
{
  public static void main(String[] args) throws FileNotFoundException,IOException
  {    
     List<String> sootArgs = new ArrayList<String>(Arrays.asList(args));
   
     sootArgs.add("edu.zju.testcase3");
     sootArgs.add(0, "-keep-line-number");
     sootArgs.add("-output-format");
     sootArgs.add("none");
     sootArgs.add("-cp");
     sootArgs.add("/home/chenzhi/Code/IdeaSpace/Faint_code_analyzer/src/main/java:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar");
     
     PackManager.v().getPack("jtp").add(new Transform("jtp.faintvariableanalysis", Assignment1.v()));
     Main.main(sootArgs.toArray(args));
  }
}
