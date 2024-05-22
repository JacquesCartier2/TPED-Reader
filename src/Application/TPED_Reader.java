package Application;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Font;

public class TPED_Reader extends JFrame{
	private static final long serialVersionUID = 5047521750664878558L; //not really important, part of swing ui
	private int height;
	private int width;
	private JButton fileButton;
	private JLabel fileLabel;
	private JLabel idLabel;
	private JLabel totalMarkersLabel;
	private JLabel nucleotideLabel;
	private JLabel homozygousNucleotideLabel;
	private JLabel heterozygousNucleotideLabel;
	private JList<String> chromosomeList;
	private DefaultListModel<String> chromosomeListModel;
	private JList<String> nucleotideList;
	private DefaultListModel<String> nucleotideListModel;
	private JList<String> homozygousNucleotideList;
	private DefaultListModel<String> homozygousNucleotideListModel;
	private JList<String> heterozygousNucleotideList;
	private DefaultListModel<String> heterozygousNucleotideListModel;
	private JScrollPane chromosomeListScroller;
	private JScrollPane nucleotideListScroller;
	private JScrollPane homozygousNucleotideListScroller;
	private JScrollPane heterozygousNucleotideListScroller;
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
		int currentIndex = 0; //returnList index of the current chromosome. 
		boolean alreadyContains;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(_file));
			
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
						currentIndex = i;
					}
				}
				if(alreadyContains == false) {
					returnList.add(new Chromosome(currentID));
					currentChromosome = returnList.get(returnList.size() - 1);
					currentIndex = (returnList.size() - 1);
				}
				
				//increase the total marker count for the chromosome.
				currentChromosome.totalMarkers++;
				
				//if the fifth and sixth data points are identical, add two to the homozygous count for that nucleotide. Otherwise, increase the heterozygous count by one for each nucleotide. 
				if(splitLine[4].equals(splitLine[5])) {
					IncreaseHash(currentChromosome.homozygousNucleotides, splitLine[4], 2);
				}
				else {
					IncreaseHash(currentChromosome.homozygousNucleotides, splitLine[4], 1);
					IncreaseHash(currentChromosome.homozygousNucleotides, splitLine[5], 1);
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
	
	//display a chromosome's information when selected. 
	private void ChromosomeSelected() {
		
	}
	
	//class constructor, all code here runs when the class if first instantiated. This is where all UI items are defined and created. 
	public TPED_Reader() {
		this.width = 600;
		this.height = 600;
		
		this.chromosomeListModel = new DefaultListModel<String>();
		this.nucleotideListModel = new DefaultListModel<String>();
		this.homozygousNucleotideListModel = new DefaultListModel<String>();
		this.heterozygousNucleotideListModel = new DefaultListModel<String>();
		
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
		idLabel.setBounds(220, 11, 347, 28);
		getContentPane().add(idLabel);
		
		totalMarkersLabel = new JLabel("Total markers: ");
		totalMarkersLabel.setBounds(220, 38, 160, 28);
		getContentPane().add(totalMarkersLabel);
		
		nucleotideLabel = new JLabel("Nucleotides");
		nucleotideLabel.setHorizontalAlignment(SwingConstants.CENTER);
		nucleotideLabel.setBounds(220, 62, 100, 48);
		getContentPane().add(nucleotideLabel);
		
		homozygousNucleotideLabel = new JLabel("Homozygous");
		homozygousNucleotideLabel.setHorizontalAlignment(SwingConstants.CENTER);
		homozygousNucleotideLabel.setBounds(345, 62, 100, 48);
		getContentPane().add(homozygousNucleotideLabel);
		
		heterozygousNucleotideLabel = new JLabel("Heterozygous");
		heterozygousNucleotideLabel.setHorizontalAlignment(SwingConstants.CENTER);
		heterozygousNucleotideLabel.setBounds(467, 62, 100, 48);
		getContentPane().add(heterozygousNucleotideLabel);
		
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
					PopupMessage(E.toString());
					TPEDFile = null;
					fileLabel.setText("No TPED file.");
					System.out.println(E);
				}
			}
		});
		getContentPane().add(fileButton);
		
		
		
		
		//set up lists. 
		chromosomeList = new JList<String>(chromosomeListModel);
		chromosomeList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				try{
					//this block happens when a chromosome in the list is clicked. 
				}
				catch(Exception E) {
					
				}
			}
		});
		chromosomeListScroller = new JScrollPane(chromosomeList);
		chromosomeListScroller.setBounds(10, 109, 160, 422);
		getContentPane().add(chromosomeListScroller);
		
		nucleotideList = new JList<String>(nucleotideListModel);
		nucleotideListScroller = new JScrollPane(nucleotideList);
		nucleotideListScroller.setBounds(220, 110, 100, 422);
		getContentPane().add(nucleotideListScroller);
		
		homozygousNucleotideList = new JList<String>(homozygousNucleotideListModel);
		homozygousNucleotideListScroller = new JScrollPane(homozygousNucleotideList);
		homozygousNucleotideListScroller.setBounds(345, 110, 100, 422);
		getContentPane().add(homozygousNucleotideListScroller);
		
		heterozygousNucleotideList = new JList<String>(heterozygousNucleotideListModel);
		heterozygousNucleotideListScroller = new JScrollPane(heterozygousNucleotideList);
		heterozygousNucleotideListScroller.setBounds(467, 109, 100, 422);
		getContentPane().add(heterozygousNucleotideListScroller);
	}
		
		
}
