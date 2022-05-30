#@ ImagePlus imp

import ij.IJ
import ij.plugin.frame.RoiManager

impLabel = IJ.createImage("Labeling", "16-bit black", imp.getWidth(), imp.getHeight(), 1)
ip = impLabel.getProcessor()
rm = RoiManager.getInstance()

rm.getRoisAsArray().eachWithIndex { roi, index ->
	ip.setColor(index+1)
	ip.fill(roi)
}

ip.resetMinAndMax()
IJ.run(impLabel, "glasbey inverted", "")
impLabel.show()