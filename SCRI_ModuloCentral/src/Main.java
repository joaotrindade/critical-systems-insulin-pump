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
    private final static double NO_RETURN_FROM_VARIANT = -1.0;
    private final static double NULL_RESULT_FROM_VOTER = -1.0;
    private final static double INVALID_HASH = -2.0;

    public static void main(String[] args) {
        System.out.println("Modulo Principal");
        String outputFilename;
        String inputFilename;

        if(args.length == 2){
            inputFilename = args[0];
            outputFilename = args[1];
        }
        else if(args.length == 1){
            inputFilename = args[0];
            outputFilename = "output_of_"+ inputFilename + "_" + new Date().getTime() + ".txt";
        }
        else{
            inputFilename = "input.txt";
            outputFilename = "output_of_"+ inputFilename + "_" + new Date().getTime() + ".txt";
        }

        readFile(inputFilename);
        //BufferedReader Systemin =new BufferedReader(new InputStreamReader(System.in));
        InetAddress address = null;
        Socket socketv1 = null;
        Socket socketv2 = null;
        Socket socketv3 = null;

        PrintWriter outToServerV1;
        BufferedReader inFromServerV1;
        PrintWriter outToServerV2;
        BufferedReader inFromServerV2;
        /*
        PrintWriter outToServerV3;
        BufferedReader inFromServerV3; */

        byte[] recBuf = new byte[1024];

        int port1 = 6792;
        int port2 = 6791;
        int port3 = 6793;

        try {
            address = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e1) {
            System.out.println("Erro: Endereco de rede desconhecido");
            e1.printStackTrace();
            return;
        }

        Boolean connected = false;
        int tentativa = 0;

        while(!connected){
            tentativa++;
            try {
                socketv1 = new Socket(address, port1);
                socketv1.setSoTimeout(10000);

                socketv2 = new Socket(address, port2);
                socketv2.setSoTimeout(10000);
//
                //socketv3 = new Socket(address, port3);
                //socketv3.setSoTimeout(10000);

                Thread.sleep(1000);

            } catch (SocketException e) {
                //e.printStackTrace();
                System.out.println("Socket nao encontrado, a tentar conectar novamente");
                continue;
            } catch (IOException | InterruptedException e) {
                System.out.println("Erro: Erro de ligacao ou interrupcao da thread");
                e.printStackTrace();
                return;
            }
            finally {
                if (socketv1 != null){
                    connected = true;
                }
            }

        }

        try {
            outToServerV1= new PrintWriter(socketv1.getOutputStream(),true);
            inFromServerV1 = new BufferedReader(new InputStreamReader(socketv1.getInputStream()));

            outToServerV2 = new PrintWriter(socketv2.getOutputStream(),true);
            inFromServerV2 = new BufferedReader(new InputStreamReader(socketv2.getInputStream()));
            /*
            outToServerV3 = new PrintWriter(socketv3.getOutputStream(),true);
            inFromServerV3 = new BufferedReader(new InputStreamReader(socketv3.getInputStream()));
            */

        } catch (IOException e) {
            System.out.println("Erro: Ligacao ao socket.");
            e.printStackTrace();
            return;
        }

        boolean VAR1_CONNECTED = true;
        boolean VAR2_CONNECTED = true;
        boolean VAR3_CONNECTED = true;

        int iterator = 0;
        double currentInsulin = 0.0;

        while(true)
        {

            // Tem que haver valores para completar a iteração (6 por cada iteração)
            if(3* iterator + 3 < sensor1.size()){
                String message = generatePutData(iterator, currentInsulin);
                System.out.println("[Main]["+iterator+"]A enviar info de iteracao " + iterator);

                String sendBufString = message;

                outToServerV1.println(sendBufString);
                outToServerV2.println(sendBufString);
                //outToServerV2.println(sendBufString);
            }
            else{
                System.out.println("[Main]["+iterator+"]Execucao Terminou - Nao ha mais valores para avaliar");

                try{
                    if(VAR1_CONNECTED) {
                        outToServerV1.println("end");
                        String receivedV1 = inFromServerV1.readLine();

                        if (receivedV1.equals("ack")) {
                            return;
                        }
                    }
                }catch (IOException e){
                    System.out.println("Erro: Empty Socket");
                }

                try{
                    if(VAR2_CONNECTED) {
                        outToServerV2.println("end");
                        String receivedV2 = inFromServerV2.readLine();

                        if (receivedV2.equals("ack")) {
                            return;
                        }
                    }
                }catch (IOException e){
                    System.out.println("Erro: Empty Socket");
                }

                /*
                try{
                    if(VAR3_CONNECTED) {
                        outToServerV3.println("end");
                        String receivedV3 = inFromServerV1.readLine();

                        if (receivedV3.equals("ack")) {
                            return;
                        }
                    }
                }catch (IOException e){
                    System.out.println("Erro: Empty Socket");
                }
                */

            }

            String receivedV1 = "";
            String receivedV2 = "";
            String receivedV3 = "";

            ArrayList<Double> results = new ArrayList<Double>();

            // Tentar ler a cada Variante
            try {
                receivedV1 = inFromServerV1.readLine();
                double resultV1 = verifyResponse(receivedV1,1);
                if(resultV1 != INVALID_HASH && resultV1 != NO_RETURN_FROM_VARIANT){ results.add(resultV1);}
            }
            catch(IOException e) {
                System.out.println("[Main] Variante 1 Desconectada");
                VAR1_CONNECTED = false;
            }

            try {
                receivedV2 = inFromServerV2.readLine();
                double resultV2 = verifyResponse(receivedV2,2);
                if(resultV2 != INVALID_HASH && resultV2 != NO_RETURN_FROM_VARIANT){ results.add(resultV2);}
            }
            catch(IOException e) {
                System.out.println("[Main] Variante 2 Desconectada");
                VAR2_CONNECTED = false;
            }

            /*
            try {
                receivedV3 = inFromServerV3.readLine();
                double resultV3 = verifyResponse(receivedV3,3);
                //if(resultV3 != INVALID_HASH && resultV3 != NO_RETURN_FROM_VARIANT){ results.add(resultV3);}
            }
            catch(IOException e) {
                System.out.println("[Main] Variante 3 Desconectada");
                VAR3_CONNECTED = false;
            }
            /*/

            //System.out.println("[Main]["+iterator+"]Recebi de V1: " + receivedV1);
            //System.out.println("[Main]["+iterator+"]Recebi de V2: " + receivedV2);
            //System.out.println("[Main]["+iterator+"]Recebi de V3: " + receivedV3);

            try{
                // VOTADOR
                System.out.print("[Main]["+iterator+"]Vai ser realizada a votacao com valores ");
                for(int i = 0; i < results.size(); i++){
                    System.out.print(results.get(i) + " ");
                }
                System.out.println();
                Voter v = new Voter(results);
                if(v.getConsensus() && v.getVotingResult()!= NULL_RESULT_FROM_VOTER){
                    System.out.println("[Main]["+iterator+"]Vai ser administrado o valor " + Math.round(v.getVotingResult()));
                    writeToFile(outputFilename, String.valueOf(v.getVotingResult()));

                    // Atualiza valor da insulina

                    currentInsulin = currentInsulin + 0.9 * v.getVotingResult();
                }
                else{
                    System.out.print("[Main]["+iterator+"]Nao houve consenso. Valores recebidos: ");
                    for(int i = 0; i < results.size(); i++){
                        System.out.print(results.get(i) + " ");
                    }
                    System.out.println("");
                    writeToFile(outputFilename, "FAIL");
                }

                int delay = 2;
                //System.out.println("[Main]["+iterator+"]Em espera "+ delay + " segundos ate a proxima iteracao");
                Thread.sleep(delay * 1000);

            } catch (InterruptedException e) {
                System.out.println("Erro: Erro de ligacao ou interrupcao");
                e.printStackTrace();
                return;
            }
            System.out.println("[Main]----- Fim da Iteracao " + iterator + "-----");
            iterator++;
        }
    }


    public static double verifyResponse(String response, int v){
        String[] parts = response.split(" ");

        if(parts.length != 5){
            System.out.println("\t[MessageHandler]["+v+"]Nao tem mensagem completa");
            return -1.0;
        }
        String action   = parts[0];
        String iteration= parts[1];
        String timestamp= parts[2];
        String result   = parts[3];
        String hash     = parts[4];

        String receivedStr = action + " " + iteration + " " + timestamp + " " + result;
        String comparable = hashString(receivedStr);

        if(comparable.equals(hash)){
            System.out.println("\t[MessageHandler]["+v+"]Hash da resposta verificada com resultado " + result);
            return Double.parseDouble(result);
        }
        else{
            System.out.println("\t[MessageHandler]["+v+"]Hash da resposta invalida");
            return INVALID_HASH;
        }
    }

    public static void writeToFile(String filename, String value){

        FileWriter fw = null;
        try {
            fw = new FileWriter(filename,true);
            fw.write(value+"\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void readFile(String filename) {

        BufferedReader br = null;
        String line = null;
        try {
            br = new BufferedReader(new FileReader(filename));
            sensor1.add("0");
            sensor2.add("0");
            line = br.readLine();
            while(line != null){
                sensor1.add(line.split(" ")[0]);
                sensor2.add(line.split(" ")[1]);
                line = br.readLine();
            }

            br.close();
            return;
        } catch (IOException e) {
            System.out.println("Erro: Ficheiro nao existe ou nao esta acessivel.");
            e.printStackTrace();
            return;
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println(line);
            System.out.println("Erro: Linha invalida.");
            e.printStackTrace();
            return;
        }

    }

    public static String generatePutData(int iterator, double currentInsulin) {
        String res = "";
        res+= "putdata ";
        res+= iterator + " ";

        Timestamp ts = new Timestamp(new Date().getTime());
        res+= ts.getTime() + " ";

        res+= sensor1.get(3*iterator + 1) + " ";
        res+= sensor2.get(3*iterator + 1) + " ";

        res+= sensor1.get(3*iterator + 2) + " ";
        res+= sensor2.get(3*iterator + 2) + " ";

        res+= sensor1.get(3*iterator + 3) + " ";
        res+= sensor2.get(3*iterator + 3) + " ";

        res+= currentInsulin;

        String hashed = hashString(res);

        res += " ";
        res += hashed;

        return res;
    }

    private static String hashString(String message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hashedBytes = digest.digest(message.getBytes("UTF-8"));

            return convertByteArrayToHexString(hashedBytes);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            //TODO: Este erro precisa de melhor handling
            System.out.println("Erro: Erro na criacao da hash da mensagem.");
            e.printStackTrace();
            return null;
        }
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
