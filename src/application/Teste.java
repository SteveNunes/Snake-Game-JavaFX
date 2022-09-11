package application;

import java.util.Arrays;
import java.util.List;

public class Teste {

	public static void main(String[] args) {
		List<String> nomes = Arrays.asList("Steve", "João", "Maria", "Maneca", "Pedro", "Claudio", "Luiz", "Lucas");
		System.out.println(nomes.subList(1, nomes.size()));
	}
	
}
