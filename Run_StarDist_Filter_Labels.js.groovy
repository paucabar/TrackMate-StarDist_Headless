#@ ImagePlus imp

import ij.IJ
import ij.measure.ResultsTable

def listRemove = [5,21,55,56,57]

RT = new ResultsTable()
rt = RT.getResultsTable()

IJ.run(imp, "Analyze Regions", "area centroid")
//a = rt.getTitle()
//println a
IJ.renameResults("CountMasksofblobs-Morphometry", "Results");
int area = rt.getValue("Area", 1)
println area


IJ.run("Intensity Measurements 2D/3D", "input=blobs.gif labels=[Count Masks of blobs.gif] mean stddev")
IJ.run(imp, "Replace/Remove Label(s)", "label(s)=$listRemove final=0")
