package edu.zju.framework;

import java.util.*;

import soot.*;


public class Main {

	public static void main(String[] args) {

		List<String> sootArgs = new ArrayList<String>(Arrays.asList(args));

		String Test= "edu.zju.testcase1";  // Stupid code to print out the class under analysis
		System.out.println("Testing " + Test);

		sootArgs.add("-verbose");
		sootArgs.add("-keep-line-number");
		sootArgs.add("-p");
		sootArgs.add("jb");
		sootArgs.add("use-original-names:true");  // Use same variable names as in test program

		// turn off all optimization
//		sootArgs.add("-p");
//		sootArgs.add("jb.dae");
//		sootArgs.add("enabled:false");
//
//		sootArgs.add("-p");
//		sootArgs.add("jop.dae");
//		sootArgs.add("enabled:false");

		sootArgs.add("-cp");
		sootArgs.add(".:/home/chenzhi/Code/IdeaSpace/Faint_code_analyzer/target/test-classes/");
		sootArgs.add("-pp");
		sootArgs.add(Test);

		PackManager.v().getPack("jtp").add(new Transform("jtp.FaintVariableAnalysis", FaintCodeTrans.getInstance()));
		soot.Main.main(sootArgs.toArray(new String[0]));
	}

}
