/* (c) https://github.com/MontiCore/monticore */

package de.monticore.codegen.symboltable;

import static de.monticore.codegen.GeneratorHelper.getSimpleTypeNameToGenerate;
import static de.se_rwth.commons.Names.getSimpleName;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import de.monticore.generating.GeneratorEngine;
import de.monticore.grammar.symboltable.MCGrammarSymbol;
import de.monticore.io.paths.IterablePath;
import de.se_rwth.commons.Names;

/**
 * @author Pedram Mir Seyed Nazari
 */
public class CommonModelNameCalculatorGenerator implements ModelNameCalculatorGenerator {

  @Override
  public void generate(GeneratorEngine genEngine, SymbolTableGeneratorHelper genHelper,
      IterablePath handCodedPath, MCGrammarSymbol grammarSymbol, Collection<String> grammarRuleNames) {
    final String className = getSimpleTypeNameToGenerate(getSimpleName(grammarSymbol.getFullName() + "ModelNameCalculator"),
        genHelper.getTargetPackage(), handCodedPath);

    final Path filePath = Paths.get(Names.getPathFromPackage(genHelper.getTargetPackage()), className + ".java");
    genEngine.generate("symboltable.ModelNameCalculator", filePath, grammarSymbol.getAstNode().get(), className, grammarRuleNames);
  }
}
