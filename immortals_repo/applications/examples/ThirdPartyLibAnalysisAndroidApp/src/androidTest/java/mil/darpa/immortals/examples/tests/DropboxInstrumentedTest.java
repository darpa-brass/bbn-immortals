package mil.darpa.immortals.examples.tests;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.examples.android.UriHelpers;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.android.Auth;

import mil.darpa.immortals.androidhelper.LocalAndroidHelper;
import mil.darpa.immortals.ontology.VulnerabilityDropboxFunctionalAspect;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import mil.darpa.immortals.annotation.dsl.ontology.java.testing.annotation.ProvidedFunctionalityValidationAnnotation;
import mil.darpa.immortals.ontology.BaselineFunctionalAspect;

/**
 * Test case that exercises expected dropbox behavior
 *
 * Created by awellman on 5/2/18.
 */

@RunWith(AndroidJUnit4.class)
public class DropboxInstrumentedTest {

    // Logging identifier
    private final String TAG = "DropboxTest";

    // Two values to track whether or not the expected failure states have been reached
    private AtomicBoolean firstUploadFailed = new AtomicBoolean(false);
    private AtomicBoolean secondUploadFailed = new AtomicBoolean(false);

    /**
     * This test must:
     *  - Pass with the original library version
     *  - Fail with the newer library version
     *  - Pass with the partial upgraded library version
     */
    @Test
    @ProvidedFunctionalityValidationAnnotation(validatedAspects = BaselineFunctionalAspect.class)
    public void shouldPassWith003ButFailWith006() throws Exception{
	DbxAuthFinish authFinish = new DbxAuthFinish("CndrbEIFnZAAAAAAAAAAFN2UF0i7juwESW5HkXt8e8gJHFP3WKXjDCRhHq9fbeBW", Auth.getUid(), "test");        
	//Assert.fail(false);
    }


    public void submitSmallFileUnperturbed() throws Exception {
        String accessToken = "CndrbEIFnZAAAAAAAAAAFN2UF0i7juwESW5HkXt8e8gJHFP3WKXjDCRhHq9fbeBW";

        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("examples-v2-demo")
                .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                .build();
        DbxClientV2 client = new DbxClientV2(requestConfig, accessToken);

        Log.i(TAG, "Client built");

        String testString = "This is a test upload that should pass.";
        ByteArrayInputStream bais = new ByteArrayInputStream(testString.getBytes());

        UploadBuilder uploadBuilder;

        uploadBuilder = client.files().uploadBuilder("/smallValidUpload.txt")
                .withMode(WriteMode.OVERWRITE)
                .withClientModified(new Date(System.currentTimeMillis()));

        try {
            Log.i(TAG, "Sanity upload starting");
            uploadBuilder.uploadAndFinish(bais);
            String msg = "Sanity upload completed successfully!";
            Log.i(TAG, msg);

        } catch (NetworkIOException e) {
            String msg = "Sanity upload failed!";
            Log.e(TAG, msg);
            Assert.fail(msg);
        }
    }

    public void submitFile(Context context) throws Exception {
        String accessToken = "CndrbEIFnZAAAAAAAAAAFN2UF0i7juwESW5HkXt8e8gJHFP3WKXjDCRhHq9fbeBW";
        String fileUri = "file://" + Environment.getExternalStorageDirectory().getPath() + "/file.txt";

        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("examples-v2-demo")
                .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                .build();
        DbxClientV2 client = new DbxClientV2(requestConfig, accessToken);

        File localFile = UriHelpers.getFileForUri(context, Uri.parse(fileUri));

        Log.i(TAG, "Client built");

        FileInputStream in;
        UploadBuilder uploadBuilder;


// Upload file1
        in = new FileInputStream(localFile);
        uploadBuilder = client.files().uploadBuilder("/" + localFile.getName())
                .withMode(WriteMode.OVERWRITE)
                .withClientModified(new Date(localFile.lastModified()));

        try {
            Log.i(TAG, "Starting first file upload");
            uploadBuilder.uploadAndFinish(in);
            String msg = "First upload finished! Network connection not successfully cut!";
            Log.e(TAG, msg);
            LocalAndroidHelper.addError(msg);
        } catch (NetworkIOException e) {
            Log.i(TAG, "First upload of file failed successfully");
            firstUploadFailed.set(true);
        }

// Upload file2
        Log.i(TAG, "Client built and sending second file.");
        in = new FileInputStream(localFile);
        uploadBuilder = client.files().uploadBuilder("/" + localFile.getName())
                .withMode(WriteMode.OVERWRITE)
                .withClientModified(new Date(localFile.lastModified()));

        try {
            Log.i(TAG, "Starting first file upload");
            uploadBuilder.uploadAndFinish(in); // <- HANGS FOREVER
            String msg = "Second upload finished! Network connection not successfully cut!";
            Log.e(TAG, msg);
            LocalAndroidHelper.addError(msg);
        } catch (NetworkIOException e) {
            Log.i(TAG, "Second upload of file failed successfully");
            secondUploadFailed.set(true);
        }
    }

    @Test
    @ProvidedFunctionalityValidationAnnotation(validatedAspects = VulnerabilityDropboxFunctionalAspect.class)
    public void dropboxUnitTest() throws Exception {
        final Context context = InstrumentationRegistry.getTargetContext();
	
        // 1. Validate a small normal upload works.
        submitSmallFileUnperturbed();

        
        // Create a thread that will submit the files
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "Starting submission...");
                    submitFile(context);
                } catch (Exception e) {
                    String msg = "Unexpected exception raised: " + e.getMessage();
                    Log.e(TAG, msg);
                    LocalAndroidHelper.addError(msg);
                }
            }
        });
        t.setDaemon(true);

        
        // 2. Start upload #1 from the bug report
        t.start();

        
        // 3. Wait two seconds for upload #1 to connect and start
        Thread.sleep(2000);

        // 4. Disconnect network
        Log.i(TAG, "Disabling network.");
        LocalAndroidHelper.disableNetwork();


        Log.i(TAG, "Waiting for timeout, error, or finish");
        int timeout_duration = 180000;
        int current_duration = 0;

        // 5. Wait for a timeout of 180 seconds or for both downloads to fail
        while (current_duration < timeout_duration &&
                !(firstUploadFailed.get() && secondUploadFailed.get())) {
            // Sleep for a second
            Thread.sleep(1000);
            current_duration += 1000;
        }

        Log.i(TAG, "Timeout reached.");

        // 6. Check that both downloads failed
        Assert.assertTrue("The first upload has not failed!", firstUploadFailed.get());
        Assert.assertTrue("The second upload has not failed!", secondUploadFailed.get());
        
        try {
            LocalAndroidHelper.enableNetwork();
        } catch (Exception e) {
            // Tearing down, so not worth listening to.
        }

        // 7. Assert that no other errors were detected that could have impacted the results
        List<String> assertionFailures = LocalAndroidHelper.getAndClearFailures();
        
        for (String failure : assertionFailures) {
            Assert.fail(failure);
        }
    }
}
