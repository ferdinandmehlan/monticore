/* (c)  https://github.com/MontiCore/monticore */

package de.monticore.codegen.emf;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.monticore.emf._ast.ASTENodePackage;
import de.monticore.emf.util.AST2ModelFiles;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;

public class ASTNodeTest {
  
  @BeforeClass
  public static void setup() {
    LogStub.init();
    Log.enableFailQuick(false);
  }
  
  /**
   *
   * @param args
   */
  @Test
  public void testSerializing() {
    
    try {
      AST2ModelFiles.get().serializeAST(ASTENodePackage.eINSTANCE);
    }
    catch (IOException e) {
      fail("Should not reach this, but: " + e);
    }
    
  }
  
}
