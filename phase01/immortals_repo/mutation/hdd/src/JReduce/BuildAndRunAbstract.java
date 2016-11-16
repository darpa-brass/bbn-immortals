package JReduce;

import Helper.Command;

/**
 * Created by arpit on 9/4/16.
 */
public class BuildAndRunAbstract {
    String buildCommand;
    JReduce.ITester tester;
    public BuildAndRunAbstract(){
        buildCommand = null;
        tester = null;
    }
    public BuildAndRunAbstract(String command, Tester tester){
        this.buildCommand = command;
        this.tester = tester;
    }
    private  boolean build(){
        return false;
    }
    public boolean run(){
        if(!build())
            return false;
        return false;
    }

}
