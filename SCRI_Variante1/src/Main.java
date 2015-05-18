import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        System.out.println("Variante 1");
        System.out.println("A correr na porta 6791");
        int portNumber = 6791;
        Device device = new Device();

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (true) {

                String message = in.readLine();
                System.out.println("[V1-IN]"+ message);

                if(message.equals("end")){
                    System.out.println("[V1]Conexão terminou - Não há mais valores para avaliar");
                    System.out.println("[V1-out]ack");
                    out.println("ack");
                    return;
                }

                MessageHandler mh = new MessageHandler(message);
                double result = -1;

                device.addMinute(mh.minute1, 1, mh.getIteration());
                device.addMinute(mh.minute2, 2, mh.getIteration());
                device.addMinute(mh.minute3, 3, mh.getIteration());
                result = device.process();


                String response = mh.generateAnswer(result);

                out.println(response);

                System.out.println("[V1-OUT]" + response);

                Thread.sleep(0);
            }
        } catch(IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
