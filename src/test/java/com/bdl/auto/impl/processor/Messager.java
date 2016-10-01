package com.bdl.auto.impl.processor;

import com.bdl.auto.impl.AutoImpl;
import com.bdl.auto.impl.ImplOption;

@AutoImpl(ImplOption.RETURN_DEFAULT_VALUE)
interface Messager extends javax.annotation.processing.Messager {
}
