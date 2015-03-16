package sorce;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Scri_main {
	
	public static void main(String[] args) throws IOException{
		double[] leitura = new double[2];
		int i = 1;
		while (true){
			leitura = read_Rf();
			System.out.println("leitura Nº " + i);
			System.out.println("Valor 1 - " + leitura[0]);
			System.out.println("Valor 2 - " + leitura[1]);
			i++;
		}
			
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public static double[] read_Rf() throws IOException {
		
	    BufferedReader br = new BufferedReader(new FileReader("rx.txt"));
        double[] retorno = new double[2];
	    try {
	        String line = br.readLine();	      
	        retorno[0] = Double.parseDouble(line.split(" ")[0]) ;
	        retorno[1] = Double.parseDouble(line.split(" ")[1]) ;
	    } finally {
	        br.close();
	    }
		return retorno ;
	}
	
	
}
