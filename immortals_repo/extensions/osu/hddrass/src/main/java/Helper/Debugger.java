package Helper;

/**
 * Created by arpit on 9/21/16.
 */
public class Debugger {

    public static void log(Object o){
        if(Globals.IsDebug){
            System.out.println(o.toString());
        }

    }
}
