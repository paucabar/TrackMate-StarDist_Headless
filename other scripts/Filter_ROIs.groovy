#@ ImagePlus imp

import ij.IJ
import ij.plugin.frame.RoiManager
import ij.measure.ResultsTable

// define empty list to fill with ROIs to be discarded
def roiDiscard = []
println roiDiscard.getClass() 

RM = new RoiManager()
rm = RM.getRoiManager()
RT = new ResultsTable()
rt = RT.getResultsTable()

// get ROIs as array and cout ROIs
def roiList = rm.getRoisAsArray()
int count = rm.getCount()
println roiList
println count

// set measurements ans generate results table
IJ.run("Set Measurements...", "area mean standard centroid display redirect=None decimal=2");
rm.runCommand(imp,"Deselect");
rm.runCommand(imp,"Measure");

// get values from the result table and store the index of ROIs that fail to meet one or more criteria
for (int i in 0..count-1) {
	int area = rt.getValue("Area", i)
	double mean = rt.getValue("Mean", i)
	if (area < 50 || area > 600 || mean < 152.45) {
		roiDiscard.add(i)
	}
}

// copy the roiDiscard list as a list of integers
println roiDiscard
def roiDiscardInt = roiDiscard as int[]

// delete ROIs
println roiDiscard.getClass() 
rm.setSelectedIndexes(roiDiscardInt)
rm.runCommand(imp,"Delete")

// close results table
IJ.selectWindow("Results")
IJ.run("Close")

// ROIs to Label Image with BIOP
//IJ.run(imp, "ROIs to Label image", "")
//impLabel = IJ.getImage()

// Set Label Map with MorphoLibJ
//IJ.run(impLabel, "Set Label Map", "colormap=[Golden angle] background=Black shuffle")
