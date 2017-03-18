package com.bdl.auto;

import com.bdl.annotation.processing.model.ClassMetadata;
import com.bdl.auto.delegate.AutoDelegate;
import com.bdl.auto.delegate.processor.AutoDelegateWriter;
import com.google.testing.compile.CompilationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.PrintWriter;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class AutoDelegateTest {

  @Rule
  public final CompilationRule compilation = new CompilationRule();

  private Elements elements;

  @Before
  public void before() {
    elements = compilation.getElements();
  }

  @Test
  public void testAutoDelegate() throws Exception {
    TypeElement element = elements.getTypeElement("com.bdl.auto.AutoDelegateTest.DelegatingTestInterface");
    ClassMetadata metadata = ClassMetadata.fromElement(element);

    AutoDelegateWriter writer = new AutoDelegateWriter(
        s-> new PrintWriter(System.out),
        System.out::println);

    writer.write(metadata);
    TestInterface mock = mock(TestInterface.class);
    TestInterface impl = new Auto_AutoDelegateTest_DelegatingTestInterface_Delegate(mock);

    assertThat(impl.bar(2)).isEqualTo(3);
    impl.foo();
    verify(mock, never()).bar(anyInt());
    verify(mock).foo();
  }

  interface TestInterface {
    void foo();

    int bar(int baz);
  }

  @AutoDelegate
  abstract static class DelegatingTestInterface implements TestInterface {
    protected final TestInterface delegate;

    protected DelegatingTestInterface(TestInterface delegate) {
      this.delegate = delegate;
    }

    @Override
    public int bar(int baz) {
      return baz + 1;
    }
  }
}
