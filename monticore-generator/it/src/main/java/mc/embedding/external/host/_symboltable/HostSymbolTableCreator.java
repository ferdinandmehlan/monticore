/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
 * Copyright (c) 2017, MontiCore, All rights reserved.
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

package mc.embedding.external.host._symboltable;

import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.ResolvingConfiguration;
import mc.embedding.external.host._ast.ASTHost;
import mc.embedding.external.host._visitor.HostVisitor;

import java.util.Deque;

public class HostSymbolTableCreator extends HostSymbolTableCreatorTOP {

  private HostVisitor realThis = this;

  public HostSymbolTableCreator(ResolvingConfiguration resolverConfig,
      MutableScope enclosingScope) {
    super(resolverConfig, enclosingScope);
  }

  public HostSymbolTableCreator (ResolvingConfiguration resolvingConfiguration,
      Deque<MutableScope> scopeStack) {
    super(resolvingConfiguration, scopeStack);
  }

  @Override public void visit(ASTHost node) {
    final HostSymbol hostSymbol = new HostSymbol(node.getName());

    addToScopeAndLinkWithNode(hostSymbol, node);
  }

  @Override public void endVisit(ASTHost node) {
    removeCurrentScope();
  }

  @Override public void setRealThis(HostVisitor realThis) {
    this.realThis = realThis;
  }

  @Override public HostVisitor getRealThis() {
    return realThis;
  }
}
