#@ ImagePlus imp

import ij.IJ
//import ij.gui.Roi
import ij.plugin.frame.RoiManager
import ij.measure.ResultsTable

def roiDiscard = []
//roiDiscard.add(2)

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


for (i=0; i<count; i++) {
	int area = rt.getValue("Area", i)
	//println area
	double mean = rt.getValue("Mean", i)
	if (area < 50 || area > 600 || mean < 152.45) {
		roiDiscard.add(i);
	}
}

println roiDiscard

// delete ROIs

//rm.select(1);
rm.setSelectedIndexes(roiDiscard)
rm.runCommand(imp,"Delete");
