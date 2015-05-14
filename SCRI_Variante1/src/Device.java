import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Utilizador on 02/05/2015.
 */
public class Device {
    private final double INVALID_VALUE = -10.0;
    private final double DOMAIN_X_MIN = 0.87;
    private final double DOMAIN_X_MAX = 6.08;
    HashMap< Integer, ArrayList<Double> > minutes;
    HashMap< Integer, ArrayList<Double> > history;
    double currentInsulin;
    ArrayList<Double> draValues = new ArrayList<Double>();
    int sensor1ErrorCounter;
    int sensor2ErrorCounter;


    public Device(){
        minutes = new HashMap<Integer, ArrayList<Double>>();
        history = new HashMap<Integer, ArrayList<Double>>();
        currentInsulin = 0.0;
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
        history.put(minute + 3*iteration , values);
        System.out.println("INSERIU VALORES PARA MIN " + (minute + 3*iteration ));
        return avaliateSensors();
    }

    private ArrayList<Double> avaliateInput(ArrayList<Double> minuteValues){
        // Avalia de os valores do sensor são validos
        boolean removeSensor1Value = false;
        boolean removeSensor2Value = false;

        // Quantos valores foram passados (2)
        if(minuteValues.size() > 2 || minuteValues.size() == 0){
            System.out.println("\t[InputAvaliation]Quantidade de valores passada errada.");
        }

        // Se foi passado algum "--"
        // Se os valores passados não estão no domínio da função
        if(minuteValues.get(0) == INVALID_VALUE){
            System.out.println("\t[InputAvaliation] Detecta falta de valor no sensor 1 ('--').");
            sensor1ErrorCounter++;
            removeSensor1Value = true;
        }
        else if(minuteValues.get(0) < DOMAIN_X_MIN || minuteValues.get(0) > DOMAIN_X_MAX){
            System.out.println("\t[InputAvaliation] Detectado valor errado no sensor 1 ("+ minuteValues.get(0) +").");
            sensor1ErrorCounter++;
            removeSensor1Value = true;
        }


        if(minuteValues.get(1) == INVALID_VALUE){
            System.out.println("\t[InputAvaliation] Detecta falta de valor no sensor 2 ('--').");
            sensor2ErrorCounter++;
            removeSensor2Value = true;
        }
        else if(minuteValues.get(1) < DOMAIN_X_MIN || minuteValues.get(1) > DOMAIN_X_MAX){
            System.out.println("\t[InputAvaliation] Detectado valor errado no sensor 2 ("+ minuteValues.get(1) +").");
            sensor2ErrorCounter++;
            removeSensor2Value = true;
        }

        // Remover valores que causam erros
        if(removeSensor1Value && removeSensor2Value){
            minuteValues.clear();
        }
        else if(removeSensor1Value){ minuteValues.remove(0); }
        else if(removeSensor2Value){ minuteValues.remove(1); }


        if(minuteValues.size() == 2){
            if( Math.abs(minuteValues.get(0) - minuteValues.get(1)) >= 1.5 ){
                System.out.println("\t [Device] Demasiada descrepancia de valores("+ minuteValues.get(0) + " em relacao a " + minuteValues.get(0) + ").");
                minuteValues.clear();
                // TODO Escolher qual usar atraves dos valores do historico
            }
        }

        return minuteValues;
    }

    public boolean avaliateSensors(){
        // TODO: Codigo para verificar stuckat
        if(sensor1ErrorCounter >= 5){
            System.out.println("\t[Device] Foram detectados demasiados erros no sensor 1. Por favor contacte o suporte");
            this.sensor1ErrorCounter = 0;
            return false;
        }
        if(sensor2ErrorCounter >= 5){
            System.out.println("\t[Device] Foram detectados demasiados erros no sensor 2. Por favor contacte o suporte");
            this.sensor2ErrorCounter = 0;
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
            System.out.println("\t[Device] Não deve ser injetada insulina - Glucose a ("+ gluc3 +")");
            return 0.0;
        }
        else if(gluc3 >= 6.0 && dg < -0.4){
            System.out.println("\t[Device] Não deve ser injetada insulina - Glucose a descer (dg = "+ dg +")");
            return 0.0;
        }
        else if(gluc3 >= 6.0 && dg >= -0.4 ){ //TODO Verificar com stor


            // Calculo das variações de glucose e numero de doses
            double ddg = (gluc2-gluc1) - dg;
            double doses = calcDoses(gluc3,dg,ddg,currentInsulin);
            // Round das doses a injectar
            int ndoses = (int) Math.round(doses);

            // Se der doses negativas
            if(ndoses < 0){
                System.out.println("\t[Device] Não deve ser injectada insulina pois já exite suficiente no corpo");
                return 0.0;
            }

            // Atualização da insulina no corpo
            if(ndoses == 0){
                ndoses = 1;
            }
            this.currentInsulin = ndoses + 0.9 * this.currentInsulin;
            System.out.println("\t[Device] Vai ser injetada insulina - " + ndoses + " doses");
            return ndoses;
        }
        else{
            System.out.println("\t[Device] Nao foi possível chegar a uma decisão. Demasiados inputs inválidos");
            return -1.0;
        }
    }

    private double calcDoses(double g, double dg, double ddg, double ins){ return 0.8*g + 0.2 * dg + 0.5*ddg - ins; }
    private double gluc(double entry){
        return -3.4 + 1.354 * entry + 1.545 * Math.tan( Math.pow(entry, 0.25));
    }
}
