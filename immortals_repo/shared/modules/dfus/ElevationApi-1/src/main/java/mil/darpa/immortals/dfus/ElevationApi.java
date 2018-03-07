package mil.darpa.immortals.dfus;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.referencing.operation.TransformException;

import java.awt.image.Raster;
import java.io.IOException;

/**
 * Created by awellman@bbn.com on 12/4/17.
 */
public class ElevationApi {

    GridCoverage2D grid;
    private static Raster gridData;


    public void init() {
        try {
            GeoTiffReader gtr = new GeoTiffReader(ElevationApi.class.getResourceAsStream("/earth.tif"));
            grid = gtr.read(null);
            gridData = grid.getRenderedImage().getData();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public ElevationData getElevation(double x, double y) {
        try {
            GridGeometry2D gg = grid.getGridGeometry();
            DirectPosition2D dp = new DirectPosition2D(x, y);
            GridCoordinates2D gc = gg.worldToGrid(dp);

            double[] pixel = new double[1];
            double[] data = gridData.getPixel(gc.x, gc.y, pixel);
            return new ElevationData(x, y, data[0], 1000);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }
    }
}
