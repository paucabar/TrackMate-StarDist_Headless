#@ ImagePlus imp

import ij.IJ
import ij.plugin.frame.RoiManager

impLabel = IJ.createImage("Labeling", "16-bit black", imp.getWidth(), imp.getHeight(), 1, imp.getNSlices(), imp.getNFrames())
rm = RoiManager.getInstance()
rm.getRoisAsArray().eachWithIndex { roi, index ->
    def pos = roi.getPosition()
    def ip = impLabel.getStack().getProcessor(pos)
    ip.setColor(index+1)
    ip.fill(roi)
}

impLabel.setDisplayRange(0, rm.getCount())
IJ.run(impLabel, "glasbey inverted", "")
impLabel.show()