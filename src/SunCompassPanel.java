import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * Custom JPanel that renders a sky-view compass showing the sun's position.
 *
 * The display uses a "fish-eye / zenith projection":
 *   - Center = directly overhead (elevation 90°)
 *   - Edge   = horizon (elevation 0°)
 *   - Direction = azimuth (North at top, clockwise)
 */
public class SunCompassPanel extends JPanel {

    // Current sun data
    private double azimuth  = 180.0;  // degrees from North, clockwise
    private double elevation = 45.0;  // degrees above horizon

    // Pre-generated star positions (deterministic, so they don't flicker)
    private static final int STAR_COUNT = 80;
    private final int[] starX = new int[STAR_COUNT];
    private final int[] starY = new int[STAR_COUNT];
    private final int[] starSize = new int[STAR_COUNT];

    // Colors
    private static final Color COL_SKY_CENTER  = new Color(100, 170, 240);
    private static final Color COL_SKY_EDGE    = new Color(50,  110, 200);
    private static final Color COL_NIGHT_CENTER = new Color(10,  10,  50);
    private static final Color COL_NIGHT_EDGE   = new Color(5,   5,  25);
    private static final Color COL_SUN          = new Color(255, 230, 50);
    private static final Color COL_SUN_GLOW     = new Color(255, 200, 0, 80);
    private static final Color COL_RING         = new Color(220, 220, 240);
    private static final Color COL_LABEL        = new Color(230, 230, 250);
    private static final Color COL_NORTH        = new Color(255, 90,  90);
    private static final Color COL_ELEV_RING    = new Color(255, 255, 255, 50);
    private static final Color COL_STAR         = new Color(220, 220, 255);

    public SunCompassPanel() {
        setPreferredSize(new Dimension(420, 420));
        setBackground(new Color(20, 20, 40));
        generateStars();
    }

