
# Lessons
1. java.lang.ArrayIndexOutOfBoundsException: 20492 when loading a java class
First check if you add the necessary class files into CLASSPATH, then check if the analysed file is class file.
In my case, I made a mistake to set the input file with source file dir, instead of the class file dir.


2. Turn off optimization of Soot
When running the FaintCodeAnalysis, it can not return the expected result.
I find it is because the generated Jimple file is optimized to remove useless instructions.
In the beginning, I think it is caused by JVM. But I check the generated class file and find it is equivalent to java file.
Furthermore, it could not be caused by JIT, because JIT is only triggered for hot code.
Therefore, the optimization is performed by Soot.

Generally, we can skip any optimization with following code before invoking Soot.
```
    sootArgs.add("-p");
    sootArgs.add("jop.dae");
    sootArgs.add("enabled:false");
```
Above code skips the DeadAssignmentEliminator at the jop (Jimple Optimization Pack) phase of Soot.
You can see all options in this [link](https://soot-build.cs.uni-paderborn.de/public/origin/develop/soot/soot-develop/options/soot_options.htm).

After some exploration, I realize that some optimizations are required when using Jimple as the IR.
From the following code, we can see that `CopyPropagator`, `ConditionalBranchFolder`, `DeadAssignmentEliminator` and
`UnusedLocalEliminator` are always performed.
```
// https://github.com/Sable/soot/blob/822aebb059b2782aacbb58f7594eb693c9701ba8/src/main/java/soot/PackManager.java#L997

  if (produceJimple) {
    Body body = m.retrieveActiveBody();
    // Change
    CopyPropagator.v().transform(body);
    ConditionalBranchFolder.v().transform(body);
    UnreachableCodeEliminator.v().transform(body);
    DeadAssignmentEliminator.v().transform(body);
    UnusedLocalEliminator.v().transform(body);
    PackManager.v().getPack("jtp").apply(body);
    if (Options.v().validate()) {
      body.validate();
    }
    PackManager.v().getPack("jop").apply(body);
    PackManager.v().getPack("jap").apply(body);
    if (Options.v().xml_attributes() && Options.v().output_format() != Options.output_format_jimple) {
      // System.out.println("collecting body tags");
      tc.collectBodyTags(body);
    }
  }
```


