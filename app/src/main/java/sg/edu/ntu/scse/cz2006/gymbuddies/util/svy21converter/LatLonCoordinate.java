package sg.edu.ntu.scse.cz2006.gymbuddies.util.svy21converter;

/**
 * Converter for SVY21 to WGS84 coordinates
 * Adapted from https://github.com/cgcai/SVY21
 *
 * @since 2019-10-21
 * @author cgcai
 */
public class LatLonCoordinate {
    private double latitude;
    private double longitude;

    public LatLonCoordinate(double latitude, double longitude) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public SVY21Coordinate asSVY21() {
        return SVY21.computeSVY21(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LatLonCoordinate other = (LatLonCoordinate) obj;
        if (Double.doubleToLongBits(latitude) != Double
                .doubleToLongBits(other.latitude))
            return false;
        if (Double.doubleToLongBits(longitude) != Double
                .doubleToLongBits(other.longitude))
            return false;
        return true;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "LatLonCoordinate [latitude=" + latitude + ", longitude="
                + longitude + "]";
    }
}