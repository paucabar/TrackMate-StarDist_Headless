#@ ImagePlus imp

import ij.IJ
import ij.plugin.frame.RoiManager


RM = new RoiManager()
rm = RM.getRoiManager()
def list = [2, 5, 7, 9] as int[]

def roiList = rm.getRoisAsArray()
int count = roiList.size()
println count
rm.setSelectedIndexes(list)
rm.runCommand(imp,"Delete")

roiList = rm.getRoisAsArray()
count = roiList.size()
println count