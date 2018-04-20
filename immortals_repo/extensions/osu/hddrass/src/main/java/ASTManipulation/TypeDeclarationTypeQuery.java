package ASTManipulation;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

/**
 * Created by root on 2/15/18.
 */
public class TypeDeclarationTypeQuery {

    public static TypeDeclarationType findType(TypeDeclaration t){
        TypeDeclarationType typeDeclarationType;
        if(t.isClassOrInterfaceDeclaration()){
            if(((ClassOrInterfaceDeclaration)t).isTypeDeclaration())
                return TypeDeclarationType.Type;
            if(((ClassOrInterfaceDeclaration)t).isInterface())
                return TypeDeclarationType.Interface;

        }

        if(t.isEnumDeclaration()){
            return TypeDeclarationType.Enumartion;

        }
        return  TypeDeclarationType.Other;
    }

    public static boolean  isNonRemovableType(TypeDeclaration t){
        return findType(t) == TypeDeclarationType.Enumartion || findType(t) == TypeDeclarationType.Interface;
    }





}
