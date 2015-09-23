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

package de.monticore.symboltable;

import de.monticore.symboltable.mocks.languages.JTypeSymbolMock;
import de.monticore.symboltable.mocks.languages.entity.ActionSymbol;
import de.monticore.symboltable.mocks.languages.entity.ActionSymbolKind;
import de.monticore.symboltable.mocks.languages.entity.EntitySymbol;
import de.monticore.symboltable.mocks.languages.entity.PropertyPredicate;
import de.monticore.symboltable.mocks.languages.entity.PropertySymbol;
import de.monticore.symboltable.mocks.languages.entity.PropertySymbolKind;
import de.monticore.symboltable.resolving.CommonResolvingFilter;
import de.monticore.symboltable.resolving.ResolvingFilter;
import de.monticore.symboltable.types.JTypeSymbol;
import de.monticore.symboltable.types.references.CommonJTypeReference;
import de.monticore.symboltable.types.references.JTypeReference;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 * @author Pedram Mir Seyed Nazari
 */
public class SymbolTableTest {

  private MutableScope topScope;
  private JTypeSymbolMock intSymbol;
  private JTypeSymbolMock stringSymbol;

  private JTypeReference<JTypeSymbol> intReference;
  private JTypeReference<JTypeSymbol> stringReference;

  @Before
  public void setUp() {
    topScope = new CommonScope(true);

    Set<ResolvingFilter<? extends Symbol>> resolvingFilters = new LinkedHashSet<>();
    resolvingFilters.add(CommonResolvingFilter.create(JTypeSymbolMock.class,
        JTypeSymbolMock.KIND));
    resolvingFilters.add(CommonResolvingFilter.create(PropertySymbol.class, PropertySymbol.KIND));
    
    topScope.setResolvingFilters(resolvingFilters);
    
    topScope.define(new JTypeSymbolMock("int"));
    topScope.define(new JTypeSymbolMock("String"));

    intSymbol = topScope.<JTypeSymbolMock>resolve("int", Symbol.KIND).get();
    stringSymbol = topScope.<JTypeSymbolMock>resolve("String", Symbol.KIND).get();

    intReference = new CommonJTypeReference<>("int", JTypeSymbol.KIND, topScope);
    stringReference = new CommonJTypeReference<>("String", JTypeSymbol.KIND, topScope);

    assertSame(intSymbol, intReference.getReferencedSymbol());
    assertSame(stringSymbol, stringReference.getReferencedSymbol());
  }
  
  @Test
  public void test() {
    /**
     * String var1;
     * int var2;
     * 
     *  void m() {
     *    int var1;
     *  
     *  }
     * 
     * 
     */
    
    
    ActionSymbol method = new ActionSymbol("m");
    topScope.define(method);
    
    Set<ResolvingFilter<? extends Symbol>> resolvingFilters = new LinkedHashSet<>();
    resolvingFilters.add(CommonResolvingFilter.create(PropertySymbol.class, PropertySymbol.KIND));
    ((MutableScope)method.getSpannedScope()).setResolvingFilters(resolvingFilters);
    
    
    assertSame(topScope, method.getEnclosingScope());
    assertSame(method.getEnclosingScope(), method.getSpannedScope().getEnclosingScope().get());
    
    PropertySymbol variable = new PropertySymbol("var1", intReference);
    method.addVariable(variable);
    
    assertSame(method.getSpannedScope(), variable.getEnclosingScope());
    
    assertSame(variable, method.getVariable("var1").get());
    assertFalse(method.getVariable("NotExisting").isPresent());
    
    PropertySymbol globalVariable1 = new PropertySymbol("var1", stringReference);
    PropertySymbol globalVariable2 = new PropertySymbol("var2", intReference);
    topScope.define(globalVariable1);
    topScope.define(globalVariable2);
    
    assertSame(globalVariable1, topScope.resolve("var1", Symbol.KIND).get());
    // returns only the local variable  
    assertSame(variable, method.getVariable("var1").get());
    // returns nothing, because var2 is not defined locally
    assertFalse(method.getVariable("var2").isPresent());
    // normal resolving finds the global variable
    assertSame(globalVariable2, method.getSpannedScope().resolve("var2", Symbol.KIND).get());
  }
  
  @Test
  public void testResolveWithFilter() {
    /**
     * String var1;
     * int var2;
     * 
     *  void m() {
     *    int var1;
     *  
     *  }
     */
    ActionSymbol method = new ActionSymbol("m");
    topScope.define(method);
    
    PropertySymbol variable = new PropertySymbol("var1", intReference);
    method.addVariable(variable);
    
    Set<ResolvingFilter<? extends Symbol>> resolvingFilters = new LinkedHashSet<>();
    resolvingFilters.add(CommonResolvingFilter.create(PropertySymbol.class, PropertySymbol.KIND));
    ((MutableScope)method.getSpannedScope()).setResolvingFilters(resolvingFilters);
    
    PropertySymbol globalVariable1 = new PropertySymbol("var1", stringReference);
    topScope.define(globalVariable1);
    

    assertSame(method, topScope.resolve(new SymbolNameAndKindPredicate("m", ActionSymbolKind.KIND)
    ).get());
    assertSame(globalVariable1, topScope.resolve(new SymbolNameAndKindPredicate("var1", PropertySymbolKind.KIND)).get());

    // no variable with name 'm' defined 
    assertFalse(topScope.resolve(new SymbolNameAndKindPredicate("m", PropertySymbolKind.KIND)).isPresent());
    
    PropertyPredicate varPredicate = new PropertyPredicate(new PropertySymbol("var1", stringReference));
    
    // Variable 'var1' in method is not returned, because is does not fulfill the predicate (wrong type)
    assertSame(globalVariable1, method.getSpannedScope().resolve(varPredicate).get());

    
    assertSame(variable, method.getSpannedScope().resolve("var1", Symbol.KIND).get());
    
  }
  
  @Test
  public void testResolveLocallyAllOfOneKind() {
    /**
     * 
     * class C {
     *   int i;
     *   int j;
     *   
     *   void m() {
     *     int f;
     *   }
     * 
     * }
     */

    Set<ResolvingFilter<? extends Symbol>> resolvingFilters = new LinkedHashSet<>();
    resolvingFilters.add(CommonResolvingFilter.create(EntitySymbol.class, EntitySymbol.KIND));
    resolvingFilters.add(CommonResolvingFilter.create(PropertySymbol.class, PropertySymbol.KIND));
    resolvingFilters.add(CommonResolvingFilter.create(ActionSymbol.class, ActionSymbol.KIND));
    
    EntitySymbol clazz = new EntitySymbol("C");
    ((MutableScope)clazz.getSpannedScope()).setResolvingFilters(resolvingFilters);
    
    PropertySymbol i = new PropertySymbol("i", intReference);
    clazz.addProperty(i);
    
    PropertySymbol j = new PropertySymbol("j", intReference);
    clazz.addProperty(j);
    
    ActionSymbol m = new ActionSymbol("m");
    clazz.addAction(m);
    
    PropertySymbol f = new PropertySymbol("f", intReference);
    m.addVariable(f);

    // all fields
    assertEquals(2, clazz.getProperties().size());
    assertTrue(clazz.getProperties().contains(i));
    assertTrue(clazz.getProperties().contains(j));
    
    // all methods
    assertEquals(1, clazz.getActions().size());
    assertTrue(clazz.getActions().contains(m));
  }

}