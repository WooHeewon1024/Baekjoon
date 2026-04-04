/**
 * Calculates the sun's azimuth and elevation for a given location and time.
 * Based on the NOAA Solar Calculator algorithm (simplified SPA).
 *
 * Azimuth:  0° = North, 90° = East, 180° = South, 270° = West (clockwise from North)
 * Elevation: 0° = horizon, 90° = directly overhead, negative = below horizon
 */
public class SunCalculator {

    /**
     * @param latitude   Observer latitude in degrees (positive = North)
     * @param longitude  Observer longitude in degrees (positive = East)
     * @param utcMillis  Current time as UTC epoch milliseconds
     * @return double[] { azimuth (0–360°), elevation (-90–90°) }
     */
    public static double[] calculate(double latitude, double longitude, long utcMillis) {

        // ── 1. Julian Date ────────────────────────────────────────────────────
        double jd = utcMillis / 86400000.0 + 2440587.5;

        // ── 2. Julian Century from J2000.0 ───────────────────────────────────
        double T = (jd - 2451545.0) / 36525.0;

        // ── 3. Geometric Mean Longitude of the Sun (degrees) ─────────────────
        double L0 = (280.46646 + T * (36000.76983 + T * 0.0003032)) % 360.0;
        if (L0 < 0.0) L0 += 360.0;

        // ── 4. Geometric Mean Anomaly of the Sun (degrees) ───────────────────
        double M    = 357.52911 + T * (35999.05029 - 0.0001537 * T);
        double Mrad = Math.toRadians(M);

        // ── 5. Equation of Center ─────────────────────────────────────────────
        double C = (1.914602 - T * (0.004817 + 0.000014 * T)) * Math.sin(Mrad)
                 + (0.019993 - 0.000101 * T)                   * Math.sin(2.0 * Mrad)
                 + 0.000289                                     * Math.sin(3.0 * Mrad);

        // ── 6. Sun's True Longitude → Apparent Longitude ─────────────────────
        double sunTrueLon = L0 + C;
        double omega      = 125.04 - 1934.136 * T;
        double lambda     = sunTrueLon - 0.00569 - 0.00478 * Math.sin(Math.toRadians(omega));

        // ── 7. Obliquity of Ecliptic (degrees) ───────────────────────────────
        double eps0     = 23.0 + (26.0 + (21.448 - T * (46.8150 + T * (0.00059 - T * 0.001813))) / 60.0) / 60.0;
        double epsilon  = eps0 + 0.00256 * Math.cos(Math.toRadians(omega));

        // ── 8. Sun's Declination ─────────────────────────────────────────────
        double lambdaRad  = Math.toRadians(lambda);
        double epsilonRad = Math.toRadians(epsilon);
        double sinDec     = Math.sin(epsilonRad) * Math.sin(lambdaRad);
        double declination = Math.toDegrees(Math.asin(sinDec));

        // ── 9. Equation of Time (minutes) ────────────────────────────────────
        double L0rad = Math.toRadians(L0);
        double e     = 0.016708634 - T * (0.000042037 + 0.0000001267 * T);
        double yy    = Math.tan(epsilonRad / 2.0);
        yy = yy * yy;  // tan²(ε/2)
        double eotDeg = yy * Math.sin(2.0 * L0rad)
                      - 2.0 * e * Math.sin(Mrad)
                      + 4.0 * e * yy * Math.sin(Mrad) * Math.cos(2.0 * L0rad)
                      - 0.5  * yy * yy * Math.sin(4.0 * L0rad)
                      - 1.25 * e * e   * Math.sin(2.0 * Mrad);
        double eotMinutes = Math.toDegrees(eotDeg) * 4.0;

        // ── 10. True Solar Time (minutes) ────────────────────────────────────
        double utcMinutes  = (utcMillis % 86400000L) / 60000.0;
        if (utcMinutes < 0.0) utcMinutes += 1440.0;
        double trueSolarTime = ((utcMinutes + eotMinutes + 4.0 * longitude) % 1440.0 + 1440.0) % 1440.0;

        // ── 11. Hour Angle (degrees) ─────────────────────────────────────────
        // 0 at solar noon; negative = morning (east); positive = afternoon (west)
        double hourAngle = trueSolarTime / 4.0 - 180.0;

        // ── 12. Solar Zenith Angle ───────────────────────────────────────────
        double latRad = Math.toRadians(latitude);
        double decRad = Math.toRadians(declination);
        double haRad  = Math.toRadians(hourAngle);

        double cosZenith = Math.sin(latRad) * Math.sin(decRad)
                         + Math.cos(latRad) * Math.cos(decRad) * Math.cos(haRad);
        cosZenith = Math.max(-1.0, Math.min(1.0, cosZenith));
        double zenithDeg = Math.toDegrees(Math.acos(cosZenith));
        double elevation = 90.0 - zenithDeg;

        // ── 13. Atmospheric Refraction Correction ────────────────────────────
        if (elevation > -0.575) {
            double refArcSec;
            if (elevation > 85.0) {
                refArcSec = 0.0;
            } else if (elevation > 5.0) {
                double t = Math.tan(Math.toRadians(elevation));
                refArcSec = 58.1 / t - 0.07 / (t * t * t) + 0.000086 / Math.pow(t, 5);
            } else {
                refArcSec = 1735.0 + elevation
                          * (-518.2 + elevation * (103.4 + elevation * (-12.79 + elevation * 0.711)));
            }
            elevation += refArcSec / 3600.0;
        }

        // ── 14. Azimuth (from North, clockwise) ──────────────────────────────
        double azimuth;
        double sinZenith = Math.sin(Math.toRadians(Math.max(0.001, zenithDeg)));
        if (sinZenith < 1e-8) {
            // Sun at or near zenith
            azimuth = 0.0;
        } else {
            double cosAz = (Math.sin(latRad) * cosZenith - Math.sin(decRad))
                         / (Math.cos(latRad) * sinZenith);
            cosAz = Math.max(-1.0, Math.min(1.0, cosAz));
            double azRaw = Math.toDegrees(Math.acos(cosAz));
            // NOAA convention: sign flip based on hour angle
            if (hourAngle > 0.0) {
                azimuth = (azRaw + 180.0) % 360.0;
            } else {
                azimuth = (540.0 - azRaw) % 360.0;
            }
        }

        return new double[]{azimuth, elevation};
    }
}
