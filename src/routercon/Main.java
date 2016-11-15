package routercon;

import javax.swing.JOptionPane;

public class Main {
    
    public static void main(String[] args) 
    {
       LogFrame logFrame = new LogFrame();
       logFrame.setVisible(true);
       RouterCon console = new RouterCon(logFrame);
       String routerIP = JOptionPane.showInputDialog("Ingrese la ip del router al que desea conectarse: ");
       if(routerIP == null)
           routerIP = "192.168.1.15";
       console.attach(routerIP);
    }
    
}
