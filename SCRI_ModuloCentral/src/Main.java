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

        byte[] recBuf = new byte[1024];

        int port1 = 6791;
        int port2 = 6792;
        int port3 = 6793;

        try {
            address = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }

        Boolean connected = false;
        int tentativa = 0;
        while(!connected){
            tentativa++;
            try {
                socketv1 = new Socket(address, port1);
                socketv1.setSoTimeout(10000);

                //socketv2 = new Socket(address, port2);
                //socketv2.setSoTimeout(10000);
//
                //socketv3 = new Socket(address, port3);
                //socketv3.setSoTimeout(10000);

                Thread.sleep(2000);

            } catch (SocketException e) {
                //e.printStackTrace();
                System.out.println("Socket não encontrado, a tentar conectar novamente");
                continue;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                if (socketv1 != null){
                    connected = true;
                }
            }

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
            int dra = 0;

            // Tem que haver valores para completar a iteração (6 por cada iteração)
            if(3* iterator + 3 < sensor1.size()){
                String message = generatePutData(iterator,dra);
                System.out.println("[Main]A enviar info de iteracao " + iterator + " e dra a " + dra);

                String sendBufString = message;

                outToServerV1.println(sendBufString);
                //outToServerV2.println(sendBufString);
                //outToServerV2.println(sendBufString);
            }
            else{
                System.out.println("[Main]Execução Terminou - Não há mais valores para avaliar");
                outToServerV1.println("end");
                //outToServerV2.println("end");
                //outToServerV3.println("end");

                String receivedV1 = inFromServerV1.readLine();
                //String receivedV2 = inFromServerV2.readLine();
                //String receivedV3 = inFromServerV3.readLine();

                if(receivedV1.equals("ack")){
                    return;
                }

            }

            try {

                String receivedV1 = inFromServerV1.readLine();
                //String receivedV2 = inFromServerV2.readLine();
                //String receivedV3 = inFromServerV3.readLine();

                System.out.println("[Main]Recebi de V1: " + receivedV1);
                //System.out.println("[Main] Recebi de V2: " + receivedV2);
                //System.out.println("[Main] Recebi de V3: " + receivedV3);

                // VOTADOR
                ArrayList<Double> results = new ArrayList<Double>();
                // Verifica Hash e retorn o valor, se hash for incorrecta devolve -1
                double resultV1 = verifyResponse(receivedV1);
                if(resultV1 != -1){ results.add(resultV1);} else{ System.out.println("[Main]Valor recebido da V1 inválido");}
                //if(resultV2 != -1){ results.add(resultV2);} else{ System.out.println("[Main]Valor recebido da V2inválido");}
                //if(resultV3 != -1){ results.add(resultV3);} else{ System.out.println("[Main]Valor recebido da V3 inválido");}

                Voter v = new Voter(results);
                if(v.getConsensus()){
                    System.out.println("[Main]Vai ser administrado o valor " + v.getVotingResult());
                }
                else{
                    System.out.println("[Main]Não houve consenso. Vai se recorrer a DRA");
                    // DRA
                    dra = 1;
                    // Enviar mensagens com dados DRA
                    String message = generatePutData(iterator,dra);
                    outToServerV1.println(message);
                    //outToServerV2.println("end");
                    //outToServerV3.println("end");
                    System.out.println("[Main]A enviar info de iteracao " + iterator + " e dra a " + dra);

                    // Receção de dados com DRA
                    receivedV1 = inFromServerV1.readLine();
                    //receivedV2 = inFromServerV2.readLine();
                    //receivedV3 = inFromServerV3.readLine();
                    System.out.println("[Main]Recebi de V1: " + receivedV1);
                    //System.out.println("[Main] Recebi de V2: " + receivedV2);
                    //System.out.println("[Main] Recebi de V3: " + receivedV3);
                    ArrayList<Double> resultsDRA = new ArrayList<Double>();

                    // Verifica Hash e retorn o valor, se hash for incorrecta devolve -1
                    resultV1 = verifyResponse(receivedV1);
                    if(resultV1 != -1){ resultsDRA.add(resultV1);} else{ System.out.println("[Main]Valor recebido da V1 inválido");}
                    //if(resultV2 != -1){ resultsDRA.add(resultV2);} else{ System.out.println("[Main]Valor recebido da V2inválido");}
                    //if(resultV3 != -1){ resultsDRA.add(resultV3);} else{ System.out.println("[Main]Valor recebido da V3 inválido");}

                    // Nova Votacao
                    Voter vDRA = new Voter(resultsDRA);
                    if(vDRA.getConsensus()){
                        double revertedValue = revertDRA(vDRA.getVotingResult());
                        System.out.println("[Main]Vai ser administrado o valor " + revertedValue);
                    }
                    else{
                        System.out.println("[Main]Não houve consenso. Sistema não obteve resposta para a iteração atual");
                    }
                }

                int delay = 4;
                System.out.println("[Main]À espera "+ delay + " segundos até a próxima iteração");
                Thread.sleep(delay * 1000);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            iterator++;
        }
    }

    public static double revertDRA(double value){
        // TODO: revert dra function
        return value;
    }

    public static double verifyResponse(String response){
        String[] parts = response.split(" ");
        String action   = parts[0];
        String iteration= parts[1];
        String dra      = parts[2];
        String result   = parts[3];
        String hash     = parts[4];

        String receivedStr = action + " " + iteration + " " + dra + " " + result;
        String comparable = hashString(receivedStr);

        if(comparable.equals(hash)){
            System.out.println("\tHash da resposta verificada com resultado " + result);
            return Double.parseDouble(result);
        }
        else{
            return -1.0;
        }
    }

    public static void readFile() {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("SCRI_ModuloCentral/input.txt"));
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
