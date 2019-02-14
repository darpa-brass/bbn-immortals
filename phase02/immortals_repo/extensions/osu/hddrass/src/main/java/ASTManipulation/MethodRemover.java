package ASTManipulation;

import Helper.CompilationUnitHelper;
import Helper.Debugger;
import Helper.FileWriterUtil;
import Helper.Stuffs;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.*;
import org.json.simple.JSONObject;
import org.json.simple.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by root on 1/1/17.
 */
public class MethodRemover {

    public CompilationUnit _cu;
    public CompilationUnit _newcu;
    int HighestLabel;
    public List<String> doNotRemoveList;
    public List<String> deletedMethodList;


    public MethodRemover(CompilationUnit _cu) {
        this._cu = _cu;
        ModuleDeclaration md = _cu.getModule().isPresent() ? _cu.getModule().get(): (ModuleDeclaration)null;
        _newcu = new CompilationUnit(_cu.getPackageDeclaration().get(),_cu.getImports(),_cu.getTypes(),_cu.getModule().isPresent() ? _cu.getModule().get(): (ModuleDeclaration)null);

    }

    public MethodRemover(String testFileName){
        this._cu = CompilationUnitHelper.CreateCompilationUnit(testFileName);
        _newcu = new CompilationUnit(_cu.getPackageDeclaration().get(),_cu.getImports(),_cu.getTypes(),_cu.getModule().isPresent() ? _cu.getModule().get(): (ModuleDeclaration)null);
    }

    public MethodRemover(CompilationUnit cu, List<String> doNotRemoveList){
        _cu = cu;
        _newcu = new CompilationUnit(_cu.getPackageDeclaration().get(),_cu.getImports(),_cu.getTypes(),_cu.getModule().isPresent() ? _cu.getModule().get(): (ModuleDeclaration)null);
        this.doNotRemoveList = doNotRemoveList;
        deletedMethodList = new ArrayList<>();

    }



    public void removeMethods(int highestLevelAllowed){
        _cu.getTypes().stream().forEach(x -> {
            NodeList<BodyDeclaration<?>> list = new NodeList<BodyDeclaration<?>>();
            List<TypeDeclaration> listType = new ArrayList<TypeDeclaration>();
            List<FieldDeclaration> listFields = new ArrayList<FieldDeclaration>();
            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("TypeDeclaration"))
                    .forEach(y -> list.add((TypeDeclaration) y));
            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("FieldDeclaration"))
                    .forEach(y -> list.add((FieldDeclaration) y));

            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("MethodDeclaration"))
                    .forEach(y -> {
                        MethodDeclaration m = (MethodDeclaration) y;

                        if (m.getAnnotations() == null || m.getAnnotations().size() == 0) {
                            list.add(m);
                        } else {
                            if(m.getAnnotations().stream().anyMatch(w -> w.getName().asString().equals("MethodLabel"))){
                                m.getAnnotations().stream().forEach(z -> {
                                    if (z.getName().asString().equals("MethodLabel") && (((NormalAnnotationExpr) z).getPairs().size() == 1)) {
                                        Expression e = ((NormalAnnotationExpr) z).getPairs().get(0).getValue();
                                        int methodLabel = Integer.parseInt(((IntegerLiteralExpr) e).getValue());
                                        if (methodLabel <= highestLevelAllowed) {
                                            list.add(m);
                                        }

                                    } else {

                                    }

                                });
                            }
                            else{
                                list.add(m);
                            }


                        }

                    });



