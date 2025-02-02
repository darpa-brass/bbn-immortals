package Helper;

/**
 * Created by root on 1/16/17.
 */
public class Stuffs {

    public static String DeriveClassNameFromFullPath(String filePath){
        String[] components = filePath.split("/");
        if(components.length > 1){
            return  (components[components.length - 1]).split("\\.")[0];

        }

        return null;
    }

    public static String DeriveFileNameFromFullPath(String filePath){
        String[] components = filePath.split("/");
        if(components.length > 1){
            return components[components.length - 1];
        }
        return Globals.EmptyString;
    }

    public static String DeriveFilenameWithoutExtensionFromFullPath(String filePath){
        String[] components = filePath.split("/");
        if(components.length > 1){
            String fileName =  components[components.length - 1];
            String[] fileparts = fileName.split("\\.");
            if(fileparts.length == 2){
                return fileparts[0];
            }

        }
        return Globals.EmptyString;
    }

    public static String DerivePathWithoutFileName(String filePath){
        String[] components = filePath.split("/");
        if(components.length > 1){
            String fileName = components[components.length - 1];
            return filePath.replace(fileName,"");

        }
        return Globals.EmptyString;
    }
    // for example, com.bbn.marti.Tests.testImageSave -> testImageSave
    public static String DeriveMethodNameFromFullName(String methodFullName){
        String[] components = methodFullName.split("\\.");
        if(components.length > 1){
            return components[components.length - 1];
        }
        return Globals.EmptyString;
    }

    public static String GetRelativePathFromFullPath(String root, String fullPath){
        String ans = fullPath.replace(root,"");
        System.out.println(ans);

        return ans;
    }

    public static void main(String[] args){
        System.out.println(DeriveMethodNameFromFullName("com.bbn.marti.Tests.testImageSave"));
        String ans = GetRelativePathFromFullPath("/root/arpit-marti/applications/server/Marti/","/root/arpit-marti/applications/server/Marti/src/com/bbn/marti/net/CotEventSender.java");
        //System.out.println(ans);

        String fileNameWithFullPath = "/home/ubuntu/results/coverage/URIBuilder/URIBuilder_1_10.coverage";
        System.out.println(Stuffs.DeriveFileNameFromFullPath(fileNameWithFullPath));
        System.out.println(Stuffs.DerivePathWithoutFileName(fileNameWithFullPath));


    }
}
