#@ ImagePlus imp

// Swap Z and T dimensions if Z=1
dims = imp.getDimensions() // default order: XYCZT
if (dims[3] == 1) {
	imp.setDimensions( dims[2,4,3] )
}

import inra.ijpb.plugins.AnalyzeRegions3D

println imp.getClass()

// analyze 3D labels with MorphoLibJ
ar3D = new AnalyzeRegions3D()
def table = ar3D.process(imp)
table.show("Results")