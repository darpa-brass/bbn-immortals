package mil.darpa.immortals.analytics.profilers;

import android.graphics.Bitmap;
import mil.darpa.immortals.analytics.AnalysisDataFormat;
import mil.darpa.immortals.analytics.AnalysisSnapshot;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

/**
 * Created by awellman@bbn.com on 10/18/16.
 */
class AndroidConsumingPipeProfiler<InputType> extends GenericConsumingPipeProfiler<InputType> {

    public AndroidConsumingPipeProfiler(int pipelineIndex, String sessionIdentifier, ConsumingPipe<InputType> next) {
        super(pipelineIndex, sessionIdentifier, next);
    }

    @Override
    public AnalysisSnapshot[] analyze(InputType subject) {
        super.analyze(subject);

        if (subject instanceof Bitmap) {
            Bitmap bitmap = (Bitmap) subject;

            long pixelCount = bitmap.getHeight() * bitmap.getWidth();

            AnalysisSnapshot as = new AnalysisSnapshot(
                    AnalysisDataFormat.PIXEL_COUNT,
                    Long.toString(pixelCount));

            AnalysisSnapshot as2 = new AnalysisSnapshot(
                    AnalysisDataFormat.BYTE_COUNT,
                    Integer.toString(((Bitmap) subject).getByteCount()));

            AnalysisSnapshot[] array = {as, as2};

            return array;
        } else {
            return new AnalysisSnapshot[0];
        }
    }
}
