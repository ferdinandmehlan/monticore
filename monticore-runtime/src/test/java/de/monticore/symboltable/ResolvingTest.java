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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Paths;

import de.monticore.ModelingLanguage;
import de.monticore.io.paths.ModelPath;
import de.monticore.symboltable.mocks.languages.JTypeSymbolMock;
import de.monticore.symboltable.mocks.languages.entity.ActionSymbol;
import de.monticore.symboltable.mocks.languages.entity.EntityLanguage;
import de.monticore.symboltable.mocks.languages.entity.EntitySymbol;
import de.monticore.symboltable.mocks.languages.entity.PropertySymbol;
import de.monticore.symboltable.resolving.CommonResolvingFilter;
import de.monticore.symboltable.resolving.ResolvedSeveralEntriesException;
import de.monticore.symboltable.resolving.ResolvingFilter;
import de.monticore.symboltable.types.JTypeSymbol;
import de.monticore.symboltable.types.references.CommonJTypeReference;
import org.junit.Test;

/**
 * @author Pedram Mir Seyed Nazari
 *
 */
public class ResolvingTest {

  // TODO PN test some complex resolving scenarios.

  @Test
  public void testSameSymbolOccursOnlyOnce() {
    final EntitySymbol entity = new EntitySymbol("Entity");

    final MutableScope localScope = new CommonScope(false);
    localScope.addResolver(CommonResolvingFilter.create(EntitySymbol.class, EntitySymbol.KIND));

    localScope.define(entity);
    localScope.define(entity);

    assertEquals(2, localScope.getSymbols().size());

    try {
      // Although the same symbol is stored twice in the scope, it is
      // only returned once, hence, no ambiguity exists.
      localScope.resolve("Entity", EntitySymbol.KIND).get();
    }
    catch(ResolvedSeveralEntriesException e) {
      fail();
    }



  }

  @Test
  public void testSpecificResolversForScopes() {
    final EntitySymbol entity = new EntitySymbol("Entity");
    final PropertySymbol property = new PropertySymbol("prop", new CommonJTypeReference<>("int", JTypeSymbol.KIND, entity.getSpannedScope()));
    entity.addProperty(property);

    final ActionSymbol action = new ActionSymbol("action");
    entity.addAction(action);

    final MutableScope localScope = new CommonScope(false);
    ((MutableScope)action.getSpannedScope()).addSubScope(localScope);

    final ResolvingFilter<PropertySymbol> propertyResolvingFilter = CommonResolvingFilter.create(
        PropertySymbol.class, PropertySymbol.KIND);

    // Only localScope is initialized with a resolver for properties
    localScope.addResolver(propertyResolvingFilter);

    // If resolving starts from the local scope, prop is found, because local scope is
    // initialized with a property resolvers.
    assertTrue(localScope.resolve("prop", PropertySymbol.KIND).isPresent());
    assertSame(property, localScope.resolve("prop", PropertySymbol.KIND).get());

    // Although prop is defined in entity, it cannot be resolved starting from action or entity,
    // because these scopes are not initialized with a property resolvers.
    assertFalse(action.getSpannedScope().resolve("prop", PropertySymbol.KIND).isPresent());
    assertFalse(entity.getSpannedScope().resolve("prop", PropertySymbol.KIND).isPresent());


    ((MutableScope)entity.getSpannedScope()).addResolver(propertyResolvingFilter);
    assertFalse(action.getSpannedScope().resolve("prop", PropertySymbol.KIND).isPresent());
    assertTrue(entity.getSpannedScope().resolve("prop", PropertySymbol.KIND).isPresent());
  }

  @Test
  public void testResolveInnerSymbol() {
    final EntitySymbol entity = new EntitySymbol("Entity");

    final ActionSymbol action = new ActionSymbol("action");
    entity.addAction(action);

    final PropertySymbol property = new PropertySymbol("prop", new CommonJTypeReference<>("int", JTypeSymbol.KIND, entity.getSpannedScope()));
    action.addVariable(property);


    final ModelingLanguage modelingLanguage = new EntityLanguage();
    final ResolverConfiguration resolverConfiguration = new ResolverConfiguration();
    resolverConfiguration.addTopScopeResolvers(modelingLanguage.getResolvers());
    final ModelPath modelPath = new ModelPath(Paths.get(""));

    final GlobalScope globalScope = new GlobalScope(modelPath, modelingLanguage.getModelLoader(), resolverConfiguration);
    globalScope.define(entity);

    assertTrue(globalScope.resolve("Entity", EntitySymbol.KIND).isPresent());
    assertTrue(globalScope.resolve("Entity.action", ActionSymbol.KIND).isPresent());
    assertTrue(globalScope.resolve("Entity.action.prop", PropertySymbol.KIND).isPresent());

  }


}