# @AutoDelegate

## Usage
If you have an interface (or abstract class) for which you mostly want to delegate
to an existing instance, with a few overrides, you can use the `@AutoDelegate` annotation.

The requirements to mark a class as `@AutoDelegate` are:
*  The class must be abstract and must either implement a single interface or 
 inherit from an abstract class.
*  The class must contain a `protected final` variable of the same type as the
inherited interface and named `delegate`.
* All constructors on the class must take an instance of the inherited interface as
the first parameter.

For example:

    @AutoDelegate
    public abstract class DelegatingFoo implements Foo {
      protected final Foo delegate;
      
      public DelegatingFoo(Foo delegate) {
        this.delegate = delegate;
      }
      
      /** Assume this is a method on Foo. */
      @Override
      public int fooMethod1(String arg) {
        // ... implementation
      }
    }

An implementation class named `Auto_[ClassName]_Delegate` will be created in which all remaining
methods are implemented. If your annotated interface/abstract class is an inner class, the name of
the generated class will be `Auto_[OuterClass]_[InnerClass]_Delegate`, with as many segments as your
class has nesting layers.

If you annotate an abstract class that already has implementations for some methods, those
implementations are kept.

If you annotate an abstract class that has non-default constructors, matching constructors are
created on the implementation class which simply delegate to your abstract class.

The resulting functionality is similar to a *Spy* from a mocking environment like
*EasyMock* or *Mockito*, but if you want your overrides to have some state, or just
prefer concrete classes to mocks, this may come in handy
