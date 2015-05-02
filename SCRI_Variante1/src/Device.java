import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Utilizador on 02/05/2015.
 */
public class Device {
    HashMap< Integer, ArrayList<Double> > minutes;
    ArrayList<Double> draValues = new ArrayList<Double>();

    public Device(){
        minutes = new HashMap<Integer, ArrayList<Double>>();
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

    public boolean addMinute(ArrayList<Double> minuteValues, int minute){
        int inputAval = avaliateInput(minuteValues);
        if(inputAval == 0){
            minutes.put(minute, minuteValues);
            return avaliateSensors();
        }
        else{
            System.out.println("Valores impossíveis, algo de errado com algum o sensor " + inputAval);
            return false;
        }
    }

    private int avaliateInput(ArrayList<Double> minuteValues){
        // TODO: Avalia de os valores do sensor são validos
        return 0;
    }

    public boolean avaliateSensors(){
        // TODO: Codigo para verificar stuckat
        return true;
    }

    public double process(){
        return 0.0;
    }
}
