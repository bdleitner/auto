package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;

/** Dummy class for use in testing. */
@AutoValue
public abstract class Thing {

  public abstract int anInt();
  public abstract String aString();

  public static Builder builder() {
    return new AutoValue_Thing.Builder();
  }

  @AutoValue.Builder
  public static abstract class Builder {
    public abstract Builder anInt(int anInt);
    public abstract Builder aString(String aString);

    public abstract Thing build();
  }
}
