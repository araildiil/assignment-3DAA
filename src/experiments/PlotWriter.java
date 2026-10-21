package experiments;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public final class PlotWriter {
    private static final Color[] COLORS = {
            new Color(0x1f77b4), new Color(0xd62728), new Color(0x2ca02c),
            new Color(0xff7f0e), new Color(0x9467bd), new Color(0x8c564b)
    };

    private PlotWriter() {}

    public static void lineChart(String file, String title, String xLabel, String yLabel,
                                 Map<String, List<double[]>> series) throws IOException {
        System.setProperty("java.awt.headless", "true");
        int w = 900, h = 600, left = 90, right = 230, top = 60, bottom = 80;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        double minX = Double.MAX_VALUE, maxX = 0, minY = Double.MAX_VALUE, maxY = 0;
        for (List<double[]> pts : series.values()) {
            for (double[] p : pts) {
                minX = Math.min(minX, p[0]);
                maxX = Math.max(maxX, p[0]);
                minY = Math.min(minY, Math.max(p[1], 0.01));
                maxY = Math.max(maxY, p[1]);
            }
        }
        double lx0 = Math.floor(Math.log10(minX)), lx1 = Math.ceil(Math.log10(maxX));
        double ly0 = Math.floor(Math.log10(minY)), ly1 = Math.ceil(Math.log10(maxY));
        if (lx1 <= lx0) lx1 = lx0 + 1;
        if (ly1 <= ly0) ly1 = ly0 + 1;
        int pw = w - left - right, ph = h - top - bottom;

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (int k = (int) lx0; k <= (int) lx1; k++) {
            int x = left + (int) ((k - lx0) / (lx1 - lx0) * pw);
            g.setColor(new Color(0xdddddd));
            g.drawLine(x, top, x, top + ph);
            g.setColor(Color.BLACK);
            g.drawString("10^" + k, x - 12, top + ph + 20);
        }
        for (int k = (int) ly0; k <= (int) ly1; k++) {
            int y = top + ph - (int) ((k - ly0) / (ly1 - ly0) * ph);
            g.setColor(new Color(0xdddddd));
            g.drawLine(left, y, left + pw, y);
            g.setColor(Color.BLACK);
            g.drawString("10^" + k, left - 45, y + 4);
        }
        g.setColor(Color.BLACK);
        g.drawRect(left, top, pw, ph);

        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString(title, left, 30);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(xLabel, left + pw / 2 - fm.stringWidth(xLabel) / 2, h - 25);
        Graphics2D gr = (Graphics2D) g.create();
        gr.rotate(-Math.PI / 2);
        gr.drawString(yLabel, -(top + ph / 2 + fm.stringWidth(yLabel) / 2), 25);
        gr.dispose();

        int ci = 0, legendY = top + 10;
        g.setStroke(new BasicStroke(2f));
        for (Map.Entry<String, List<double[]>> en : series.entrySet()) {
            Color c = COLORS[ci++ % COLORS.length];
            g.setColor(c);
            int px = -1, py = -1;
            for (double[] p : en.getValue()) {
                int x = left + (int) ((Math.log10(p[0]) - lx0) / (lx1 - lx0) * pw);
                int y = top + ph - (int) ((Math.log10(Math.max(p[1], 0.01)) - ly0) / (ly1 - ly0) * ph);
                if (px >= 0) g.drawLine(px, py, x, y);
                g.fillOval(x - 4, y - 4, 8, 8);
                px = x;
                py = y;
            }
            g.drawLine(left + pw + 15, legendY, left + pw + 40, legendY);
            g.setColor(Color.BLACK);
            g.drawString(en.getKey(), left + pw + 46, legendY + 4);
            legendY += 22;
        }
        g.dispose();
        ImageIO.write(img, "png", new File(file));
    }
}