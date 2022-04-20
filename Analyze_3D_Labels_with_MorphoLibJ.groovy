#@ ImagePlus imp

import ij.IJ

// Swap T and Z dimensions if Z=1
dims = imp.getDimensions() // default order: XYCZT
if (dims[3] == 1) {
	imp.setDimensions( dims[2,4,3] )
}

IJ.run("Analyze Regions 3D", "centroid surface_area_method=[Crofton (13 dirs.)] euler_connectivity=6")
IJ.renameResults("Results")