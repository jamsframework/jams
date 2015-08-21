/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gw.ui.layers;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.*;

import java.net.*;

/**
 * OneGeology BRGM World 1:25M Geologic Units
 *
 * @author Patrick Murris
 * @version $Id$
 */
public class OneGeologyWorldLayer extends BasicTiledImageLayer
{
    public OneGeologyWorldLayer()
    {
        super(makeLevels(new URLBuilder()));
        this.setUseTransparentTextures(true);
        this.setOpacity(.6);
    }

    private static LevelSet makeLevels(URLBuilder urlBuilder)
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 512);
        params.setValue(AVKey.TILE_HEIGHT, 512);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/OneGeologyWorld");
        params.setValue(AVKey.SERVICE, "http://mapsone.brgm.fr/1GmapserverFR/wms?map=/applications/mapserver/mapFiles/Lithology_FR.map");
        params.setValue(AVKey.DATASET_NAME, "WORLD_CGMW_25M_GeologicUnits");
        params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        params.setValue(AVKey.NUM_LEVELS, 6);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(36d), Angle.fromDegrees(36d)));
        params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
        params.setValue(AVKey.TILE_URL_BUILDER, urlBuilder);

        return new LevelSet(params);
    }

    private static class URLBuilder implements TileUrlBuilder
    {
        public URL getURL(Tile tile, String imageFormat) throws MalformedURLException
        {
            StringBuffer sb = new StringBuffer(tile.getLevel().getService());
            sb.append("&request=GetMap");
            sb.append("&layers=");
            sb.append(tile.getLevel().getDataset());
            sb.append("&srs=EPSG:4326");
            sb.append("&width=");
            sb.append(tile.getLevel().getTileWidth());
            sb.append("&height=");
            sb.append(tile.getLevel().getTileHeight());

            Sector s = tile.getSector();
            sb.append("&bbox=");
            sb.append(s.getMinLongitude().getDegrees());
            sb.append(",");
            sb.append(s.getMinLatitude().getDegrees());
            sb.append(",");
            sb.append(s.getMaxLongitude().getDegrees());
            sb.append(",");
            sb.append(s.getMaxLatitude().getDegrees());

            sb.append("&format=image/png");
            sb.append("&styles=default");
            sb.append("&version=1.1.1");
            sb.append("&transparent=true");
            sb.append("&service=wms");
            sb.append("&uuid=1ed5dab1-9d86-45f6-9eab-02d6a8b282a6");

            return new java.net.URL(sb.toString());
        }
    }

    @Override
    public String toString()
    {
        return "OneGeology World 1:25M (BRGM)";
    }
}
