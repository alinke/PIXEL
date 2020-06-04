
package org.onebeartoe.web.enabled.pixel.controllers;

import javafx.beans.property.DoubleProperty;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Screen;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;


public class WindowsLCD {

GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
static String NOT_FOUND = "";
private static ImageIcon ii;
private boolean addedScroller = false;
private String basePath = "D:\\Arcade\\Pixelcade\\lcdmarquees";
private String pixelHome = System.getProperty("user.dir") + "\\";  
protected JFrame myFrame = new JFrame();
protected BufferedImage bi = null;
protected MarqueePanel marqueePanel;

{
    this.myFrame.setSize(1280, 390);
    this.myFrame.setType(Window.Type.UTILITY);
    this.myFrame.setBackground(Color.black);
    this.myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.myFrame.setUndecorated(true);
}
    
public GraphicsDevice[] connectedDevices() {

    for (GraphicsDevice display: screens
         ) {
        System.out.print(String.format("Resolution:%dx%d\n",display.getDisplayMode().getWidth(),display.getDisplayMode().getHeight()));
    }
     return screens;

}
    void scrollText(String message, Font font, Color color, int speed) {
        marqueePanel = new MarqueePanel(message,speed,color);
        myFrame.getContentPane().add(marqueePanel);
        addedScroller = true;
}


    void displayImage(String named, String system) throws IOException {
        if(addedScroller) {
            myFrame.getContentPane().remove(marqueePanel);
            addedScroller = false;
    }
        basePath = pixelHome + "lcdmarquees/";
        NOT_FOUND = basePath + "pixelcade.png";  //if not found, we'll show d:\arcade\pixelcade\lcdmarquees\pixelcade.png for example
        ;
    String marqueePath = NOT_FOUND;
    if(new File(String.format("%s%s.png",basePath,named)).exists()){
        marqueePath = String.format("%s%s.png",basePath,named);
    }else if(new File(String.format("%sconsole/default-%s.png",basePath,system)).exists()){
        marqueePath = String.format("%sconsole/default-%s.png",basePath,system);
    }

  System.out.println(String.format("MARQPATH is:%s Requested: %s %s",marqueePath,named,system));
        JLabel joe = new JLabel(new ImageIcon());
        bi = ImageIO.read(new File(marqueePath));
        bi = resize(bi, 390,1280);
        ii = new ImageIcon(bi);
        joe.setIcon(ii);
        myFrame.getContentPane().removeAll();
        myFrame.add(joe);
        
        showOnScreen(1, myFrame);
    }
    
    void getVideo(String  videoPath){
        final JFXPanel VFXPanel = new JFXPanel();
        VFXPanel.setBackground(Color.BLACK);

        File video_source = new File(videoPath);
        Media m = new Media(video_source.toURI().toString());
        MediaPlayer player = new MediaPlayer(m);
        MediaView viewer = new MediaView(player);
        player.setCycleCount(MediaPlayer.INDEFINITE);

        StackPane root = new StackPane();
        Scene scene = new Scene(root);

        // center video position
        javafx.geometry.Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        viewer.setX((screen.getWidth() - myFrame.getWidth()) / 2);
        viewer.setY((screen.getHeight() - myFrame.getHeight()) / 2);

        // resize video based on screen size
        DoubleProperty width = viewer.fitWidthProperty();
        DoubleProperty height = viewer.fitHeightProperty();
        //width.bind(Bindings.selectDouble(viewer.sceneProperty(), "width"));
       // height.bind(Bindings.selectDouble(viewer.sceneProperty(), "height"));
        viewer.setPreserveRatio(false);

        // add video to stackpane
        root.getChildren().add(viewer);

        VFXPanel.setScene(scene);
        player.play();
        myFrame.setLayout(new BorderLayout());
        myFrame.add(VFXPanel, BorderLayout.CENTER);
        myFrame.setVisible(true);
    }        

    public  void showOnScreen(int screen, JFrame frame) {//, JComponent jc)

        Container pane = frame.getContentPane();
        pane.setBackground(Color.black);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        if( screen > -1 && screen < gd.length ) {
            frame.setLocation(gd[screen].getDefaultConfiguration().getBounds().x, frame.getY());
        } else if( gd.length > 0 ) {
            frame.setLocation(gd[0].getDefaultConfiguration().getBounds().x, gd[0].getDefaultConfiguration().getBounds().y + frame.getY());
        } else {
            throw new RuntimeException( "No Screens Found" );
        }

        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    public BufferedImage resize(BufferedImage img, int height, int width) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
}

class MarqueePanel extends JPanel implements ActionListener {

    private static final int RATE = 12;
    private final Timer timer = new Timer(1000 / RATE, this);
    private final JLabel label = new JLabel();
    private final String s;
    private final int n;
    Font font = new Font("Serif", Font.ITALIC, 144);
    private int index;

    public MarqueePanel(String s, int n, Color color) {
        if (s == null || n < 1) {
            throw new IllegalArgumentException("Null string or n < 1");
        }
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(' ');
        }
        this.s = sb + s + sb;
        this.n = n;
        label.setFont(font);
        //label.setFont(new Font("Serif", Font.ITALIC, 144));
        label.setText(sb.toString());
        label.setForeground(color);
        label.setBackground(Color.BLACK);
        label.setVisible(true);
        this.add(label);
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        index++;
        if (index > s.length() - n) {
            index = 0;
        }
        label.setText(s.substring(index, index + n));
    }
}


