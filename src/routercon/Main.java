package routercon;

public class Main {
    
    public static void main(String[] args) 
    {
       LogFrame logFrame = new LogFrame();
       logFrame.setVisible(true);
       RouterCon console = new RouterCon(logFrame);
       console.attach("192.168.1.16");
    }
    
}
