# Objective

This lab brings together constant folding and reaching definitions on a control flow graph to implement constant propagation.

The first objective of this lab is to implement a `ConstantPropagation` class that for each `MethodDeclaration` does constant propagation to simplify the code. The implementation should be constructed with an `ASTVisitor`. 

The second objective of this lab is to create a test framework to test the implementation. That framework needs to accomplish the following:

  * Black-box functional tests for constant propagation
  * White-box decision coverage for the constant propagation implementation: the visiter and any code including the folding, control flow graph, and reaching definitions --- use mocks where appropriate to get the needed coverage
  * Any additional integration deemed necessary for the system

As before, the test framework should be self-documenting and make clear how the tests are organized and what part of the testing belongs too: white-box, black-box, integration.

# Reading

See [DOM-Visitor](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/DOM-Visitor/) for constant folding and [cfg-rd-lecture.md](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/cfg-rd-lecture.md).

Be sure to carefully read about [constant propagation](https://en.wikipedia.org/wiki/Constant_folding)

# Java Subset

Use the same subset of Java as defined in the prior labs.

# Environment Setup

Rather than copy over files into a new repository from the previous labs, this lab adds the previous labs as dependencies. The cuurent `pom.xml` contains the dependencies with default names. The `pom.xml` files in the prior labs may need to be modified to match the dependency names indicated in the `pom.xml` file for this lab. The `mvn install` command adds the jar file to the Maven cache. Run it for `lab0-contant-propagation` and `lab1-cfg-rd` so these are availabile the this new lab.

If using Docker containers for development, then it may be necessary to put all the projects in a single directory, and then open the docker container in the directory with all the projects. In this way all the projects share the same Maven cache in the container so that Maven is able to install the jar files for the dependencies. Another solution is to have docker mount the local cache in the container. Either solution works fine.

# Constant Propagation

Constant propagation replaces variables references with literals anytime only one definition reaches that reference and that definition assigns the variable to a literal. There is a nice example in the [reading](https://en.wikipedia.org/wiki/Constant_folding).

```java
int a = 30;
int b = 9 - (a / 5);
int c;

c = b * 4;
if (c > 10) {
  c = c - 10;
}
return c * (60 / a);
```

Constant folding is not able to reduce anywhere. Reading definitions shows that only a single definition of ```a``` reaches the initializer for ```b```, and since ```a``` is assigned a literal in that definition the literal is able to replace ```a``` in the initializer. Similarly, the reference to ```a``` in the return can be replaced as well.

```java
int a = 30;
int b = 9 - (30 / 5);
int c;

c = b * 4;
if (c > 10) {
  c = c - 10;
}
return c * (60 / 30);
```

Constant folding reduces the initializer for ```b``` to a literal (the literal is promoted out of the parenthesis-expression as one of the special cases).

```java
int a = 30;
int b = 3;
int c;

c = b * 4;
if (c > 10) {
  c = c - 10;
}
return c * 2;
```

Another analysis on this new version of the code shows that only a single definition of ```b``` reaches the assignment to ```c``` before the if-statement, so that ```b``` can be replaced with the literal. No other replacements are possible.

```java
int a = 30;
int b = 3;
int c;

c = 12;
if (c > 10) {
  c = c - 10;
}
return c * 2;
```

Reaching definitions on the new code replaces the reference to ```c``` in the if-statement, and it replaces the reference to ```c``` in the body of the if-statement. It does not replace the reference to ```c``` in the return statement because two different definitions of ```c``` reach that line. 

```java
int a = 30;
int b = 3;
int c;

c = 12;
if (12 > 10) {
  c = 12 - 10;
}
return c * 2;
```

Constant folding reduces the code more.

```java
int a = 30;
int b = 3;
int c;

c = 12;
c = 2;

return c * 2;
```

Another round of reaching definitions and constant folding give the final code.

```java
int a = 30;
int b = 3;
int c;

c = 12;
c = 2;

return 4;
```

The lab does not require any further reduction, but feel free to go further if desired.

## Algorithm

For each method, repeat until no changes

  1. Constant folding
  2. Construct the control flow graph
  3. Perform reaching definitions
  4. Replace any use of a variable that has a single reaching definition that is a literal with the literal (or if there are multiple definitions that reach the use but both are the same literal do the same)

# Lab Requirements

  1. A minimal set of black-box tests for the functionality of ```ConstantPropagtion```
  2. An implementation of `ConstantPropagation`
  3. A white-box test framework that gives decision coverage for the ```ConstantPropagation``` implementation including it's visitor---use mocks where appropriate and don't include constant folding, control flow graph construction, or reaching definitions
  4. Any additions integration tests deemed needful to be sure everything plays nicely together

It is strongly encouraged to use test driven development that writes a test, writes code to pass the test, and then repeats until the implementation is complete. 

## What to turn in?

Create a pull request when the lab is done. Submit to Canvas the URL of the repository.

# Rubric

| Item | Point Value |
| ------- | ----------- |
| Minimal black-box tests for  ```ConstantPropagation``` with reasonable oracles | 50 |
| ```ConstantPropagation``` Implementation | 30 |
| White-box test framework for the entire implementation | 30 |
| Decision coverage for constant folding, control flow graph construction, reaching definitions, and constant propagation | 60 |
| Self-documenting tests using `@Nested`, `@Tag`, and `@DisplayName` and other names and classes to organize and communicate the test methodology | 20 |
| Adherence to best practices (e.g., no errors, no warnings, documented code, well grouped commits, appropriate commit messages, etc.) | 10 |