    /** Called from MainFrame to update the displayed sun position. */
    public void setData(double azimuth, double elevation) {
        this.azimuth   = azimuth;
        this.elevation = elevation;
        repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void generateStars() {
        Random rng = new Random(0x5EED);
        for (int i = 0; i < STAR_COUNT; i++) {
            // Stars radiate outward from the panel center (will be clamped to the compass circle)
            double angle  = rng.nextDouble() * 2 * Math.PI;
            double frac   = 0.05 + rng.nextDouble() * 0.90;  // 5%–95% of radius
            // Store as fraction; apply actual radius in paintComponent
            starX[i]    = (int)(frac * 1000 * Math.cos(angle));  // ×1000 to preserve precision
            starY[i]    = (int)(frac * 1000 * Math.sin(angle));
            starSize[i] = rng.nextInt(2) + 1;  // 1 or 2 px
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

            int w  = getWidth();
            int h  = getHeight();
            int cx = w / 2;
            int cy = h / 2;
            int R  = Math.min(w, h) / 2 - 45;  // compass disc radius

            boolean nighttime = elevation < 0;

            drawSkyBackground(g2, cx, cy, R, nighttime);
            if (nighttime) drawStars(g2, cx, cy, R);
            drawElevationRings(g2, cx, cy, R);
            drawCompassBezel(g2, cx, cy, R);
            drawCenterCross(g2, cx, cy);

            if (nighttime) {
                drawNightIndicator(g2, cx, cy, R);
            } else {
                drawSun(g2, cx, cy, R);
            }
        } finally {
            g2.dispose();
        }
    }

    // ── Sky background ────────────────────────────────────────────────────────

    private void drawSkyBackground(Graphics2D g2, int cx, int cy, int R, boolean night) {
        Color center = night ? COL_NIGHT_CENTER : COL_SKY_CENTER;
        Color edge   = night ? COL_NIGHT_EDGE   : COL_SKY_EDGE;
        RadialGradientPaint grad = new RadialGradientPaint(
                new Point2D.Float(cx, cy), R,
                new float[]{0f, 1f},
                new Color[]{center, edge});
        g2.setPaint(grad);
        g2.fillOval(cx - R, cy - R, 2 * R, 2 * R);
    }

    // ── Stars (night only) ───────────────────────────────────────────────────

    private void drawStars(Graphics2D g2, int cx, int cy, int R) {
        g2.setColor(COL_STAR);
        for (int i = 0; i < STAR_COUNT; i++) {
            int sx = cx + starX[i] * R / 1000;
            int sy = cy + starY[i] * R / 1000;
            int sz = starSize[i];
            g2.fillOval(sx - sz / 2, sy - sz / 2, sz, sz);
        }
    }

    // ── Elevation rings (0°, 30°, 60°) ───────────────────────────────────────

    private void drawElevationRings(Graphics2D g2, int cx, int cy, int R) {
        g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[]{4f, 4f}, 0f));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));

        int[] elevations = {30, 60};
        for (int elev : elevations) {
            int rr = (int)(R * (90.0 - elev) / 90.0);
            g2.setColor(COL_ELEV_RING);
            g2.drawOval(cx - rr, cy - rr, 2 * rr, 2 * rr);
            // Label at the right side
            g2.setColor(new Color(255, 255, 255, 120));
            g2.drawString(elev + "°", cx + rr + 3, cy + 4);
        }
        // Horizon ring label
        g2.setColor(new Color(255, 255, 255, 120));
        g2.drawString("0°", cx + R + 3, cy + 4);
    }

    // ── Compass bezel ─────────────────────────────────────────────────────────

    private void drawCompassBezel(Graphics2D g2, int cx, int cy, int R) {
        // Outer ring
        g2.setStroke(new BasicStroke(2.5f));
        g2.setColor(COL_RING);
        g2.drawOval(cx - R, cy - R, 2 * R, 2 * R);

        // Fine ticks every 10°
        g2.setStroke(new BasicStroke(0.6f));
        g2.setColor(new Color(200, 200, 220, 100));
        for (int deg = 0; deg < 360; deg += 10) {
            if (deg % 45 != 0) {
                double a  = Math.toRadians(deg - 90);
                int x1 = (int)(cx + (R - 5)  * Math.cos(a));
                int y1 = (int)(cy + (R - 5)  * Math.sin(a));
                int x2 = (int)(cx + R * Math.cos(a));
                int y2 = (int)(cy + R * Math.sin(a));
                g2.drawLine(x1, y1, x2, y2);
            }
        }

        // 8 compass directions
        String[] labels = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        for (int i = 0; i < 8; i++) {
            double a        = Math.toRadians(i * 45 - 90);
            boolean main    = (i % 2 == 0);
            int    tickLen  = main ? 14 : 8;

            // Tick
            g2.setStroke(new BasicStroke(main ? 2f : 1f));
            g2.setColor(COL_RING);
            int x1 = (int)(cx + (R - tickLen) * Math.cos(a));
            int y1 = (int)(cy + (R - tickLen) * Math.sin(a));
            int x2 = (int)(cx + R * Math.cos(a));
            int y2 = (int)(cy + R * Math.sin(a));
            g2.drawLine(x1, y1, x2, y2);

            // Label
            Font lf = main
                    ? new Font("SansSerif", Font.BOLD,  main && i == 0 ? 16 : 14)
                    : new Font("SansSerif", Font.PLAIN, 11);
            g2.setFont(lf);
            g2.setColor(i == 0 ? COL_NORTH : COL_LABEL);

            int lx = (int)(cx + (R + 20) * Math.cos(a));
            int ly = (int)(cy + (R + 20) * Math.sin(a));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(labels[i],
                    lx - fm.stringWidth(labels[i]) / 2,
                    ly + fm.getAscent() / 2 - 1);
        }
    }

    // ── Center cross ──────────────────────────────────────────────────────────

    private void drawCenterCross(Graphics2D g2, int cx, int cy) {
        g2.setColor(new Color(255, 255, 255, 90));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(cx - 6, cy, cx + 6, cy);
        g2.drawLine(cx, cy - 6, cx, cy + 6);
    }

    // ── Sun rendering ─────────────────────────────────────────────────────────

    private void drawSun(Graphics2D g2, int cx, int cy, int R) {
        // "Fish-eye" mapping: elevation 90° → center, 0° → edge
        double clampedElev = Math.max(0.0, Math.min(90.0, elevation));
        double sunFrac     = 1.0 - clampedElev / 90.0;       // 0 (zenith) → 1 (horizon)
        double sunRadius   = R * sunFrac;
        double sunAngle    = Math.toRadians(azimuth - 90.0); // rotate so 0° (N) = top

        int sunX = (int)(cx + sunRadius * Math.cos(sunAngle));
        int sunY = (int)(cy + sunRadius * Math.sin(sunAngle));

        // Direction line from center to sun
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(255, 220, 50, 120));
        g2.drawLine(cx, cy, sunX, sunY);

        // Glow halo
        int glowR = 24;
        RadialGradientPaint glow = new RadialGradientPaint(
                new Point2D.Float(sunX, sunY), glowR,
                new float[]{0f, 1f},
                new Color[]{COL_SUN_GLOW, new Color(255, 200, 0, 0)});
        g2.setPaint(glow);
        g2.fillOval(sunX - glowR, sunY - glowR, 2 * glowR, 2 * glowR);

        // Sun disc
        int discR = 11;
        g2.setPaint(COL_SUN);
        g2.fillOval(sunX - discR, sunY - discR, 2 * discR, 2 * discR);
        g2.setColor(new Color(255, 180, 0));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(sunX - discR, sunY - discR, 2 * discR, 2 * discR);

        // Sun rays (8 short lines)
        g2.setColor(new Color(255, 220, 80, 200));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 8; i++) {
            double rayAngle = Math.toRadians(i * 45.0);
            int rx1 = (int)(sunX + (discR + 4)  * Math.cos(rayAngle));
            int ry1 = (int)(sunY + (discR + 4)  * Math.sin(rayAngle));
            int rx2 = (int)(sunX + (discR + 10) * Math.cos(rayAngle));
            int ry2 = (int)(sunY + (discR + 10) * Math.sin(rayAngle));
            g2.drawLine(rx1, ry1, rx2, ry2);
        }

        // Arrowhead at the midpoint of the direction line, pointing toward the sun
        drawArrowhead(g2, cx, cy, sunX, sunY, new Color(255, 230, 80, 180));
    }

    // ── Night indicator ───────────────────────────────────────────────────────

    private void drawNightIndicator(Graphics2D g2, int cx, int cy, int R) {
        // Show where the sun will rise (its azimuth direction below the horizon)
        double sunAngle = Math.toRadians(azimuth - 90.0);
        int indicatorR  = R - 25;
        int ix = (int)(cx + indicatorR * Math.cos(sunAngle));
        int iy = (int)(cy + indicatorR * Math.sin(sunAngle));

        // Moon crescent icon
        g2.setColor(new Color(200, 210, 255, 140));
        g2.fillOval(ix - 13, iy - 13, 26, 26);
        g2.setColor(new Color(15, 20, 55));
        g2.fillOval(ix - 6,  iy - 13, 25, 26);

        // Night message
        g2.setColor(new Color(150, 160, 210));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String msg = "야간 — 해가 지평선 아래";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, cx - fm.stringWidth(msg) / 2, cy + R + 28);
    }

    // ── Helper: arrowhead along a line ───────────────────────────────────────

    private void drawArrowhead(Graphics2D g2, int x1, int y1, int x2, int y2, Color color) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 30) return;  // line too short, skip

        // Place arrowhead at 55% along the line
        double t  = 0.55;
        int ax = (int)(x1 + t * dx);
        int ay = (int)(y1 + t * dy);
        double angle = Math.atan2(dy, dx);
        int sz = 9;
        int[] arrowX = {
                ax,
                (int)(ax - sz * Math.cos(angle - Math.PI / 7)),
                (int)(ax - sz * Math.cos(angle + Math.PI / 7))
        };
        int[] arrowY = {
                ay,
                (int)(ay - sz * Math.sin(angle - Math.PI / 7)),
                (int)(ay - sz * Math.sin(angle + Math.PI / 7))
        };
        g2.setColor(color);
        g2.fillPolygon(arrowX, arrowY, 3);
    }
}