            /*x.getMembers().stream().forEach(y -> {
                BodyDeclaration b = (BodyDeclaration)y;
                if(b.getClass().getSimpleName().equals("MethodDeclaration")){
                    Debugger.log(b.getClass().getSimpleName());
                    MethodDeclaration m = (MethodDeclaration)b;

                    if(m.getAnnotations() == null || m.getAnnotations().size() == 0){
                        list.add(m);
                    }
                    else{
                        m.getAnnotations().stream().forEach(z -> {
                            if(z.getName().getName().equals("MethodLabel") && (((NormalAnnotationExpr)z).getPairs().size() == 1)) {
                                Expression e = ((NormalAnnotationExpr)z).getPairs().get(0).getValue();
                                int methodLabel =  Integer.parseInt(((IntegerLiteralExpr)e).getValue());
                                if(methodLabel <= highestLevelAllowed){
                                    list.add(m);
                                }

                            }
                            else{
                                list.add(m);
                            }

                        });
                    }


                }
            });*/
            _newcu.getTypes().get(0).setMembers(list);

        });

        Debugger.log(_newcu);


    }


    public void removeMethodsByPercentage(int percentageOfMethodToBeRemoved){
        _cu.getTypes().stream().forEach(x -> {
            NodeList<BodyDeclaration<?>> list = new NodeList<BodyDeclaration<?>>();
            List<TypeDeclaration> listType = new ArrayList<TypeDeclaration>();
            List<FieldDeclaration> listFields = new ArrayList<FieldDeclaration>();

            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("TypeDeclaration"))
                    .forEach(y -> list.add((TypeDeclaration) y));
            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("FieldDeclaration"))
                    .forEach(y -> list.add((FieldDeclaration) y));

            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("InitializerDeclaration"))
                    .forEach(y -> list.add((InitializerDeclaration) y));
            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("ConstructorDeclaration"))
                    .forEach(y -> list.add((ConstructorDeclaration) y));

            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("ClassOrInterfaceDeclaration"))
                    .forEach(y -> list.add((ClassOrInterfaceDeclaration) y));

            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("MethodDeclaration"))
                    .forEach(y -> {

                        MethodDeclaration m = (MethodDeclaration) y;

                        if((!doNotRemoveList.contains(m.getName())) && (isTestMethod(m))){
                            if(Math.random() >= (double)percentageOfMethodToBeRemoved/100) {
                                list.add(m);
                            }
                            else{
                                FileWriterUtil.appendLine("temp.txt",m.getName().asString());
                                deletedMethodList.add(m.getName().asString());
                            }
                        }
                        else{
                            list.add(m);
                        }

                    });



            _newcu.getTypes().get(0).setMembers(list);


        });

        Debugger.log(_newcu);


    }


    public CompilationUnit complement(List<BodyDeclaration<?>> compilationUnit, String comFile) {
        NodeList<BodyDeclaration<?>> removedList = new NodeList<>();
        NodeList<BodyDeclaration<?>> list = new NodeList<>();

        _cu.getTypes().stream().forEach(x -> {
            List<TypeDeclaration> listType = new ArrayList<TypeDeclaration>();
            List<FieldDeclaration> listFields = new ArrayList<FieldDeclaration>();

            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("TypeDeclaration"))
                    .forEach(y -> list.add((TypeDeclaration) y));
            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("FieldDeclaration"))
                    .forEach(y -> list.add((FieldDeclaration) y));

            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("InitializerDeclaration"))
                    .forEach(y -> list.add((InitializerDeclaration) y));
            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("ConstructorDeclaration"))
                    .forEach(y -> list.add((ConstructorDeclaration) y));

            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("ClassOrInterfaceDeclaration"))
                    .forEach(y -> list.add((ClassOrInterfaceDeclaration) y));

            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("MethodDeclaration"))
                    .forEach(y -> {

                        MethodDeclaration m = (MethodDeclaration) y;
                        if (isTestMethod(m)) {
                            boolean exist = false;
                            for (int i = 0; i < compilationUnit.size(); i++) {
                                BodyDeclaration bodyDeclaration = compilationUnit.get(i);
                                MethodDeclaration original = (MethodDeclaration) bodyDeclaration;
                                if (original.toString().equals(m.toString())) {
                                    exist = true;
                                    BlockStmt body = original.getBody().get();
                                }

                            }
                            if (!exist) {
                                list.add(m);
                                removedList.add(m);

                            }
                        } else {
                            list.add(m);

                        }



                    });



            _newcu.getTypes().get(0).setMembers(list);


        });


        System.out.println(_newcu.toString());
        return _newcu;
