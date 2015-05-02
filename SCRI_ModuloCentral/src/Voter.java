import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Leonel on 02/05/2015.
 */
public class Voter {
    ArrayList<Double> receivedValues = new ArrayList<Double>();
    private boolean consensus;
    private Double votingResult;
    private Double tolerance;

    Voter(ArrayList<Double> results){
        consensus = false;
        tolerance = 0.01;
        receivedValues = results;
        elect();
        // Forçar para testar logica do two pass adj
        //consensus = false;
    }

    private void elect(){
        if(receivedValues.size()>1){
            // Seleccionar um dos elementos da resposta aleatoriamente
            Random r = new Random();
            int index = r.nextInt(receivedValues.size());
            Double value1 = receivedValues.get(index);
            // Remover para não compara consigo mesmo
            receivedValues.remove(index);
            ArrayList<Double> feasibleSet = new ArrayList<Double>();

            // Adicionar ao feasible set os valores que estao dentro da tolerancia em relação ao valor seleccionado
            for(int i = 0; i < receivedValues.size(); i++){
                if(Math.abs(value1 - receivedValues.get(i)) < getTolerance()){
                    feasibleSet.add(receivedValues.get(i));
                }
            }

            // Seleccionar aleatoriamente do tamanho de feasible set.
            int indexF = r.nextInt(feasibleSet.size());
            consensus = true;
            votingResult = feasibleSet.get(indexF);
            return;

        }
        else if(receivedValues.size() == 1){
            // TODO: É assim que o formal majority voter faz?
            // Se só há 1 valor está a seleccionar esse
            consensus = true;
            votingResult = receivedValues.get(0);
            return;
        }
        else{
            System.out.println("\t Nao foram obtidos dados suficentes para avaliar. Vai ser retornado valor 0.00");
            consensus = true;
            votingResult = 0.0;
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

    public Double getVotingResult() {
        return votingResult;
    }
}
