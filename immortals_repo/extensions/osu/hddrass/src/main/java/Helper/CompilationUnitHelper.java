package Helper;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by root on 1/1/17.
 */
public  class CompilationUnitHelper {

    public static CompilationUnit CreateCompilationUnit(CompilationUnit cu, String fullClassPath) {


        FileInputStream in = null;
        String fullPath = fullClassPath;
        try {
            String current = new java.io.File(".").getCanonicalPath();
            Debugger.log("Current dir:" + current);
        } catch (Exception ex2) {

        }

        try {
            in = new FileInputStream(fullPath);
        } catch (FileNotFoundException fex) {
            Debugger.log("file not found");

        }

        try {
            // parse the file
            cu = JavaParser.parse(in);

        } catch (Exception pex) {
            pex.printStackTrace();

        }  finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return cu;
    }

    public static CompilationUnit CreateCompilationUnit(String fullClassPath){
        FileInputStream in = null;
        String fullPath = fullClassPath;
        CompilationUnit cu = null;
        try {
            String current = new java.io.File(".").getCanonicalPath();
            Debugger.log("Current dir:" + current);
        } catch (Exception ex2) {
            return null;

        }

        try {
            in = new FileInputStream(fullPath);
        } catch (FileNotFoundException fex) {
            Debugger.log("file not found");
            return null;

        }

        try {
            // parse the file
            cu = JavaParser.parse(in);

        } catch (Exception ex) {
            Debugger.log(ex);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return cu;
    }
}