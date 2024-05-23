package Application;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Font;

public class TPED_Reader extends JFrame{
	private static final long serialVersionUID = 5047521750664878558L; //not really important, part of swing ui
	private int height;
	private int width;
	private JButton fileButton;
	private JButton toggleDisabledButton;
	private JLabel fileLabel;
	private JLabel idLabel;
	private JLabel totalMarkersLabel;
	private JLabel nucleotideLabel;
	private JList<String> chromosomeList;
	private DefaultListModel<String> chromosomeListModel;
	private JList<String> nucleotideList;
	private DefaultListModel<String> nucleotideListModel;
	private JScrollPane chromosomeListScroller;
	private JScrollPane nucleotideListScroller;
	private File TPEDFile;
	private ArrayList<Chromosome> chromosomes;
	private Chromosome selectedChromosome;

	//use a filechooser to get a file from the computer. Returns File class if a valid file is chosen or null otherwise. 
	public File OpenFile() {
		JFileChooser fileChooser = new JFileChooser();
		int r = fileChooser.showOpenDialog(null);
		
		// if the user selects a file
        if (r == JFileChooser.APPROVE_OPTION) {
        	return fileChooser.getSelectedFile();
        }
        else {
        	return null;
        }
	}
	
	//show a popup message with the given text
	public void PopupMessage(String _message) {
		JOptionPane.showMessageDialog(this, _message);
	}
	
	//function used to increase the count of a value on a hashmap with integer values and string keys such as the ones in the Chromosome class. 
	//Negative increment will decrease the count by that value. If no key exists for the entered value, one will be created with a value of 0. 
	private void IncreaseHash(HashMap<String,Integer> hash, String key ,Integer increment) {
		int currentValue;
		if(hash.get(key) == null) {
			hash.put(key, 0);
		}
		
		currentValue = hash.get(key);
		
		hash.put(key, currentValue + increment);
	}
	
	//reads a file and returns a list of chromosomes from the file. 
	private ArrayList<Chromosome> GetChromosomesFromFile(File _file){
		ArrayList<Chromosome> returnList = new ArrayList<Chromosome>();
		Chromosome currentChromosome = null; //chromosome that contains the marker being processed. 
		String line = "";
		String currentID; //id of the current chromosome. 
		boolean alreadyContains;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(_file));
			
			//add the "total" chromosome to the start of the list.
			returnList.add(new Chromosome("total"));
			
