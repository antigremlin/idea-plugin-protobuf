package protobuf.lang.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.UsefulTestCase;
import protobuf.util.PbTestUtil;
import protobuf.util.TestPath;

import java.io.IOException;

/**
 * author: Nikolay Matveev
 */

public class PbLexerTestCase extends UsefulTestCase {

    protected String getBasePath() {
        return PbTestUtil.getTestDataPath() + TestPath.LEXER_TEST_DIR;
    }

    protected void doTest(Lexer lexer) {
        doTest(lexer, getTestName(true).replace('$', '/') + ".test");
    }

    protected void doTest(Lexer lexer, String filePath) {
        try {
            Pair<String, String> pair = PbTestUtil.getSimpleTestMaterialsFromFile(getBasePath() + filePath);
            lexer.start(pair.getFirst());
            String result = "";
            while (true) {
                IElementType tokenType = lexer.getTokenType();
                if (tokenType == null) {
                    break;
                }
                String tokenText = getTokenText(lexer);
                String tokenTypeName = tokenType.toString();
                String line = tokenTypeName + "#" + tokenText + "\r\n";
                result += line;
                lexer.advance();
            }
            assertEquals(pair.getSecond()+"\r\n", result);
            System.out.println("abc");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTokenText(Lexer lexer) {
        String text = lexer.getBufferSequence().subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString();
        text = StringUtil.replace(text, "\r", "\\r");
        text = StringUtil.replace(text, "\n", "\\n");
        text = StringUtil.replace(text, "\t", "\\t");
        text = StringUtil.replace(text, "\f", "\\f");
        text = StringUtil.replace(text, " ", "\\_");
        return text;
    }

    public void testFlex$common(){
        doTest(new PbFlexLexer());
    }

    public void testFlex$comments(){
        doTest(new PbFlexLexer());
    }

    public void testFlex$keywords(){
        doTest(new PbFlexLexer());
    }
    
    public void testFlex$numbers(){
        doTest(new PbFlexLexer());
    }

    public void testFlex$string(){
        doTest(new PbFlexLexer());
    }

    public void testMerging(){
        doTest(new PbMergingLexer());
    }
}
