#@ ImagePlus imp

import ij.IJ
import ij.plugin.frame.RoiManager
import ij.measure.ResultsTable

def roiDiscard = []// as int[]
println roiDiscard.getClass() 

RM = new RoiManager()
rm = RM.getRoiManager()
RT = new ResultsTable()
rt = RT.getResultsTable()


def roiList = rm.getRoisAsArray()
println roiList
int count = roiList.size()
println count

// set measurements ans generate results table
IJ.run("Set Measurements...", "area mean standard centroid display redirect=None decimal=2");
rm.runCommand(imp,"Deselect");
rm.runCommand(imp,"Measure");


for (int i in 0..count-1) {
	int area = rt.getValue("Area", i)
	//println area
	double mean = rt.getValue("Mean", i)
	if (area < 50 || area > 600 || mean < 152.45) {
		roiDiscard.add(i)
	}
}

println roiDiscard
def roiDiscardInt = roiDiscard.clone() as int[]

// delete ROIs

println roiDiscard.getClass() 
rm.setSelectedIndexes(roiDiscardInt)
rm.runCommand(imp,"Delete")
rm.runCommand(imp,"Deselect")

// ROIs to Label Image with BIOP
IJ.run(imp, "ROIs to Label image", "")
impLabel = IJ.getImage()

// Set Label Map with MorphoLibJ
IJ.run(impLabel, "Set Label Map", "colormap=[Golden angle] background=Black shuffle")