			while ((line = br.readLine()) != null)   //go through each line in the file, each line represents a genetic marker. 
			{
				String[] splitLine = line.split(" ");   // use space as separator. Each data point for a marker/line is represented in the splitLine array as a string. 
				
				//chromosome id is represented by the first data point
				currentID = splitLine[0];
				
				//add the chromosome to the list if it is not already included. 
				alreadyContains = false;
				for(int i=0;i<returnList.size();i++) {
					if(returnList.get(i).id.equals(currentID)) {
						alreadyContains = true;
						currentChromosome = returnList.get(i);
					}
				}
				if(alreadyContains == false) {
					returnList.add(new Chromosome(currentID));
					currentChromosome = returnList.get(returnList.size() - 1);
				}
				
				//increase the total marker count for the chromosome.
				currentChromosome.totalMarkers++;
				
				//if the fifth and sixth data points are identical, add two to the homozygous count for that nucleotide. Otherwise, increase the heterozygous count by one for each nucleotide. 
				if(splitLine[4].equals(splitLine[5])) {
					IncreaseHash(currentChromosome.homozygousNucleotides, splitLine[4], 2);
				}
				else {
					IncreaseHash(currentChromosome.heterozygousNucleotides, splitLine[4], 1);
					IncreaseHash(currentChromosome.heterozygousNucleotides, splitLine[5], 1);
				}
				
				//increase the nucleotide counts for each nucleotide on the marker. 
				IncreaseHash(currentChromosome.nucleotides, splitLine[4], 1);
				IncreaseHash(currentChromosome.nucleotides, splitLine[5], 1);
			}
			br.close();
		}
		catch(Exception E) {
			//E.printStackTrace();
			PopupMessage("Error: " + E.toString());
			return null;
		}
		
		//calculate the totals for each chromosome.
		for(Chromosome chromosome : returnList) {
			chromosome.CalculateTotals();
		}
		
		return returnList;
	}
	
	private void UpdateChromosomeList() {
		//clear existing chromosomes
		chromosomeListModel.clear();
		
		if(chromosomes == null) {
			return;
		}
		
		//add species and portraits to their respective lists
		for(Chromosome chromo : chromosomes) {
			chromosomeListModel.addElement(chromo.id);
		}
	}
	
	//returns the chromosome in a list that matches the id, returns null if no match is found. 
	public Chromosome FindChromosomeByID(List<Chromosome> _list, String _id) {
		Chromosome returnChromosome = null;
		
		for(Chromosome chromo : _list) {
			if(chromo.id.equals(_id)) {
				returnChromosome = chromo;
				break;
			}
		}
		
		return returnChromosome;
	}
	
	//returns the index of a chromosome in a list that matches the id, returns -1 if no match is found. 
	public Integer FindIndexByID(List<Chromosome> _list, String _id) {
		int returnInt = -1;
		
		for(int i = 0; i < _list.size(); i++) {
			if(_list.get(i).id.equals(_id)) {
				returnInt = i;
				break;
			}
		}
		
		return returnInt;
	}
	
	//add the counts for each chromosome together to fill out the "total" chromosome, which is at index 0. 
	private void CalculateTotalChromosome() {
		Chromosome totalChromo = new Chromosome("total"); //this chromosome will replace the previous "total" chromosome. 
		for(Chromosome chromo : chromosomes) {
			//exclude disabled chromosomes and the "total" chromosome from calculation. 
			if(chromo.disabled == true || chromo.id.equals("total")) {
				continue;
			}
			
			totalChromo.totalMarkers += chromo.totalMarkers;

			for(String key : chromo.nucleotides.keySet()) {
				if(totalChromo.nucleotides.containsKey(key) == false) {
					totalChromo.nucleotides.put(key, 0);
					totalChromo.homozygousNucleotides.put(key, 0);
					totalChromo.heterozygousNucleotides.put(key, 0);
				}
				
				totalChromo.nucleotides.put(key, ( totalChromo.nucleotides.get(key) + chromo.nucleotides.get(key) ) );
				if(chromo.homozygousNucleotides.containsKey(key)) {
					totalChromo.homozygousNucleotides.put(key, ( totalChromo.homozygousNucleotides.get(key) + chromo.homozygousNucleotides.get(key) ) );
				}
				if(chromo.heterozygousNucleotides.containsKey(key)) {
					totalChromo.heterozygousNucleotides.put(key, ( totalChromo.heterozygousNucleotides.get(key) + chromo.heterozygousNucleotides.get(key) ) );				}	
			}
		}
		chromosomes.set(0, totalChromo);
	}
	
	//class constructor, all code here runs when the class if first instantiated. This is where all UI items are defined and created. 
	public TPED_Reader() {
		this.width = 1200;
		this.height = 800;
		
		this.chromosomeListModel = new DefaultListModel<String>();
		this.nucleotideListModel = new DefaultListModel<String>();
		
		//required to make the program fully shut down when closed. 
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		this.setSize(width, height);
		getContentPane().setLayout(null);
		
		//set up labels.
		fileLabel = new JLabel("No TPED File");
		fileLabel.setHorizontalAlignment(SwingConstants.CENTER);
		fileLabel.setBounds(10, 0, 160, 28);
		getContentPane().add(fileLabel);
		
		idLabel = new JLabel("Chromosome: none selected");
		idLabel.setBounds(220, 38, 347, 28);
		getContentPane().add(idLabel);
		
		totalMarkersLabel = new JLabel("Total markers: ");
		totalMarkersLabel.setBounds(220, 60, 160, 28);
		getContentPane().add(totalMarkersLabel);
		
		nucleotideLabel = new JLabel("Nucleotides in the selected chromosome:");
		nucleotideLabel.setBounds(220, 82, 300, 28);
		getContentPane().add(nucleotideLabel);
		
		//set up buttons.
		fileButton = new JButton("Open TPED File");
		fileButton.setBounds(10, 38, 160, 50);
		fileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					TPEDFile = OpenFile();
					if(TPEDFile == null) {
						fileLabel.setText("No TPED file.");
					}
					else {
						fileLabel.setText(TPEDFile.getName());
						chromosomes = GetChromosomesFromFile(TPEDFile);
						UpdateChromosomeList();
					}
				}
				catch(Exception E){
					PopupMessage("Error: " + E.toString());
					TPEDFile = null;
					fileLabel.setText("No TPED file.");
				}
			}
		});
		getContentPane().add(fileButton);
		
		toggleDisabledButton = new JButton("Disable Chromosome");
		toggleDisabledButton.setBounds(10, 670, 160, 50);
		toggleDisabledButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if(selectedChromosome == null) {
						PopupMessage("No chromosome has been selected");
					}
					else if(selectedChromosome.id.equals("total")) {
						PopupMessage("'total' cannot be disabled");
					}
					else {
						if(selectedChromosome.disabled) {
							selectedChromosome.disabled = false;
							PopupMessage("chromosome enabled: " + selectedChromosome.id + " will now be used for 'total' calculations again.");
							toggleDisabledButton.setText("Disable Chromosome");
							chromosomeListModel.set(chromosomeList.getSelectedIndex(), selectedChromosome.id);
						}
						else {
							selectedChromosome.disabled = true;
							PopupMessage("chromosome disabled: " + selectedChromosome.id + " will no longer be used for 'total' calculations.");
							toggleDisabledButton.setText("Enable Chromosome");
							chromosomeListModel.set(chromosomeList.getSelectedIndex(), selectedChromosome.id + " - Disabled");
						}
					}
				}
				catch(Exception E){
					PopupMessage("Error: " + E.toString());
				}
			}
		});
		getContentPane().add(toggleDisabledButton);
		
		//set up lists. 
		chromosomeList = new JList<String>(chromosomeListModel);
		chromosomeList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				try{
					//this block happens when a chromosome in the list is clicked. 
					
					//isolate the chromosome ID from the selected value. This is used to remove things such as the " - Disabled" indicator. 
					String[] splitItem = chromosomeList.getSelectedValue().split(" ");
					String chromosomeID = splitItem[0];
					
					//if the selected chromosome has not changed then do nothing. 
					if(selectedChromosome != null && selectedChromosome.id.equals("total") == false && selectedChromosome.id.equals(chromosomeID)) {
						return;
					}
					
					selectedChromosome = FindChromosomeByID(chromosomes, chromosomeID);
					
					//if the selected chromosome could not be found in the chromosomes list, set the selectedChromosome to an empty chromosome and display an error popup. 
					if(selectedChromosome == null) {
						selectedChromosome = new Chromosome("");
						PopupMessage("Error: chromosome could not be found in the data.");
					}
					
					//if the selcted chromosome is the "total" chromosome, calculate it. 
					if(selectedChromosome.id.equals("total")) {
						CalculateTotalChromosome();
					}
					
					//set the text of the toggleDisabledButton to reflect the state of the selected chromosome. 
					if(selectedChromosome.disabled) {
						toggleDisabledButton.setText("Enable Chromosome");
					}
					else {
						toggleDisabledButton.setText("Disable Chromosome");
					}
					
					idLabel.setText("Chromosome: " + selectedChromosome.id);
					totalMarkersLabel.setText("Total Markers: " + selectedChromosome.totalMarkers);
					
					nucleotideListModel.clear();
					
					nucleotideListModel.addElement("total: " + selectedChromosome.nucleotides.get("total") + ",   " + selectedChromosome.homozygousNucleotides.get("total") + " homozygous,   " + selectedChromosome.heterozygousNucleotides.get("total") + " heterozygous.");
					
					int homozygous = 0;
					int heterozygous = 0;
					for(String key : selectedChromosome.nucleotides.keySet()) {
						if(key.equals("total")) {
							continue;
						}
						
						if(selectedChromosome.homozygousNucleotides.containsKey(key)) {
							homozygous = selectedChromosome.homozygousNucleotides.get(key);
						}
						else {
							homozygous = 0;
						}
						
						if(selectedChromosome.heterozygousNucleotides.containsKey(key)) {
							heterozygous = selectedChromosome.heterozygousNucleotides.get(key);
						}
						else {
							heterozygous = 0;
						}
						
						nucleotideListModel.addElement(key + ": " + selectedChromosome.nucleotides.get(key) + " total,   " + homozygous + " homozygous,   " + heterozygous + " heterozygous.");
					}
				}
				catch(Exception E) {
					PopupMessage("Error: " + E);
				}
			}
		});
		chromosomeListScroller = new JScrollPane(chromosomeList);
		chromosomeListScroller.setBounds(10, 109, 160, 550);
		getContentPane().add(chromosomeListScroller);
		
		nucleotideList = new JList<String>(nucleotideListModel);
		nucleotideListScroller = new JScrollPane(nucleotideList);
		nucleotideListScroller.setBounds(220, 110, 400, 550);
		getContentPane().add(nucleotideListScroller);

	}
		
		
}
