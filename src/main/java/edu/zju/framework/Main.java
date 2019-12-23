package edu.zju.framework;

import java.util.*;

import soot.*;


public class Main {

	public static void main(String[] args) {
		List<String> sootArgs = new ArrayList<String>(Arrays.asList(args));

		String Test= "edu.zju.framework.testcase3";										//Stupid code to print out the class under analysis
		System.out.println("Testing " + Test);

		sootArgs.add(Test);
		sootArgs.add(0, "-keep-line-number");  // Use same variable names as in test program
		sootArgs.add("-output-format");
		sootArgs.add("none");
		sootArgs.add("-cp");
		sootArgs.add("/home/chenzhi/Code/IdeaSpace/Faint_code_analyzer/src/main/java:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar");

		PackManager.v().getPack("jtp").add(new Transform("jtp.FaintVariableAnalysis", FaintCodeTrans.getInstance()));
		soot.Main.main(sootArgs.toArray(args));
	}

}