//        Debugger.log(removedList);

    }

    public     NodeList<BodyDeclaration<?>>  getAvaibleTestMethods() {
        NodeList<BodyDeclaration<?>> list = new NodeList<>();

        _cu.getTypes().stream().forEach(x -> {
            List<TypeDeclaration> listType = new ArrayList<TypeDeclaration>();
            List<FieldDeclaration> listFields = new ArrayList<FieldDeclaration>();



            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("MethodDeclaration"))
                    .forEach(y -> {

                        MethodDeclaration m = (MethodDeclaration) y;
                        if (isTestMethod(m)) {
                            String s = m.toString();
                            String s1 = m.getBody().toString();
                            list.add(m);

                        }

//                        if((!doNotRemoveList.contains(m.getName())) && (isTestMethod(m))){
//
//                            if(Math.random() >= (double)percentageOfMethodToBeRemoved/100) {
//                                list.add(m);
//                            }
//                            else{
//                                FileWriterUtil.appendLine("temp.txt",m.getName());
//                                deletedMethodList.add(m.getName());
//                            }
//                        }
//                        else{
//                            list.add(m);
//                        }

                    });



            _newcu.getTypes().get(0).setMembers(list);


        });

        Debugger.log(_newcu);
        return list;
    }

    public void removeMethodsByExternalLabeling(JSONArray requiredTests, JSONArray optionalTests){
        _cu.getTypes().stream().forEach(x -> {
            NodeList<BodyDeclaration<?>> list = new NodeList<BodyDeclaration<?>>();
            List<TypeDeclaration> listType = new ArrayList<TypeDeclaration>();
            List<FieldDeclaration> listFields = new ArrayList<FieldDeclaration>();

            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("TypeDeclaration"))
                    .forEach(y -> list.add((TypeDeclaration) y));
            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("FieldDeclaration"))
                    .forEach(y -> list.add((FieldDeclaration) y));

            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("InitializerDeclaration"))
                    .forEach(y -> list.add((InitializerDeclaration) y));
            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("ConstructorDeclaration"))
                    .forEach(y -> list.add((ConstructorDeclaration) y));

            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("ClassOrInterfaceDeclaration"))
                    .forEach(y -> list.add((ClassOrInterfaceDeclaration) y));

            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("MethodDeclaration"))
                    .forEach(y -> {

                        MethodDeclaration m = (MethodDeclaration) y;
                        if(isOptionalTestMethod(m,optionalTests)){
                            FileWriterUtil.appendLine("temp.txt",m.getName().asString());
                            deletedMethodList.add(m.getName().asString());
                        }
                        else{
                            list.add(m);
                        }

                    });



            _newcu.getTypes().get(0).setMembers(list);


        });

        Debugger.log(_newcu);


    }

    private boolean isTestMethod(MethodDeclaration m){
        List<AnnotationExpr> annotations = m.getAnnotations();
        if(annotations == null || annotations.size() == 0)
            return false;
        for (AnnotationExpr e: annotations) {
            if(e.getName().toString().equals("Test"))
                return  true;

        }
        return false;
    }

    private boolean isOptionalTestMethod(MethodDeclaration m, JSONArray optioalTests){
        boolean isOptioanlTest = false;
        if(!isTestMethod(m)){
            return false;
        }
        for (Object o :  optioalTests) {
            String methondName = Stuffs.DeriveMethodNameFromFullName(o.toString());
            if(m.getName().equals(methondName))
                return true;

        }
        return isOptioanlTest;
    }

    public int GetTestMethodCount(){
        List<MethodDeclaration> list = new ArrayList<>();
        _cu.getTypes().stream().forEach(x -> {


            x.getMembers().stream()
                    .filter(a -> ((BodyDeclaration)a).getClass().getSimpleName().equals("MethodDeclaration"))
                    .forEach(y -> {

                        MethodDeclaration m = (MethodDeclaration) y;

                        if((isTestMethod(m))){
                            list.add(m);
                        }


                    });






        });

        return list.size();


    }


}
