#@ ImagePlus imp
#@ File (style = "file", label = "ilastik project") project

import ij.IJ

impName = imp.getTitle()
IJ.run(imp, "Run Pixel Classification Prediction", "projectfilename=$project inputimage=[$impName] pixelclassificationtype=Probabilities");
