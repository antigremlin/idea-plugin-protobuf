package protobuf.lang.resolve;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;

import static protobuf.lang.psi.PbPsiEnums.*;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiPackage;
import protobuf.lang.psi.api.*;
import protobuf.lang.psi.api.blocks.PbBlock;
import protobuf.lang.psi.api.definitions.*;
import protobuf.lang.psi.api.references.PbRef;
import protobuf.lang.psi.utils.PsiUtil;

/**
 * author: Nikolay Matveev
 * Date: Mar 29, 2010
 */
public class ResolveUtil {

    private final static Logger LOG = Logger.getInstance(ResolveUtil.class.getName());

    public static PsiElement resolveInScope(final PsiElement scope, final PbRef ref) {
        ReferenceKind kind = ref.getKind();
        String refName = ref.getReferenceName();
        if (scope instanceof PsiPackage) {
            switch (kind) {
                case DIRECTORY:
                case PACKAGE: {

                }
                case MESSAGE_OR_GROUP_FIELD: {
                    assert false;
                }
                break;
                case MESSAGE_OR_GROUP:
                case MESSAGE_OR_ENUM_OR_GROUP:
                case EXTEND_FIELD: {
                    //get imported files by package name and invoke this function for this files
                    PbFile containingFile = (PbFile) ref.getContainingFile();
                    if (PsiUtil.isSamePackage(containingFile, (PsiPackage) scope)) {
                        PsiElement resolveResult = resolveInScope(containingFile, ref);
                        if (resolveResult != null) {
                            return resolveResult;
                        }
                    }
                    PbFile[] importedFiles = PsiUtil.getImportedFilesByPackageName(containingFile, ((PsiPackage) scope).getQualifiedName());
                    for (PbFile importedFile : importedFiles) {
                        PsiElement resolveResult = resolveInScope(importedFile, ref);
                        if (resolveResult != null) {
                            return resolveResult;
                        }
                    }
                }
                break;
                case MESSAGE_OR_PACKAGE_OR_GROUP: {
                    PbFile containingFile = (PbFile) ref.getContainingFile();
                    //resolve in subpackages scope
                    //alg: find subpackage and then find it in subpackages
                    PsiPackage subPackage = resolveInSubPackagesScope((PsiPackage) scope, refName);
                    if (subPackage != null) {
                        //f(subPackage, thisFile) -> boolean
                        //true if this subPackage in visible scope either false
                        if (PsiUtil.isVisibleSubPackage(subPackage, containingFile)) {
                            return subPackage;
                        }
                    }
                    //resolve in containing file
                    if (PsiUtil.isSamePackage(containingFile, (PsiPackage) scope)) {
                        PsiElement resolveResult = resolveInScope(containingFile, ref);
                        if (resolveResult != null) {
                            return resolveResult;
                        }
                    }
                    //resolve in imported files scope
                    PbFile[] importedFiles = PsiUtil.getImportedFilesByPackageName(containingFile, ((PsiPackage) scope).getQualifiedName());
                    for (PbFile importedFile : importedFiles) {
                        PsiElement resolveResult = resolveInScope(importedFile, ref);
                        if (resolveResult != null) {
                            return resolveResult;
                        }
                    }
                }
                break;
            }

        } else if (scope instanceof PbBlock || scope instanceof PbFile) {
            switch (kind) {
                case DIRECTORY:
                case PACKAGE: {
                    assert false;
                }
                case MESSAGE_OR_PACKAGE_OR_GROUP:
                case MESSAGE_OR_GROUP: {
                    PsiElement[] children = scope.getChildren();
                    for (PsiElement child : children) {
                        if (child instanceof PbMessageDef || (!(scope instanceof PbFile) && child instanceof PbGroupDef)) {
                            if (refName.equals(((PsiNamedElement) child).getName())) {
                                return child;
                            }
                        }
                    }
                    LOG.info(refName + " : MESSAGE_OR_PACKAGE_OR_GROUP not resolved in " + (scope instanceof PbFile ? "PbFile" : ("PbBlock: " + ((PbBlock) scope).getParent().getText())));
                }
                break;
                case MESSAGE_OR_ENUM_OR_GROUP: {
                    PsiElement[] children = scope.getChildren();
                    for (PsiElement child : children) {
                        if (child instanceof PbMessageDef || child instanceof PbEnumDef || child instanceof PbGroupDef) {
                            if (refName.equals(((PsiNamedElement) child).getName())) {
                                return child;
                            }
                        }
                    }
                }
                break;
                case EXTEND_FIELD: {
                    PsiElement[] children = scope.getChildren();
                    for (PsiElement child : children) {
                        if (child instanceof PbExtendDef) {
                            PbBlock extendBlock = ((PbExtendDef) child).getBlock();
                            if (extendBlock == null) {
                                return null;
                            }
                            PsiElement[] extendChildren = extendBlock.getChildren();
                            for (PsiElement extendChild : extendChildren) {
                                if (extendChild instanceof PbFieldDef && refName.equals(((PbFieldDef) extendChild).getName())) {
                                    return extendChild;
                                } else if (extendChild instanceof PbGroupDef && refName.equals(((PbGroupDef) extendChild).getLowerCaseName())) {
                                    return extendChild;
                                }
                            }
                        }
                    }
                }
                break;
                case MESSAGE_OR_GROUP_FIELD: {
                    if (scope instanceof PbFile) assert false;
                    PsiElement[] children = scope.getChildren();
                    for (PsiElement child : children) {
                        if (child instanceof PbFieldDef) {
                            if (refName.equals(((PsiNamedElement) child).getName())) {
                                return child;
                            }
                        } else if (child instanceof PbGroupDef && refName.equals(((PbGroupDef) child).getLowerCaseName())) {                            
                            return child;
                        }
                    }
                }
                break;
            }
        }
        return null;
    }

    public static PsiPackage resolveInSubPackagesScope(final PsiPackage parentPackage, String refName) {
        PsiPackage[] subPackages = parentPackage.getSubPackages();
        for (PsiPackage subPackage : subPackages) {
            if (subPackage.getName().equals(refName)) {
                return subPackage;
            }
        }
        return null;
    }

}
