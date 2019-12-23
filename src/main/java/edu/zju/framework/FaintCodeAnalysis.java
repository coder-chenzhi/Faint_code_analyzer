package edu.zju.framework;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JInvokeStmt;
import soot.tagkit.LineNumberTag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.util.Chain;


public  class FaintCodeAnalysis extends BackwardFlowAnalysis {
	
	private FlowSet localVariables; 

	public	FaintCodeAnalysis (UnitGraph g)
	{
	  super(g);
	  
	    Chain locs=g.getBody().getLocals();
		localVariables=new ArraySparseSet();
		Iterator it=locs.iterator();
		while(it.hasNext()) {
		    Local loc=(Local) it.next();
		    localVariables.add(loc);
		}
	    
	  doAnalysis();

	}
	
	protected Object newInitialFlow()		// Used to initialize the in and out sets for each node. In our case, all variables are considered to be faint
	{
		FlowSet ret=new ArraySparseSet();		//Type of flowset
		localVariables.copy(ret);
		return ret;
	
	}
	

	protected Object entryInitialFlow() {	//Returns a flow set representing the initial set of the entry node
		FlowSet ret=new ArraySparseSet();
		localVariables.copy(ret);			//Will be same as newInitialFlow
		return ret;
	}
	
	
	protected void merge(Object in1, Object in2, Object out)
	{
		FlowSet inSet1 = (FlowSet) in1,
				inSet2 = (FlowSet) in2,
		        outSet = (FlowSet) out;
		inSet1.intersection(inSet2, outSet);
	}
	
	
	protected void copy(Object source, Object dest) {
		FlowSet sourceSet = (FlowSet) source,
		destSet = (FlowSet) dest;
		sourceSet.copy(destSet);
	}
    
	       
    protected void flowThrough(Object outValue, Object unit,Object inValue)	{
		FlowSet in  = (FlowSet) inValue,
				out = (FlowSet) outValue;
		Stmt    s   = (Stmt)    unit;
		out.copy( in );
		Iterator useBox = s.getUseBoxes().iterator(),  //Right hand side variables
				 defBox = s.getDefBoxes().iterator();  //Left hand side variables
		boolean flag = true;
		
		if(s instanceof JInvokeStmt){       //Nested if-else of Kill functions 
			
			while (useBox.hasNext()){       //Const Kill
				final ValueBox box1 = (ValueBox) useBox.next();
				Value use = box1.getValue();
				if(use instanceof Local){
					in.remove(use);
				}
			}
		}

		else if(s instanceof AssignStmt){   //Dep Kill
			while (defBox.hasNext()){

				final ValueBox box = (ValueBox) defBox.next();
				Value def = box.getValue();
				if(def instanceof Local){
					if(in.contains(def)){   //Checks if the left hand side variable belongs in the In set
						flag=false;
					}
				}	
			}
			if(flag){   //Do not run this if the variable is in the In set
				while (useBox.hasNext()){   //get all the right hand side variables
					final ValueBox box1 = (ValueBox) useBox.next();
					Value use = box1.getValue();
					if(use instanceof Local){
						in.remove(use);
					}
				}
			}
		}
		
		else{ 									//for all other types of statements, kill the right hand side variables
			while (useBox.hasNext()){
				final ValueBox box1 = (ValueBox) useBox.next();
				Value use = box1.getValue();
				if(use instanceof Local){
					in.remove(use);
				}
			}
		}
			
		if(s instanceof AssignStmt){    //Now on to Generate function
		    Boolean flag2=true;
			while (defBox.hasNext()){   //ConstGen

				final ValueBox box = (ValueBox) defBox.next();
				Value def = box.getValue();  //Put all left hand variables to valuebox
				if(def instanceof Local){
					while (useBox.hasNext()){
						final ValueBox box1 = (ValueBox) useBox.next();
						Value use = box1.getValue();  //Put all right hand side variables to valuebox
						if(use instanceof Local){
							if(use==def){           //statements like x=x+1
								flag2=false;       //Don't add anything
							}
						}
					}
					
					if(flag2){                  //If left hand side variable is not available in the Operand section
						in.add(def);			   //Generate it in InSet!!!	
					}
				}
			}
		}
	}
}


class FaintCodeTrans extends BodyTransformer {

	private static FaintCodeTrans instance = new FaintCodeTrans();

	private HashMap results = new HashMap();

	public static FaintCodeTrans getInstance() {
		return instance;
	}

	protected void internalTransform(Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) {
		System.out.println("\n\nAnalysis of " + b.getMethod().getName() + "()  :");    //Print method name. Just User Friendly way!

		UnitGraph g = new ExceptionalUnitGraph(b);

		FaintCodeAnalysis an = new FaintCodeAnalysis(g); 			   //Run the analysis

		Iterator sIt = b.getUnits().iterator();							//Get next units corresponding to body from the graph| Use iterator method
		Unit u = null;													//Initialize units

		while( sIt.hasNext() ) {
			u = (Unit)sIt.next();											//Fill up the the units of the graph
			FlowSet FaintVariables = (FlowSet) an.getFlowAfter(u);
			Iterator variableIt = FaintVariables.iterator();			//Make an iterator on flowset

			while( variableIt.hasNext() ) {
				Value variable = (Value)variableIt.next();
				Iterator defBox = u.getDefBoxes().iterator();			//Get all left hand side variables

				while (defBox.hasNext()){
					final ValueBox box1 = (ValueBox) defBox.next();
					Value def = box1.getValue();						//put all the left hand side variables in def

					if(variable == def){								//Check between flowset and def

						if(!variable.toString().startsWith("$") && !variable.toString().equals("this")) {  //Parse the line number tag jimple implementation for local and aliased variables
							LineNumberTag tag = (LineNumberTag)u.getTag("LineNumberTag"); 				//get tags|line numbers of the code

							if(results.containsKey(u.toString()))										//containskey is a hashmap function which relates to index
								results.put(u.toString(), results.get(u.toString())+", "+tag);			//get tag if line is mapped to hashmap function
							else
								results.put(u.toString(), tag);
						}
					}
				}
			}
		}

		Set<String> set = results.keySet();
		Iterator<String> i = set.iterator();
		while(i.hasNext()){
			String unit = i.next().toString();
			System.out.println("\nFaint code at line"+" "+results.get(unit));
			System.out.println(unit);
		}
	}

}

