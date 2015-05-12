import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Utilizador on 02/05/2015.
 */
public class Device {
    private final double INVALID_VALUE = -10.0;
    HashMap< Integer, ArrayList<Double> > minutes;
    HashMap< Integer, ArrayList<Double> > historico;
    ArrayList<Double> draValues = new ArrayList<Double>();
    int sensor1ErrorCounter;
    int sensor2ErrorCounter;

    public Device(){
        minutes = new HashMap<Integer, ArrayList<Double>>();
        historico = new HashMap<Integer, ArrayList<Double>>();
        sensor1ErrorCounter = 0;
        sensor2ErrorCounter = 0;
    }

    public ArrayList<Double> getMinute(int minute){
        return minutes.get(minute);
    }

    public void addDRAMinute(ArrayList<Double> values){
        for(int i = 0; i < values.size(); i++){
            draValues.add(values.get(i));
        }
    }

    public void discardOldDRAValues(){
        draValues.clear();
    }

    public boolean addMinute(ArrayList<Double> minuteValues, int minute, int iteration){
        ArrayList<Double> values = avaliateInput(minuteValues);
        minutes.put(minute, values);
        historico.put(minute + iteration , values);
        return avaliateSensors();
    }

    private ArrayList<Double> avaliateInput(ArrayList<Double> minuteValues){
        // Avalia de os valores do sensor são validos

        // ver a diferença entre os 2
        if(minuteValues.size() > 2 || minuteValues.size() == 0){
            System.out.println("Quantidade de valores passada errada.");
        }
        if(minuteValues.get(0) < 0 || minuteValues.get(0) > 30 || minuteValues.get(0) == INVALID_VALUE){
            System.out.println("\t [Device] Detectado erro no valor do sensor 1 ("+ minuteValues.get(0) +").");
            sensor1ErrorCounter++;
            minuteValues.remove(0);
        }
        if(minuteValues.get(1) < 0 || minuteValues.get(1) > 30 || minuteValues.get(1) == INVALID_VALUE){
            System.out.println("\t [Device] Detectado erro no valor do sensor 2 ("+ minuteValues.get(1) +").");
            sensor2ErrorCounter++;
            minuteValues.remove(1);
        }
        if(minuteValues.size() == 2){
            if( Math.abs(minuteValues.get(0) - minuteValues.get(1)) >= 1.5 ){
                System.out.println("\t [Device] Demasiada descrepancia de valores("+ minuteValues.get(0) + " em relacao a " + minuteValues.get(0) + ").");
                // TODO Escolher qual usar atraves dos valores do historico
            }
        }
        return minuteValues;
    }

    public boolean avaliateSensors(){
        // TODO: Codigo para verificar stuckat
        if(sensor1ErrorCounter >= 5){
            System.out.println("\t[Device] Foram detectados demasiados erros no sensor 1. Por favor contacte o suporte");
            return false;
        }
        if(sensor2ErrorCounter >= 5){
            System.out.println("\t[Device] Foram detectados demasiados erros no sensor 2. Por favor contacte o suporte");
            return false;
        }
        return true;
    }

    public double process(){
        // A partir dos 2 valores do sensor obter um valor - esta variante chega a esse valor pela média
        // Como estes valores podem ser apagados (por serem invalidos) é necessario percorre los como abaixo

        double sum = 0;
        for(int i = 0; i < minutes.get(1).size(); i++){
            sum+=minutes.get(1).get(i);
        }
        double minute1 = sum / minutes.get(1).size();
        sum = 0;
        for(int i = 0; i < minutes.get(2).size(); i++){
            sum+=minutes.get(2).get(i);
        }
        double minute2 = sum / minutes.get(2).size();
        sum = 0;

        for(int i = 0; i < minutes.get(3).size(); i++){
            sum+=minutes.get(3).get(i);
        }
        double minute3 = sum / minutes.get(3).size();

        double gluc1 = gluc(minute1);
        double gluc2 = gluc(minute2);
        double gluc3 = gluc(minute3);
        double dg = gluc3 - gluc2;

        // Se o nível de açúcar for abaixo de 6.0 nao é injetada insulina
        // Se o nível de açúcar for acima de 6.0 mas a variação mostra que este valor está a descrescer a mais que
        // 0.4 por minuto então não é injetada insulina
        if(gluc3 < 6.0){
            System.out.println("\t[Device] Nao vai ser injetada insulina nesta iteracao");
            return 0.0;
        }
        else if(gluc3 >= 6.0 && dg < -0.4){
            System.out.println("\t[Device] Nao vai ser injetada insulina nesta iteracao");
            return 0.0;
        }
        else if(gluc3 >= 6.0 && dg >= 0.4 ){
            System.out.println("\t[Device] Vai ser injetada insulina nesta iteracao");
            double ddg = 0;
        }


        return 0.0;
    }

    private double gluc(double entry){
        return -3.4 + 1.354 * entry + 1.545 * Math.tan( Math.pow(entry, 0.25));
    }
}
