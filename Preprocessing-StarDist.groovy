#@ ImagePlus imp

import ij.IJ
import ij.plugin.Duplicator

// Swap Z and T dimensions if T=1
dims = imp.getDimensions() // default order: XYCZT
if (dims[4] == 1) {
	imp.setDimensions( dims[2,4,3] )
}

// duplicate nuclei and marker channels
// 1-based (1 is the first channel/slice/frame)
// (imp, ch0, chN, sl0, slN, fr0, frN)
impNuclei = new Duplicator().run(imp, 4, 4, 1, 1, 1, 7)
impMarker = new Duplicator().run(imp, 1, 1, 1, 1, 1, 7)

// run StarDist
IJ.run(impNuclei, "StarDist 2D", "input=$impNuclei modelchoice=[Versatile (fluorescent nuclei)] normalizeinput=true percentilebottom=1.0 percentiletop=99.8 probthresh=0.5 nmsthresh=0.4 outputtype=[ROI Manager] ntiles=1 excludeboundary=2 roiposition=Automatic verbose=false showcsbdeepprogress=false showprobanddist=false")