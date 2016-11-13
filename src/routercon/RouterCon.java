package routercon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RouterCon 
{
    private LogFrame logFrame;
    private RouterListener listener;
    
    public RouterCon(LogFrame logFrame)
    {
        this.logFrame = logFrame;
    }

    void attach(String ip) 
    {
        this.listener = new RouterListener(ip);
        new Thread(this.listener).start();        
    }
        
}

class RouterListener implements Runnable
{
    RouterCon console;
    String ip;

    RouterListener(String ip) {
        this.ip = ip;
    }

    @Override
    public void run() 
    {
        try 
        {
            ServerSocket sock = new ServerSocket(1984);
            while (true)
            {
                Socket conn = sock.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                
                for (String line = in.readLine(); line != null; line = in.readLine())
                {
                    System.out.println(line);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
