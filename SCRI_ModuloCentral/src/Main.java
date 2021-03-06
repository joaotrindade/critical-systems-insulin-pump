import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


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

        PrintWriter     outToServerV1 = null;
        BufferedReader  inFromServerV1 = null;
        PrintWriter     outToServerV2 = null;
        BufferedReader  inFromServerV2 = null;
        PrintWriter     outToServerV3 = null;
        BufferedReader  inFromServerV3 = null;

        byte[] recBuf = new byte[1024];

        int port1 = 6791;
        int port2 = 6792;
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

        boolean VAR1_CONNECTED = false;
        boolean VAR2_CONNECTED = false;
        boolean VAR3_CONNECTED = false;

        boolean VAR1_ENDED = false;
        boolean VAR2_ENDED = false;
        boolean VAR3_ENDED = false;

        while(!connected){
            tentativa++;
            try {
                socketv1 = new Socket(address, port1);
                socketv1.setSoTimeout(10000);

                socketv2 = new Socket(address, port2);
                socketv2.setSoTimeout(10000);
//
                socketv3 = new Socket(address, port3);
                socketv3.setSoTimeout(10000);

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
                    VAR1_CONNECTED = true;
                }
                if (socketv2 != null){
                    VAR2_CONNECTED = true;
                }
                if (socketv3 != null){
                    VAR3_CONNECTED = true;
                }

                // Para testar uma variante individualmente descomentar esta condição e comentar a abaixo
                //if(VAR1_CONNECTED || VAR2_CONNECTED || VAR3_CONNECTED){
                //    connected = true;
                //}

                if(VAR1_CONNECTED && VAR2_CONNECTED && VAR3_CONNECTED){
                    connected = true;
                }
            }

        }

        try {
            if(VAR1_CONNECTED){
                outToServerV1= new PrintWriter(socketv1.getOutputStream(),true);
                inFromServerV1 = new BufferedReader(new InputStreamReader(socketv1.getInputStream()));
            }

            if(VAR2_CONNECTED){
                outToServerV2 = new PrintWriter(socketv2.getOutputStream(),true);
                inFromServerV2 = new BufferedReader(new InputStreamReader(socketv2.getInputStream()));
            }

            if(VAR3_CONNECTED){
                outToServerV3 = new PrintWriter(socketv3.getOutputStream(),true);
                inFromServerV3 = new BufferedReader(new InputStreamReader(socketv3.getInputStream()));
            }


        } catch (IOException e) {
            System.out.println("Erro: Ligacao ao socket.");
            e.printStackTrace();
            return;
        }

        int iterator = 0;
        double currentInsulin = 0.0;

        while(true)
        {

            // Tem que haver valores para completar a iteração (6 por cada iteração)
            if(3* iterator + 3 < sensor1.size()){
                String message = generatePutData(iterator, currentInsulin);
                System.out.println("[Main]["+iterator+"]A enviar info de iteracao " + iterator);

                String sendBufString = message;

                if(VAR1_CONNECTED) { outToServerV1.println(sendBufString); }
                if(VAR2_CONNECTED) { outToServerV2.println(sendBufString); }
                if(VAR3_CONNECTED) { outToServerV3.println(sendBufString); }

            }
            else{
                System.out.println("[Main]["+iterator+"]Execucao Terminou - Nao ha mais valores para avaliar");

                try{
                    if(VAR1_CONNECTED) {
                        outToServerV1.println("end");
                        String receivedV1 = inFromServerV1.readLine();

                        if (receivedV1.equals("ack")) {
                            VAR1_ENDED = true;
                        }
                    }
                }catch (IOException e){
                    System.out.println("Erro: Empty Socket Var1");
                }

                try{
                    if(VAR2_CONNECTED) {
                        outToServerV2.println("end");
                        String receivedV2 = inFromServerV2.readLine();

                        if (receivedV2.equals("ack")) {
                            VAR2_ENDED = true;
                        }
                    }
                }catch (IOException e){
                    System.out.println("Erro: Empty Socket Var2");
                }


                try{
                    if(VAR3_CONNECTED) {
                        outToServerV3.println("end");
                        String receivedV3 = inFromServerV3.readLine();

                        if (receivedV3.equals("ack")) {
                            VAR3_ENDED = true;
                        }
                    }
                }catch (IOException e){
                    System.out.println("Erro: Empty Socket Var3");
                }

                if(VAR1_ENDED && VAR2_ENDED && VAR3_ENDED){
                    return;
                }

            }

            String receivedV1 = "";
            String receivedV2 = "";
            String receivedV3 = "";

            ArrayList<Double> results = new ArrayList<Double>();

            // Tentar ler a cada Variante
            try {
                if(VAR1_CONNECTED){
                    receivedV1 = inFromServerV1.readLine();
                    double resultV1 = verifyResponse(receivedV1,1);
                    if(resultV1 != INVALID_HASH && resultV1 != NO_RETURN_FROM_VARIANT){ results.add(resultV1);}
                }
            }
            catch(IOException e) {
                System.out.println("[Main] Variante 1 Desconectada");
                VAR1_CONNECTED = false;
            }

            try {
                if(VAR2_CONNECTED){
                    receivedV2 = inFromServerV2.readLine();
                    double resultV2 = verifyResponse(receivedV2,2);
                    if(resultV2 != INVALID_HASH && resultV2 != NO_RETURN_FROM_VARIANT){ results.add(resultV2);}
                }
            }
            catch(IOException e) {
                System.out.println("[Main] Variante 2 Desconectada");
                VAR2_CONNECTED = false;
            }


            try {
                if(VAR3_CONNECTED){
                    receivedV3 = inFromServerV3.readLine();
                    double resultV3 = verifyResponse(receivedV3,3);
                    if(resultV3 != INVALID_HASH && resultV3 != NO_RETURN_FROM_VARIANT){ results.add(resultV3);}
                }
            }
            catch(IOException e) {
                System.out.println("[Main] Variante 3 Desconectada");
                VAR3_CONNECTED = false;
            }


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

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.##", otherSymbols);
        res+= df.format(currentInsulin);

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
