import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;

/**
 * Main application window.
 * Allows the user to enter a city, then displays a real-time compass
 * showing the sun's direction and elevation.  Refreshes every 10 seconds.
 */
public class MainFrame extends JFrame {

    // Default city: Seoul
    private double latitude  = 37.5666;
    private double longitude = 126.9782;
    private String cityName  = "서울";

    // UI components
    private final SunCompassPanel compassPanel;
    private final JTextField       cityField;
    private final JLabel           locationLabel;
    private final JLabel           sunInfoLabel;
    private final JLabel           timeLabel;
    private final JButton          searchButton;

    private javax.swing.Timer updateTimer;

    // UI colours
    private static final Color BG_DARK   = new Color(22, 22, 42);
    private static final Color BG_PANEL  = new Color(32, 32, 55);
    private static final Color BG_FIELD  = new Color(48, 48, 72);
    private static final Color FG_WHITE  = new Color(235, 235, 250);
    private static final Color FG_DIM    = new Color(170, 170, 200);
    private static final Color ACCENT    = new Color(70, 140, 210);

    // ─────────────────────────────────────────────────────────────────────────

    public MainFrame() {
        super("☀  실시간 태양 방향 나침반");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(BG_DARK);

        // ── Build components ─────────────────────────────────────────────────
        compassPanel   = new SunCompassPanel();
        cityField      = buildCityField();
        searchButton   = buildSearchButton();
        locationLabel  = infoLabel("", Font.PLAIN, 12);
        sunInfoLabel   = infoLabel("", Font.BOLD,  13);
        timeLabel      = infoLabel("", Font.PLAIN, 11);

        // ── Layout ───────────────────────────────────────────────────────────
        setLayout(new BorderLayout(0, 0));
        add(buildTopPanel(),    BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        // ── Wire up actions ──────────────────────────────────────────────────
        ActionListener searchAction = e -> onSearch();
        searchButton.addActionListener(searchAction);
        cityField.addActionListener(searchAction);   // Enter key triggers search

        // ── Initial data + timer ─────────────────────────────────────────────
        updateSunPosition();
        locationLabel.setText("위치: " + cityName + formatCoords(latitude, longitude));

        updateTimer = new javax.swing.Timer(10_000, e -> updateSunPosition());
        updateTimer.start();

        pack();
        setLocationRelativeTo(null);
    }

    // ── Panel builders ────────────────────────────────────────────────────────

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(60, 60, 90)));

        JLabel cityLabel = new JLabel("도시:");
        cityLabel.setForeground(FG_WHITE);
        cityLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        panel.add(cityLabel);
        panel.add(cityField);
        panel.add(searchButton);
        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        wrapper.setBackground(BG_DARK);
        wrapper.add(compassPanel);
        return wrapper;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 2, 4));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(60, 60, 90)),
                new EmptyBorder(10, 16, 10, 16)));
        panel.add(locationLabel);
        panel.add(sunInfoLabel);
        panel.add(timeLabel);
        return panel;
    }

    // ── Component factories ───────────────────────────────────────────────────

    private JTextField buildCityField() {
        JTextField field = new JTextField(cityName, 16);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBackground(BG_FIELD);
        field.setForeground(FG_WHITE);
        field.setCaretColor(FG_WHITE);
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(80, 80, 120), 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        return field;
    }

    private JButton buildSearchButton() {
        JButton btn = new JButton("검색");
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 18, 6, 18));
        return btn;
    }

    private JLabel infoLabel(String text, int style, int size) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(style == Font.BOLD ? FG_WHITE : FG_DIM);
        label.setFont(new Font("SansSerif", style, size));
        return label;
    }

    // ── Search logic ──────────────────────────────────────────────────────────

    private void onSearch() {
        String city = cityField.getText().trim();
        if (city.isEmpty()) return;

        setUiEnabled(false);
        sunInfoLabel.setText("검색 중…");
        sunInfoLabel.setForeground(FG_DIM);

        new Thread(() -> {
            try {
                double[] coords = GeocodingService.search(city);
                if (coords != null) {
                    latitude  = coords[0];
                    longitude = coords[1];
                    cityName  = city;
                    SwingUtilities.invokeLater(() -> {
                        locationLabel.setText("위치: " + cityName + formatCoords(latitude, longitude));
                        updateSunPosition();
                        setUiEnabled(true);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        sunInfoLabel.setText("도시를 찾을 수 없습니다: " + city);
                        sunInfoLabel.setForeground(new Color(255, 120, 120));
                        setUiEnabled(true);
                    });
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    sunInfoLabel.setText("검색 오류: " + ex.getMessage());
                    sunInfoLabel.setForeground(new Color(255, 120, 120));
                    setUiEnabled(true);
                });
            }
        }, "geocoding-thread").start();
    }

    private void setUiEnabled(boolean enabled) {
        cityField.setEnabled(enabled);
        searchButton.setEnabled(enabled);
    }

    // ── Sun position update ───────────────────────────────────────────────────

    private void updateSunPosition() {
        long nowMillis = System.currentTimeMillis();
        double[] result = SunCalculator.calculate(latitude, longitude, nowMillis);
        double az = result[0];
        double el = result[1];

        compassPanel.setData(az, el);

        // Rebuild info text
        String dir    = cardinalName(az);
        String elSign = el >= 0 ? "+" : "";
        String info   = String.format(
                "방위각: %.1f°  |  고도: %s%.1f°  |  방향: %s",
                az, elSign, el, dir);
        sunInfoLabel.setText(info);
        sunInfoLabel.setForeground(el >= 0 ? FG_WHITE : new Color(130, 130, 190));

        // Estimated local solar time offset (rough, from longitude)
        ZoneOffset offset  = ZoneOffset.ofTotalSeconds((int)(longitude / 15.0) * 3600);
        ZonedDateTime local = ZonedDateTime.now(offset);
        String localTime   = local.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        timeLabel.setText("현지시각(추정): " + localTime
                + "   |   마지막 업데이트: "
                + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String formatCoords(double lat, double lon) {
        String ns = lat >= 0 ? "N" : "S";
        String ew = lon >= 0 ? "E" : "W";
        return String.format("  (%.4f°%s, %.4f°%s)", Math.abs(lat), ns, Math.abs(lon), ew);
    }

    /**
     * Converts an azimuth in degrees to a Korean cardinal direction name (16 points).
     */
    private static String cardinalName(double azimuth) {
        String[] dirs = {
            "북", "북북동", "북동", "동북동",
            "동", "동남동", "남동", "남남동",
            "남", "남남서", "남서", "서남서",
            "서", "서북서", "북서", "북북서"
        };
        // Add half a sector (11.25°) so boundaries fall between labels
        int idx = (int)((azimuth + 11.25) / 22.5) % 16;
        return dirs[idx];
    }
}
