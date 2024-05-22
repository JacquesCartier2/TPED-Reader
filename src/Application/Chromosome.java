package Application;

import java.util.HashMap;

public class Chromosome {
	String id;
	//hashmaps (aka dictionaries) are used to store numbers with their respective markers, for example in nucleotides a key of "A" and value of "10" indicated that the chromosome has 10 A nucleotides. 
	HashMap<String,Integer> nucleotides; //amount of nucleotides of a certain type.
	HashMap<String,Integer> homozygousNucleotides; //amount of nucleotides in homozygous sites for each nucleotide type. 
	HashMap<String,Integer> heterozygousNucleotides; //amount of nucleotides in heterozygous sites for each nucleotide type. 
	int totalMarkers; //total amount of markers for the chromosome. 
	
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
		nucleotides.put("total", 0);
		for(String key : nucleotides.keySet()) {
			if(key.equals("total") == false) {
				amountToAdd = nucleotides.get("total") + nucleotides.get(key);
				nucleotides.put("total", amountToAdd);
			}
		}
		homozygousNucleotides.put("total", 0);
		for(String key : homozygousNucleotides.keySet()) {
			if(key.equals("total") == false) {
				amountToAdd = homozygousNucleotides.get("total") + homozygousNucleotides.get(key);
				homozygousNucleotides.put("total", amountToAdd);
			}
		}
		heterozygousNucleotides.put("total", 0);
		for(String key : heterozygousNucleotides.keySet()) {
			if(key.equals("total") == false) {
				amountToAdd = heterozygousNucleotides.get("total") + heterozygousNucleotides.get(key);
				heterozygousNucleotides.put("total", amountToAdd);
			}
		}
	}
	
	Chromosome(String _id){
		this.id = _id;
		this.nucleotides = new HashMap<String,Integer>();
		this.homozygousNucleotides = new HashMap<String,Integer>();
		this.heterozygousNucleotides = new HashMap<String,Integer>();
		this.totalMarkers = 0;
	}
}
