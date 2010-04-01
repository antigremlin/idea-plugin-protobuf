package protobuf.lang.psi.impl.definitions;

import com.intellij.lang.ASTNode;
import protobuf.lang.psi.ProtobufPsiElementVisitor;
import protobuf.lang.psi.api.PbFile;
import protobuf.lang.psi.api.references.PbImportRef;
import protobuf.lang.psi.api.definitions.PbImportDef;
import protobuf.lang.psi.impl.PbPsiElementImpl;

/**
 * author: Nikolay Matveev
 * Date: Mar 10, 2010
 */
public class PbImportDefImpl extends PbPsiElementImpl implements PbImportDef {

    public PbImportDefImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(ProtobufPsiElementVisitor visitor) {
        visitor.visitImportDefinition(this);
    }

    public String toString() {
        return "import definition";
    }

    @Override
    public PbFile getAliasedFile() {
        PbImportRef ref = this.findChildByClass(PbImportRef.class);
        return ref != null ? (PbFile) ref.resolve() : null;
    }
}