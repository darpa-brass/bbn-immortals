package mil.darpa.immortals.core.das.knowledgebuilders.generic;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.printer.YamlPrinter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import mil.darpa.immortals.config.ImmortalsConfig;
import org.junit.*;

import java.io.File;
import java.util.List;


public class StringVariableAssignmentResolverTest {

    private static CompilationUnit cu = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        File sourcePath = new File(FILE_TO_ANALYZE);

        CombinedTypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver(),
                new JavaParserTypeSolver(sourcePath));

        ParserConfiguration parserConfiguration =
                new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));

        SourceRoot sourceRoot = new SourceRoot(sourcePath.toPath());
        sourceRoot.setParserConfiguration(parserConfiguration);

        List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse("");

        cu = parseResults.stream()
                .filter(ParseResult::isSuccessful)
                .map(r -> r.getResult().get()).findFirst().get();

        System.out.println(new YamlPrinter(true).output(cu.findRootNode()));
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @SuppressWarnings("unused")
    private void analysisTarget1() {

        String myvar = "TEST1";

        System.out.println(myvar);

    }

    private String myvar = "TEST2";

    @SuppressWarnings("unused")
    private void analysisTarget2() {

        System.out.println(myvar);

        String myvar = "TESTXYZ";

    }

    @SuppressWarnings("unused")
    private void analysisTarget3() {

        String myvar = null;

        myvar = "TEST3";

        System.out.println(myvar);
    }

    @SuppressWarnings("unused")
    private void analysisTarget4() {

        String myvar = null;

        myvar = "TEST" + "4";

        System.out.println(myvar);

    }

    @SuppressWarnings("unused")
    private void analysisTarget5() {

        String myvar = null;

        myvar = "TESTabc";

        myvar = "TEST5";

        System.out.println(myvar);

    }

    @SuppressWarnings("unused")
    private void analysisTarget6() {

        String myvar = null;

        myvar = "TEST";

        myvar = myvar + "6";

        System.out.println(myvar);

    }


    @Test
    public void testVariableInitializer() {

        MethodDeclaration targetMethod = cu.findFirst(MethodDeclaration.class,
                t -> t.getName().toString().equals("analysisTarget1")).get();

        MethodCallExpr methodCall = targetMethod
                .findFirst(MethodCallExpr.class,
                        t -> t.getName().asString().equals("println")).get();

        Expression myvar = methodCall.getArgument(0);

        StringVariableValueVisitor sc = new StringVariableValueVisitor();
        SimpleStringAssignment assignment = sc.getStaticAssignment(myvar.asNameExpr().getNameAsString(), methodCall);

        assert (assignment.getValue().toString().equals("TEST1"));

    }

    @Test
    public void testFieldInitializer() {

        MethodDeclaration targetMethod = cu.findFirst(MethodDeclaration.class,
                t -> t.getName().toString().equals("analysisTarget2")).get();

        MethodCallExpr methodCall = targetMethod
                .findFirst(MethodCallExpr.class,
                        t -> t.getName().asString().equals("println")).get();

        Expression myvar = methodCall.getArgument(0);

        StringVariableValueVisitor sc = new StringVariableValueVisitor();
        SimpleStringAssignment assignment = sc.getStaticAssignment(myvar.asNameExpr().getNameAsString(), methodCall);

        assert (assignment.getValue().toString().equals("TEST2"));
    }

    @Test
    public void testAssignLiteral() {

        MethodDeclaration targetMethod = cu.findFirst(MethodDeclaration.class,
                t -> t.getName().toString().equals("analysisTarget3")).get();

        MethodCallExpr methodCall = targetMethod
                .findFirst(MethodCallExpr.class,
                        t -> t.getName().asString().equals("println")).get();

        Expression myvar = methodCall.getArgument(0);

        StringVariableValueVisitor sc = new StringVariableValueVisitor();
        SimpleStringAssignment assignment = sc.getStaticAssignment(myvar.asNameExpr().getNameAsString(), methodCall);

        assert (assignment.getValue().toString().equals("TEST3"));
    }

    @Test
    public void testAssignSimpleExpression() {

        MethodDeclaration targetMethod = cu.findFirst(MethodDeclaration.class,
                t -> t.getName().toString().equals("analysisTarget4")).get();

        MethodCallExpr methodCall = targetMethod
                .findFirst(MethodCallExpr.class,
                        t -> t.getName().asString().equals("println")).get();

        Expression myvar = methodCall.getArgument(0);

        StringVariableValueVisitor sc = new StringVariableValueVisitor();
        SimpleStringAssignment assignment = sc.getStaticAssignment(myvar.asNameExpr().getNameAsString(), methodCall);

        assert (assignment.getValue().toString().equals("TEST4"));
    }

    @Test
    public void testAssignSimpleExpression2() {

        MethodDeclaration targetMethod = cu.findFirst(MethodDeclaration.class,
                t -> t.getName().toString().equals("analysisTarget5")).get();

        MethodCallExpr methodCall = targetMethod
                .findFirst(MethodCallExpr.class,
                        t -> t.getName().asString().equals("println")).get();

        Expression myvar = methodCall.getArgument(0);

        StringVariableValueVisitor sc = new StringVariableValueVisitor();
        SimpleStringAssignment assignment = sc.getStaticAssignment(myvar.asNameExpr().getNameAsString(), methodCall);

        assert (assignment.getValue().toString().equals("TEST5"));
    }

    public void testAssignSimpleExpression3() {

        MethodDeclaration targetMethod = cu.findFirst(MethodDeclaration.class,
                t -> t.getName().toString().equals("analysisTarget6")).get();

        MethodCallExpr methodCall = targetMethod
                .findFirst(MethodCallExpr.class,
                        t -> t.getName().asString().equals("println")).get();

        Expression myvar = methodCall.getArgument(0);

        StringVariableValueVisitor sc = new StringVariableValueVisitor();
        SimpleStringAssignment assignment = sc.getStaticAssignment(myvar.asNameExpr().getNameAsString(), methodCall);

        assert (assignment.getValue().toString().equals("TEST6"));
    }


    private static final String FILE_TO_ANALYZE = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve(
            "das/das-service/src/test/java/mil/darpa/immortals/core/das/knowledgebuilders/generic").toString();
}
