import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                javax.swing.UIManager.setLookAndFeel(
                        javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to default L&F; no impact on functionality
            }
            new MainFrame().setVisible(true);
        });
    }
}
