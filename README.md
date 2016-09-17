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

An class named `Auto_[ClassName]_Impl` will be created in which all methods are implemented.
If your annotated interface/abstract class is an inner class, the name of the generated class will
be `Auto_[OuterClass]_[InnerClass]_Impl`, with as many segments as your class has nesting layers.

If you annotate an abstract class that already has implementations for some methods, those
 implementations are kept.

If you annotate an abstract class that has non-default constructors, matching constructors are
created on the implementation class which simply delegate to your abstract class.

The options for behavior of the implemented methods are reflected in the
[`ImplOption` enum](https://bdleitner.github.io/autoimpl/com/bdl/auto/impl/ImplOption.html)

* `THROW_EXCEPTION` - the default.  The implemented method throws an`UnsupportedOperationException`.
* `RETURN_DEFAULT_VALUE` - the method returns a default value.
    * 0 for all numeric types.
    * false for booleans.
    * "" for Strings
    * null for everything else.
* `USE_PARENT` - defers to the next higher level.

The top level control is `AutoImpl.value()`, which defaults to `THROW_EXCEPTION`
Finer-grained controls are supported on [`AutoImpl`](https://bdleitner.github.io/autoimpl/com/bdl/auto/impl/AutoImpl.html) itself
for each category of return type:

  * numeric - `AutoImpl.numericImpl()`
  * boolean - `AutoImpl.booleanImpl()`
  * String - `AutoImpl.stringImpl()`
  * void - `AutoImpl.voidImpl()`
  * Everything else - `AutoImpl.objectImpl()`

These can all be set independently.  Any that are not set (or that are set to the default `USE_PARENT`
will defer to `AutoImpl.value()`.

Finally, individual methods can be annotated with [`MethodImpl`](https://bdleitner.github.io/autoimpl/com/bdl/auto/impl/MethodImpl.html).
`MethodImpl` has a single `value` parameter that, if present, overrides the default settings
 from `AutoImpl`.

## Examples / Use Cases
#### Optional Methods
You've written an interface for which you expect some methods to be frequently implemented with a
no-op (e.g. various listeners).

    @AutoImpl(ImplOptions.RETURN_DEFAULT_VALUE)
    public interface SomeInterface {
      void firstMethod(...);
      void secondMethod(...);
      void thirdMethod(...);
      ...
      void umpteenthMethod(...);
    }

Thanks to the `@AutoImpl` annotation, you can write partial implementations:

    public class PartialImpl extends Auto_SomeInterface_Impl { // implements SomeInterface
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
perform some logic, a default implementation class may be simpler:

    public class SomeTestClass {
      @AutoImpl
      abstract static class MyTestImplementation implements LibraryInterface {
        void doSomething(...) {
           // implementation code
        }
      }
      
      @Test
      public void testDoesSomething() {
        LibraryInterface impl = new Auto_SomeTestClass_MyTestImplementation_Impl();
        ... // use this instance for your test.
      }
    }
Note: this will throw exceptions if any other method is called.  To avoid that, 
use `AutoImpl(ImplOptions.RETURN_DEFAULT_VALUE)` or otherwise override the behavior as described
above.

