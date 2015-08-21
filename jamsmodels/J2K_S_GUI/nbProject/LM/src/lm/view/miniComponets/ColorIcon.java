/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view.miniComponets;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
   import javax.swing.*;
    import java.awt.*;

    public class ColorIcon implements Icon
    {
        private static int HEIGHT = 14;
        private static int WIDTH = 14;

        private Color color;

       public ColorIcon(Color color)  
       {
           this.color = color;
       }

       public int getIconHeight()
       {
          return HEIGHT;
       }

       public int getIconWidth()
       {
           return WIDTH;
       }

       public void paintIcon(Component c, Graphics g, int x, int y)
       {
           g.setColor(color);
          g.fillRect(x, y, WIDTH - 1, HEIGHT - 1);

           g.setColor(Color.black);
          g.drawRect(x, y, WIDTH - 1, HEIGHT - 1);
      }
   }
