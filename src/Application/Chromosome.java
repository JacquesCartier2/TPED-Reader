package Application;

import java.util.HashMap;
import java.util.List;

public class Chromosome {
	String id;
	//hashmaps (aka dictionaries) are used to store numbers with their respective markers, for example in nucleotides a key of "A" and value of "10" indicated that the chromosome has 10 A nucleotides. 
	HashMap<String,Integer> nucleotides; //total amount of nucleotides of a certain type.
	HashMap<String,Integer> homozygousNucleotides; //amount of nucleotides in homozygous sites for each nucleotide type. 
	HashMap<String,Integer> heterozygousNucleotides; //amount of nucleotides in heterozygous sites for each nucleotide type. 
	int totalMarkers; //total amount of markers for the chromosome. 
	boolean disabled;
	
	//returns a hashmap of heterozygous sites by subtracting the amount of homozygous sites from the total amount of sites (two nucleotides per site). 
	public HashMap<String,Integer> GetHeterozygous(){
		HashMap<String,Integer> heterozygous = new HashMap<String,Integer>();
		//for each type of nucleotide, 
		for(String nucleotide : nucleotides.keySet()) {
			heterozygous.put(nucleotide, (nucleotides.get(nucleotide)/2) - homozygousNucleotides.get(nucleotide));
		}
		return heterozygous;
	}
	
	//adds a "total" key to all hashmaps and stores the total counts for all other entries under it. 
	public void CalculateTotals() {
		int amountToAdd = 0;
		CalculateTotal(nucleotides);
		CalculateTotal(homozygousNucleotides);
		CalculateTotal(heterozygousNucleotides);
	}
	
	private void CalculateTotal(HashMap<String,Integer> _hashMap) {
		int totalAmount = 0;
		for(String key : _hashMap.keySet()) {
			if(key.equals("total") == false) {
				totalAmount += _hashMap.get(key);
			}
		}
		_hashMap.put("total", totalAmount);
	}
	
	Chromosome(String _id){
		this.id = _id;
		this.nucleotides = new HashMap<String,Integer>();
		this.homozygousNucleotides = new HashMap<String,Integer>();
		this.heterozygousNucleotides = new HashMap<String,Integer>();
		this.totalMarkers = 0;
		this.disabled = false;
	}
}
