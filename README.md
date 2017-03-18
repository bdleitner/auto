# Auto
*Automatic generation of implementation classes for Java*  
**Benjamin Leitner**  
Inspired by the [Google Auto](https://github.com/google/auto) project.

## Overview
Writing default implementation classes is annoying and tedious.  To quote
 [Google Auto](https://github.com/google/auto), sounds like a job for robots.

This project makes available two annotations, with annotation processors for
generating default implementations of Java interfaces.

## [@AutoImpl](impl.md)
Provides a default implementation that throws exception or returns default
values for each method.  Can be applied to an abstract class so as to only
provide default implementations for abstract methods.

## [@AutoDelegate](delegate.md)
Provides an implementation of an abstract class that inherits from a single interface
or nontrivial superclass.  The implementation takes an instance of that
interface on construction and implements all abstract methods to delegate
to that instance.  Methods implemented on the abstract class are not overridden.
