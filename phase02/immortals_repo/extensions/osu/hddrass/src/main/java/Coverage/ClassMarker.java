package Coverage;

import Helper.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.*;

import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by root on 12/6/17.
 */
public class ClassMarker {

    public CompilationUnit cu;
    public String classNameWithFullPath;
    public String outputWriteFilePath;

    public ClassMarker(String classNameWithFullPath, String outputWriteFilePath){
        this.classNameWithFullPath = classNameWithFullPath;
        this.cu = CompilationUnitHelper.CreateCompilationUnit(classNameWithFullPath);
        this.outputWriteFilePath = outputWriteFilePath;

    }

    public void mark(){

        this.cu.getTypes().stream().forEach(x -> {
            TypeDeclaration t = (TypeDeclaration)x;

            for (Object b2: t.getMembers()
                    ) {
                BodyDeclaration<?> b = (BodyDeclaration<?>)b2;
                if (b.getClass().getSimpleName().toString().equals("MethodDeclaration")) {

                    MethodDeclaration m = (MethodDeclaration)b;


                    BlockStmt blk  = m.getBody().get();

                    BlockMarker blockMkaer = new BlockMarker(blk);
                    blockMkaer.isStaticMarker  = m.getModifiers().contains(Modifier.STATIC);
                    blockMkaer.outputFileName = this.outputWriteFilePath;
                    BlockStmt newBlock = blockMkaer.mark();
                    m.setBody(newBlock);

                }
            }


            t.getMembers().add(Instrumentation.intrumentationMethod());
            t.getMembers().add(Instrumentation.instrumentationMethodStatic());
        });

        ImportDeclaration importDeclaration = new ImportDeclaration("java.io",false,true );

        if(cu.getImports() != null){
            cu.getImports().add(importDeclaration);
        }



        Debugger.log(cu);


    }


    public static void main(String[] args){

        ClassMarker classMarker = new ClassMarker("/home/ubuntu/research/HDD/testdata/Coverage1.java","/home/ubuntu/temp/test.txt");
        classMarker.mark();;
        FileWriterUtil.write("/home/ubuntu/research/HDD/testdata/annotatedfiles/Coverage1.java",classMarker.cu.toString());


        //classMarker = new ClassMarker("/home/ubuntu/backup/ManagerOriginal.java","/home/ubuntu/temp/ManagerOriginal.txt");
        //classMarker.mark();
        // FileWriterUtil.write("/home/ubuntu/research/HDD/testdata/annotatedfiles/ManagerOriginal.java",classMarker.cu.toString());







    }
}


