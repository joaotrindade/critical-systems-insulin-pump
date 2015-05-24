import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Leonel on 02/05/2015.
 */
public class Voter {
    ArrayList<Double> receivedValues = new ArrayList<Double>();
    private boolean consensus;
    private int votingResult;
    private Double tolerance;

    Voter(ArrayList<Double> results){
        consensus = false;
        tolerance = 1.1;
        receivedValues = results;
        elect();
    }

    private void elect(){
        ArrayList<Double> temp = new ArrayList<Double>(receivedValues);

        if(temp.size()>1){
            // Seleccionar um dos elementos da resposta aleatoriamente
            Random r = new Random();
            int index = r.nextInt(temp.size()-1);
            Double value1 = temp.get(index);
            // Remover para não compara consigo mesmo
            temp.remove(index);
            ArrayList<Double> feasibleSet = new ArrayList<Double>();

            // Adicionar ao feasible set os valores que estao dentro da tolerancia em relação ao valor seleccionado
            for(int i = 0; i < temp.size(); i++){
                if(Math.abs(value1 - temp.get(i)) < getTolerance()){
                    feasibleSet.add(temp.get(i));
                }
            }

            // Seleccionar aleatoriamente do tamanho de feasible set.
            if(feasibleSet.size() == 0){
                consensus = false;
                votingResult = -1;
            }
            else if(feasibleSet.size() == 1){
                // So captou 2 valores e sao compativeis
                consensus = true;
                votingResult = (int) Math.floor(value1);
            }
            else{
                int indexF = r.nextInt(feasibleSet.size()-1);
                consensus = true;
                votingResult = (int) Math.floor(feasibleSet.get(indexF));
            }
            return;

        }
        else if(temp.size() == 1){
            // Se só há 1 valor está a seleccionar esse
            consensus = true;
            votingResult = (int) Math.floor(temp.get(0));
            return;
        }
        else{
            System.out.println("\t[Voter]Nao foram obtidos dados suficentes para avaliar. Retornado NULL");
            consensus = true;
            votingResult = -1;
            return;

        }
    }

    public boolean getConsensus() {
        return consensus;
    }

    public Double getTolerance() {
        return tolerance;
    }

    public void setTolerance(Double tolerance) {
        this.tolerance = tolerance;
    }

    public int getVotingResult() {
        return votingResult;
    }
}
