package com.bdl.auto;

import com.bdl.auto.impl.AutoImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class AutoImplTest {

  @Test
  public void testAutoImpl() {
    TestInterface impl = new Auto_AutoImplTest_TestInterface_Impl();
    try {
      impl.foo();
      fail();
    } catch (UnsupportedOperationException ex) {
      // expected
    }

    try {
      impl.bar(15);
      fail();
    } catch (UnsupportedOperationException ex) {
      // expected
    }
  }

  @AutoImpl
  interface TestInterface {
    int foo();

    int bar(int baz);
  }
}
