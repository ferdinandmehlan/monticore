/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */

package de.monticore.types;

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author Martin Schindler
 */
public class NestedTypeParametersTest {
  
  @Test
  public void testNestedTypeParameter1() {
    try {
      TypesTestHelper.getInstance().testTypeParameter("<T extends A.B<C[]>.D<E<F<G<H>>,I>> & J<K>>");
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }
  
  @Test
  public void testNestedTypeParameter2() {
    try {
      TypesTestHelper.getInstance().testTypeParameter("<T1 extends A<B<D, E>>, T2 extends " + "A.B<C[]>.D<E<F<G<H>>,I>> & J<K>>");
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }
  
}