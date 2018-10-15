/*  this class allows interactive
    viewing of a tree built from
    Node instances
*/

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import java.util.*;
import java.io.*;

public class TreeViewer extends Basic
{
  // instance variables for the application:
  // vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
 
  private Node root;     // root of tree to be viewed

  private String keyInput;
  private double horizGap = 2, vertGap = .5;

  // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

  public TreeViewer( String title, int ulx, int uly, int pw, int ph,
                     Node tree )
  {
    super(title,ulx,uly,pw,ph);

    setBackgroundColor( new Color( 128, 128, 200 ) );
  
    // tree display
    cameras.add( new Camera( 25, 45, pw-55, ph-75,
                             0, 10, 0, 5,
                             new Color( 200, 200, 200 )  ) );

    root = tree;

    super.start();
  }

  public void step()
  {
    // tree display
    Camera cam = cameras.get(0);
    cam.activate();
    cam.setColor( Color.black );

    root.draw( cam, 5.0, 4.0, horizGap, vertGap );

  }// step

  public void keyTyped( KeyEvent e ) {
    char key = e.getKeyChar();
    
    if( key == 'w' ){
      horizGap *= 1.2;
    }
    else if( key == 'n' ){
      horizGap /= 1.2;
    }

  }// keyTyped

  public void keyPressed( KeyEvent e ) {
    int code = e.getKeyCode();

    if( code == KeyEvent.VK_LEFT ){
      cameras.get(0).shiftRegion( -0.25, 0 );
    }
    else if( code == KeyEvent.VK_RIGHT ){
      cameras.get(0).shiftRegion( 0.25, 0 );
    }
    else if( code == KeyEvent.VK_UP ){
      cameras.get(0).shiftRegion( 0, 0.25 );
    }
    else if( code == KeyEvent.VK_DOWN ){
      cameras.get(0).shiftRegion( 0, -0.25 );
    }
  }// keyPressed

}// TreeViewer
