package mil.darpa.immortals.harness;

import mil.darpa.immortals.harness.visualization.datatypes.DataColumn;

/**
 * Created by awellman@bbn.com on 1/23/17.
 */
public class HarnessRouter {

//    private final HarnessSubmissionInterface harnessSubmissionInterface;

    private static HarnessRouter harnessRouter;

    public DataColumn createVisualizationDataColumn(String title) {
        return new DataColumn(title);
    }

//    private static class HarnessSubmissionInterface {
//        @POST("/visualization/createDisplayColumn")
//        Call<String> createDisplayColumn();
//        
//        @POST("/visualization/updateDisplayColumn")
//        Call<String> updateDisplayColumn();
//    }

    private HarnessRouter() {
//        String url = "http://127.0.0.1:1234";
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(url)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        
//        harnessSubmissionInterface = retrofit.create(HarnessSubmissionInterface.class);
    }

    public static HarnessRouter getInstance() {
        if (harnessRouter == null) {
            harnessRouter = new HarnessRouter();
        }
        return harnessRouter;
    }
}
