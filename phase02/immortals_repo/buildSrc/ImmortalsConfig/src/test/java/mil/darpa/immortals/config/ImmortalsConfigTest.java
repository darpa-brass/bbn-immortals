package mil.darpa.immortals.config;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

/**
 * Created by awellman@bbn.com on 10/31/17.
 */
public class ImmortalsConfigTest {

    @Test
    public void testImmortalsRootExists() {
        ImmortalsConfig c = ImmortalsConfig.getInstance();
        Assert.assertTrue("The determined immortals root '" + c.globals.getImmortalsRoot() + "' does not exist!",
                c.globals.getImmortalsRoot().toFile().exists());
    }

    @Test
    public void testImmortalsRepositoryExists() {
        ImmortalsConfig c = ImmortalsConfig.getInstance();
        Assert.assertTrue("The determined immortals repository '" + c.globals.getImmortalsRepo() + "' does not exist!",
                new File(c.globals.getImmortalsRepo()).exists());
    }

    @Test
    public void writeImmortalsConfigToDisk() {
        try {
            Files.write(Paths.get("../../immortals_config.json"),
                    ImmortalsConfig.getInstanceAsJsonString().getBytes(),
                    StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testDirectoryAutoCreation() {
        try {
            GlobalsConfig gc = ImmortalsConfig.getInstance().globals;
            Path p;
            String identifier;

            p = gc.getApplicationsDeploymentDirectory("TestDir0" + Long.toString(System.currentTimeMillis()));
            Assert.assertTrue(Files.exists(p));
            Files.delete(p);
            
            p = gc.getAdaptationWorkingDirectory("TestDir1" + Long.toString(System.currentTimeMillis()));
            Assert.assertTrue(Files.exists(p));
            Files.delete(p);
            p = gc.getAdaptationLogDirectory("TestDir2" + Long.toString(System.currentTimeMillis()));
            Assert.assertTrue(Files.exists(p));
            Files.delete(p);
            p = gc.getGlobalLogDirectory();
            Assert.assertTrue(Files.exists(p));
            
            try {
                Files.delete(p);
            } catch (DirectoryNotEmptyException e) {
                // If actual runs have populated this, you will not be able to delete it.
                // Best to just leave it alone...
            }
            p = gc.getGlobalWorkingDirectory();
            Assert.assertTrue(Files.exists(p));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
