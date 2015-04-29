import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;


public class Main {

    static ArrayList<String> sensor1 = new ArrayList<String>();
    static ArrayList<String> sensor2 = new ArrayList<String>();

    public static void main(String[] args) throws IOException {
        System.out.println("Modulo Principal");

        readFile();
        //BufferedReader Systemin =new BufferedReader(new InputStreamReader(System.in));
        InetAddress address = null;
        Socket socketv1 = null;
        Socket socketv2 = null;
        Socket socketv3 = null;

        PrintWriter outv1 = null;
        BufferedReader inv1 = null;
        PrintWriter outv2 = null;
        BufferedReader inv2 = null;
        PrintWriter outv3 = null;
        BufferedReader inv3 = null;

        byte[] recBuf = new byte[1024];

        int port1 = 6791;
        int port2 = 6792;
        int port3 = 6793;

        try {
            address = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }

        try {
            socketv1 = new Socket(address, port1);
            socketv1.setSoTimeout(10000);

            //socketv2 = new Socket(address, port2);
            //socketv2.setSoTimeout(10000);
//
            //socketv3 = new Socket(address, port3);
            //socketv3.setSoTimeout(10000);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        PrintWriter outToServerV1 = new PrintWriter(socketv1.getOutputStream(),true);
        BufferedReader inFromServerV1 = new BufferedReader(new InputStreamReader(socketv1.getInputStream()));

        //PrintWriter outToServerV2 = new PrintWriter(socketv2.getOutputStream(),true);
        //BufferedReader inFromServerV2 = new BufferedReader(new InputStreamReader(socketv2.getInputStream()));
//
        //PrintWriter outToServerV3 = new PrintWriter(socketv3.getOutputStream(),true);
        //BufferedReader inFromServerV3 = new BufferedReader(new InputStreamReader(socketv3.getInputStream()));


        int iterator = 0;

        while(true)
        {
            String message = generatePutData(iterator,0);

            String sendBufString = new String("Send String");
            outToServerV1.println(sendBufString);


            try {

                String received_string = inFromServerV1.readLine();

                System.out.println(received_string);

            } catch (IOException e) {
                e.printStackTrace();
                //System.out.println("Send Again!");
            }


        }
    }



    public static void readFile() {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("input.txt"));
            sensor1.add("0");
            sensor2.add("0");
            String line = br.readLine();
            while(line != null){
                sensor1.add(line.split(" ")[0]);
                sensor2.add(line.split(" ")[1]);
                line = br.readLine();
            }

            br.close();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static String generatePutData(int iterator, int dra) {
        String res = "";
        res+= "putdata ";
        res+= iterator + " ";
        res+= dra + " ";

        Timestamp ts = new Timestamp(new Date().getTime());
        res+= ts.getTime() + " ";

        res+= sensor1.get(3*iterator + 1) + " ";
        res+= sensor2.get(3*iterator + 1) + " ";

        res+= sensor1.get(3*iterator + 2) + " ";
        res+= sensor2.get(3*iterator + 2) + " ";

        res+= sensor1.get(3*iterator + 3) + " ";
        res+= sensor2.get(3*iterator + 3);

        String hashed = hashString(res);

        res += " ";
        res += hashed;

        System.out.println(res);



        return res;
    }

    private static String hashString(String message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hashedBytes = digest.digest(message.getBytes("UTF-8"));

            return convertByteArrayToHexString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }
}
