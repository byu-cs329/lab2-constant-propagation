# Objective

This lab brings together constant folding and reaching definitions on a control flow graph to implement constant propagation.

The first objective of this lab is to implement a `ConstantPropagation` class that for each `MethodDeclaration` does constant propagation to simplify the code. The implementation should be constructed with an `ASTVisitor` to find and replace a variable use with a constant based on the reaching definitions analysis. The visitor only looks at a statement in isolation, and the control flow graph is used to be sure each statement is considered. 

The second objective of this lab is to create a test framework to test the implementation. That framework needs to accomplish the following:

  * Black-box functional test constant propagation
  * White-box decision coverage test constant propagation: the visitor and any code needed to implement this lab --- use mocks where appropriate to get the needed coverage.
  * Any additional integration deemed necessary for the system

As before, the test framework should be self-documenting and make clear how the tests are organized and what part of the testing belongs too: white-box, black-box, integration.

# Reading

See [DOM-Visitor](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/DOM-Visitor/) for constant folding and [cfg-rd-lecture.md](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/cfg-rd-lecture.md).

Be sure to carefully read about [constant propagation](https://en.wikipedia.org/wiki/Constant_folding)

# Java Subset

Use the same subset of Java as defined in the prior labs.

# Environment Setup

Rather than copy over files into a new repository from the previous labs, this lab adds the previous labs as dependencies. The current `pom.xml` contains the dependencies with default names. 

```xml 
<dependency>
  <groupId>edu.byu.cs329</groupId>
  <artifactId>lab1-cfg-rd</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>

<dependency>
  <groupId>edu.byu.cs329</groupId>
  <artifactId>lab0-constant-folding</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

The `mvn install` command builds and names a jar file for the project according to the `pom.xml` file and installs that jar in the local Maven cache. Run it for `lab0-contant-propagation` and `lab1-cfg-rd` so that these are available to use in this new lab. Notice that the `pom.xml` files in the prior labs may need to be modified to match the dependency names indicated in the `pom.xml` file for this lab, so be sure the `groupID`, `artifactID`, and `version` in the `pom.xml` files match what is built and cached by `mvn install` for each project. 

If using Docker containers for development, then it may be necessary to put all the projects in a single directory, and then open the docker container in the directory with all the projects. In this way all the projects share the same Maven cache in the container so that Maven is able to install the jar files for the dependencies. Another solution is to have docker mount the local cache in the container. Either solution works fine.

If a solution to a prior project is needed, then please contact the instructor directly.

# Constant Propagation

Constant propagation replaces variables references with literals anytime only one definition reaches that reference and that definition assigns the variable to a literal. There is a nice example in the [reading](https://en.wikipedia.org/wiki/Constant_folding).

```java
int a = 30;
int b = 9 + (a + 5);
int c;

c = b + 4;
if (10 < c) {
  c = c + 10;
}
return c + (60 + a);
```

Constant folding is not able to reduce anywhere. Reaching definitions shows that only a single definition of ```a``` reaches the initializer for ```b```, and since ```a``` is assigned a literal in that definition the literal is able to replace ```a``` in the initializer. Similarly, the reference to ```a``` in the return can be replaced as well.

```java
int a = 30;
int b = 9 + (30 + 5);
int c;

c = b + 4;
if (10 < c) {
  c = c + 10;
}
return c + (60 + 30);
```

Constant folding reduces the initializer for ```b``` to a literal (the literal is promoted out of the parenthesis-expression as one of the special cases).

```java
int a = 30;
int b = 44;
int c;

c = b + 4;
if (10 < c) {
  c = c + 10;
}
return c + 90;
```

Another analysis on this new version of the code shows that only a single definition of ```b``` reaches the assignment to ```c``` before the if-statement, so that ```b``` can be replaced with the literal. No other replacements are possible.

```java
int a = 30;
int b = 44;
int c;

c = 48;
if (10 < c) {
  c = c + 10;
}
return c + 90;
```

Reaching definitions on the new code replaces the reference to ```c``` in the if-statement, and it replaces the reference to ```c``` in the body of the if-statement. It does not replace the reference to ```c``` in the return statement because two different definitions of ```c``` reach that line. 

```java
int a = 30;
int b = 44;
int c;

c = 48;
if (10 < 48) {
  c = 48 + 10;
}
return c + 90;
```

Constant folding reduces the code more.

```java
int a = 30;
int b = 44;
int c;

c = 48;
c = 58;

return c + 90;
```

Another round of reaching definitions and constant folding give the final code.

```java
int a = 30;
int b = 44;
int c;

c = 48;
c = 58;

return 148;
```

The lab does not require any further reduction, but feel free to go further if desired.

## Algorithm

For each method, repeat until no changes

  1. Constant folding
  2. Construct the control flow graph
  3. Perform reaching definitions
  4. Replace any use of a variable that has a single reaching definition that is a literal with the literal (or if there are multiple definitions that reach the use but both are the same literal)

# Lab Requirements

  1. A minimal set of black-box tests for the functionality of ```ConstantPropagtion```
  2. An implementation of `ConstantPropagation`
  3. Any additional tests for white-box decision coverage (e.g., branch coverage) as reported by Jacoco for the ```ConstantPropagation``` class and any new classes created in this lab to implement the `ConstantPropagation` class. Assertion statements are exempt from the coverage report, so if a decision is not covered, and it is not due to an assertion, then that lack of coverage must be justified in some way. 

It is strongly encouraged to use test driven development that writes a test, writes code to pass the test, and then repeats until the implementation is complete. 

## What to turn in?

Create a pull request when the lab is done. Submit to Canvas the URL of the pull request.

# Block Folding

Consider the following code.

```java
int name(int i) {
  i = 10;
  if (i < 20) {
     i = 20;
  } 
}
```

Constant propagation replaces the use of `i` in the if-statement with the literal `10` and then constant folding reduces the if-statement.

```java
int name(int i) {
  i = 10;
  {
    i = 20;
  } 
}
```

The `ControlFlowGraphBuilder` constructs an incorrect graph on this version of the code because it assumes that block-statements are always attached to either a method declaration, an if-statement, or a while-statement. In the above, that is not the case. The easiest solution is to add a new folding option to constant folding that reduces nested blocks. Variable **shadowing is not allowed** in the Java subset for this class, so folding nested blocks does not cause scoping issues. Block folding inspects every statement in a block, and if a statement in a block is itself a block, then all the statements in that nested block are lifted up to be part of the current block as in the following:

```java
int name(int i) {
  i = 10;
  i = 20; 
}
```

[BlockFolding.java](BlockFolding.java) in this directory is an implementation of block folding that may be used freely to remove nested blocks. It will need to be added to the list of folding techniques employed by the `ConstantFolding` implementation. And, as a fair warning, it is provided *as is* with no guarantee of correctness; although, there are no known defects at this point in time.

# Jacoco

Jacoco is already configured in the `pom.xml` file for this project. The configuration makes the `mvn test` command automatically generate a `./target/jacoco.exec` binary file with white-box coverage data. A human readable version of the data is created with the `mvn jacoco:report` command. This command creates the `./target/site/jacoco/index.html` report that is easily navigated to see decision (e.g., branch) coverage for `ConstantPropagation` and any new classes that are a part of this project. Coverage is based only on the defined and run tests. 

It is permissible to modify the `ConstantPropagation` class to make it easier to achieve branch coverage. It may be necessary to use mocks to cover some of the more difficult decisions. 

Some further work must take place to read the report if working in a container as the container file-system may not be visible to the browser on the host operating system. It is possible to have Jacoco generate different reports such as a CSV report that can be inspected in the text editor. See the Jacoco documentation for details.

# Rubric

| Item | Point Value |
| ------- | ----------- |
| Minimal black-box tests for  ```ConstantPropagation``` with reasonable oracles | 75 |
| ```ConstantPropagation``` Implementation | 75 |
| Additional tests for decision coverage | 30 |
| Adherence to best practices (e.g., no errors, no warnings, documented code, well grouped commits, appropriate commit messages, etc.) | 20 |
