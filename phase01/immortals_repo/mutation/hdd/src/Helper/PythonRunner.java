package Helper;
import java.io.*;
/**
 * Created by christia
 * copied and modiefied from some online resource.
 */
public class PythonRunner {
    private String _pythonPath;
    private String _scriptPath;
    private String[] _args;

    public PythonRunner(String pythonPath, String scriptPath, String[] args) {
        _pythonPath = pythonPath;
        _scriptPath = scriptPath;
        _args = args;
    }

    public String execute() {
        String output = Globals.EmptyString;

        String[] cmd = new String[_args.length + 2];
        cmd[0] = _pythonPath; // check version of installed python: python -V
        cmd[1] = _scriptPath;

        for(int i = 0;i<_args.length;i++){
            cmd[i+2] = _args[i];
        }

        Runtime rt = Runtime.getRuntime();
        Process pr = null;
        try {
            pr = rt.exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = "";
        try {
            while ((line = bfr.readLine()) != null) {
                output += line + "\n";

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Debugger.log(output);
        return output;
    }


    public static void main(String[] args){
        PythonRunner prRunner = new PythonRunner("python", "/home/ubuntu/learning/python/First.py",new String[]{"-w", "-r", "client-test-location"});
        String output = prRunner.execute();
        Debugger.log(output);
    }
}