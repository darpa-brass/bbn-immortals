package schemavalidator;

import java.util.ArrayList;
import java.util.List;

final class DocumentNode{
    int startIndex;
    int endIndex;
    DocumentNode parent;
    final List<DocumentNode> children = new ArrayList<>();
    
    final List<String> content = new ArrayList<>();
    
    @Override
    public String toString(){
        return String.format("[%4d,%4d] -> %s", startIndex, endIndex, parent);
    }
    
    static List<DocumentNode> collect(DocumentNode d){
        List<DocumentNode> docs = new ArrayList<>();
        
        docs.add(d);
        for(DocumentNode c:d.children){
            docs.addAll(collect(c));
        }
        
        return docs;
    }
}