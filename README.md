# Auto Impl
*Automatic generation of implementation classes for Java*  
**Benjamin Leitner**  
Inspired by the [Google Auto](https://github.com/google/auto) project.

## Overview
Writing default implementation classes is annoying and tedious.  To quote
 [Google Auto](https://github.com/google/auto), sounds like a job for robots.

## Usage
If you have an interface or abstract class for which you'd like to create a default implementation,
simply add the `@AutoImpl` annotation and let the annotation processor do the rest.

Two concrete classes will be generated:

* A *DefaultValue* class that implements all methods by returning a default value for all non-void methods.
Numeric-valued methods return 0, booleans return false, Strings return "", and everything else returns null.
* An *Throwing* class that implements all methods by throwing an `UnsupportedOperationException.`

If you annotate an abstract class that already has implementations for some methods, those
 implementations are kept.

If you annotate an abstract class that has non-default constructors, matching constructors are
created on the implementation class which simply delegate to your abstract class.

The names of the generated classes take the form `AutoAdapter_[ClassName]_[Suffix]` where

* `[ClassName]` is your class name: if your class is an inner class then `[ClassName]` will
be the underscore-separated name of the full class, e.g. `ReallyOuterClass_OuterClass_InnerClass`
* `[Suffix]` is either `DefaultValues` for the DefaultValue implementation or `Throwing` for the Throwing
 implementation.

## Examples / Use Cases
#### Adapter Class
You've written an interface for which you expect some methods to be frequently implemented with a
no-op (e.g. various listeners).

    @AutoAdapter
    public interface SomeInterface {
      void firstMethod(...);
      void secondMethod(...);
      void thirdMethod(...);
      ...
      void umpteenthMethod(...);
    }

Thanks to the `@AutoAdapter` annotation, you can write partial implementations:

    public class PartialImpl extends AutoAdapter_SomeInterface_DefaultValues { // implements SomeInterface
      void thirdMethod(...) {
        ... // implementation
      }

      void seventhMethod(...) {
        ... // implementation
      }
    }
 
and all other methods will simply do nothing when called.
 
#### Testing
Suppose you're testing your class's interaction with a library interface for which generating an
instance is nontrivial.  One approach to solve this is to mock the interface with something like
*EasyMock* or *Mockito*.  But if you want a partial fake or want the implemented methods to actually 
perform some logic, an adapter may be simpler:

    public class SomeTestClass {
      @AutoAdapter
      abstract static class MyTestImplementation implements LibraryInterface {
        void doSomething(...) {
           // implementation code
        }
      }
      
      @Test
      public void testDoesSomething() {
        LibraryInterface impl = new AutoAdapter_SomeTestClass_MyTestImplementation_DefaultValues();
        ... // use this instance for your test.
      }
    }
If you want to make sure that no other methods are called, you can use the 
`AutoAdapter_SomeTestClass_MyTestImplementation_Throwing` implementation instead.

