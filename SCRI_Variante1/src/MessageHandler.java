import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by Utilizador on 02/05/2015.
 */
public class MessageHandler {
    private final double INVALID_VALUE = -10.0;

    private String rawMessage;
    private String action;
    private int iteration;
    private int dra;
    private Timestamp timestamp;
    public ArrayList<Double> minute1 = new ArrayList<Double>();
    public ArrayList<Double> minute2 = new ArrayList<Double>();
    public ArrayList<Double> minute3 = new ArrayList<Double>();

    public MessageHandler(String message){

        String[] parts = message.split(" ");
        if (parts.length != 11){
            System.out.println("\t[Message Handler] Mensagem inválida - Faltam elementos");
            return;
        }

        rawMessage  = message;
        action      = parts[0];
        iteration   = Integer.parseInt(parts[1]);
        dra         = Integer.parseInt(parts[2]);
        timestamp   = new Timestamp(Long.parseLong(parts[3]));

        for(int i = 4; i < parts.length-1; i++){
            if(parts[i] == "--"){
                parts[i] = "" + INVALID_VALUE;
            }
        }

        minute1.add(Double.parseDouble(parts[4]));
        minute1.add(Double.parseDouble(parts[5]));

        minute2.add(Double.parseDouble(parts[6]));
        minute2.add(Double.parseDouble(parts[7]));

        minute3.add(Double.parseDouble(parts[8]));
        minute3.add(Double.parseDouble(parts[9]));

        String reveivedHash = parts[10];

        if(verifyHashReceived(reveivedHash)){
            System.out.println("\t[Message Handler] Hash verificada com sucesso");
        }


    }

    private boolean verifyHashReceived(String receivedHash){

        String storedData = "";
        storedData += action + " " + getIteration() + " " + getDra() + " " + getTimestamp().getTime() + " ";
        storedData += minute1.get(0) + " " + minute1.get(1) + " ";
        storedData += minute2.get(0) + " " + minute2.get(1) + " ";
        storedData += minute3.get(0) + " " + minute3.get(1);

        String generatedHash = hashString(storedData);

        if(receivedHash.equals(generatedHash)){
            return true;
        }
        else{
            return false;
        }


    }

    public String generateAnswer(double value){
        String res = "";
        res+= "putresult ";
        res+= this.iteration + " ";
        res+= dra + " ";
        res+= value;

        String hashed = hashString(res);

        res += " ";
        res += hashed;

        return res;
    }


    private String hashString(String message) {
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

    private String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }

    public int getIteration() {
        return iteration;
    }

    public int getDra() {
        return dra;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
