#@ ImagePlus imp

import ij.IJ
//import ij.gui.Roi
import ij.plugin.frame.RoiManager
import ij.measure.ResultsTable

roiDiscard = []
//roiDiscard.add(2)

RM = new RoiManager()
rm = RM.getRoiManager()

def roiList = rm.getRoisAsArray()
println roiList
int count = roiList.size()
println count

IJ.run("Set Measurements...", "area mean standard centroid display redirect=None decimal=2");
rm.runCommand(imp,"Deselect");
rm.runCommand(imp,"Measure");

