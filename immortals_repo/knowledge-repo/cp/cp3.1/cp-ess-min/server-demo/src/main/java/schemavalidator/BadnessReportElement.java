package schemavalidator;

public class BadnessReportElement{
    
    private final Exception e;
    private final String exceptionClass;
    final String summary;
    
    public BadnessReportElement(Exception e, String exceptionClass, String summary) {
        super();
        this.e = e;
        this.exceptionClass = exceptionClass;
        this.summary = summary;
    }
    
    @Override
    public String toString(){
        return exceptionClass.toUpperCase() + ": " + summary;
    }
    
}