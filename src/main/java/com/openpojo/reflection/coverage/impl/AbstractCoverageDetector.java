/*
 * Copyright (c) 2010-2017 Osman Shoukry
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openpojo.reflection.coverage.impl;

import com.openpojo.reflection.PojoClassFilter;
import com.openpojo.reflection.adapt.PojoClassAdapter;
import com.openpojo.reflection.coverage.CoverageDetector;
import com.openpojo.reflection.java.load.ClassUtil;

/**
 * @author oshoukry
 */
public abstract class AbstractCoverageDetector implements CoverageDetector {

  public abstract String getName();

  public abstract String getCoverageClassName();

  public abstract PojoClassFilter getPojoClassFilter();

  public abstract PojoClassAdapter getPojoClassAdapter();

  public boolean isLoaded() {
    return ClassUtil.isClassLoaded(getCoverageClassName());
  }
}
