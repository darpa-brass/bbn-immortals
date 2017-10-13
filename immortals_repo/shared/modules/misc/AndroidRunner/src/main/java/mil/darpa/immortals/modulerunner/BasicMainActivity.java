package mil.darpa.immortals.modulerunner;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import mil.darpa.immortals.core.synthesis.annotations.dfu.SynthesisAndroidContext;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class BasicMainActivity extends Activity {

    Thread moduleThread;

    @SynthesisAndroidContext
    public Context context;

    // Declaration
    // $B49219A8-493B-4639-8F94-73EEC496C523-declaration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = this;

        setContentView(R.layout.activity_basic_main);

        // Initialization
        // $B49219A8-493B-4639-8F94-73EEC496C523-init


        moduleThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Execution
                // $B49219A8-493B-4639-8F94-73EEC496C523-work
            }
        });
        moduleThread.start();
    }

    @Override
    protected void onDestroy() {
        // Cleanup
        // $B49219A8-493B-4639-8F94-73EEC496C523-cleanup

        super.onDestroy();
    }
}
