#@ ImagePlus imp
#@ File (style = "directory", label = "Output folder") outputFolder

import ij.ImagePlus
import ij.IJ
import fiji.plugin.trackmate.Model
import fiji.plugin.trackmate.Settings
import fiji.plugin.trackmate.TrackMate
import fiji.plugin.trackmate.detection.LabelImageDetectorFactory
import fiji.plugin.trackmate.features.FeatureFilter
import fiji.plugin.trackmate.tracking.jaqaman.LAPUtils
import fiji.plugin.trackmate.tracking.jaqaman.SparseLAPTrackerFactory
import fiji.plugin.trackmate.action.LabelImgExporter

int target_channel = 1 // 1-based (1 is the first channel)
int frameGap = 1
double linkingMax = 4
double closingMax = 4
int minDuration = 2

def setDisplayMinAndMax(imageStack) {
	int nFrames = imageStack.getNFrames()
	int nSlices = imageStack.getNSlices()
	int nImages = nFrames * nSlices
	println "$nFrames frames x $nSlices slices = $nImages images"
	int maxStack = 0
	for (i in 1..nImages) {
		def ip = imageStack.getStack().getProcessor(i)
		maxImage = ip.getStats().max as int
		if (maxImage > maxStack) {
			maxStack = maxImage
		}
	}
	println "Set display 0 - $maxStack"
	
	imageStack.setDisplayRange(0, maxStack)
	IJ.run(imageStack, "glasbey inverted", "")
}

// Swap Z and T dimensions if T=1
dims = imp.getDimensions() // default order: XYCZT
if (dims[4] == 1) {
	imp.setDimensions( dims[2,4,3] )
}

// Setup settings for TrackMate
settings = new Settings(imp)

// Configure StarDist default detector
settings.detectorFactory = new LabelImageDetectorFactory()
settings.detectorSettings['TARGET_CHANNEL'] = target_channel
settings.detectorSettings['SIMPLIFY_CONTOURS'] = true
println settings.detectorSettings

/*
// Configure spot filter
filter1_spot = new FeatureFilter('AREA', 4.86, true)
filter2_spot = new FeatureFilter('MEAN_INTENSITY_CH3', 12.51, true) // green
settings.addSpotFilter(filter1_spot)
settings.addSpotFilter(filter2_spot)
println settings.spotFilters
*/

// Configure tracker
settings.trackerFactory = new SparseLAPTrackerFactory()
settings.trackerSettings = settings.trackerFactory.getDefaultSettings()
settings.trackerSettings['MAX_FRAME_GAP']  = frameGap
settings.trackerSettings['LINKING_MAX_DISTANCE']  = linkingMax
settings.trackerSettings['GAP_CLOSING_MAX_DISTANCE']  = closingMax
println settings.trackerSettings

/*
// Configure track filter
filter1_track = new FeatureFilter('TRACK_DURATION', minDuration, true)
settings.addTrackFilter(filter1_track)
println settings.trackFilters
*/

// Run TrackMate and store data into Model
model = new Model()
trackmate = new TrackMate(model, settings)

println trackmate.checkInput()
println trackmate.process()
println trackmate.getErrorMessage()

println model.getSpots().getNSpots(true)
println model.getTrackModel().nTracks(true)

// 

path = new File(outputFolder, 'labels.tif').getAbsolutePath()
boolean exportSpotsAsDots = false
boolean exportTracksOnly = true
boolean useSpotIDsAsLabels = false
ImagePlus impLabels = LabelImgExporter.createLabelImagePlus(trackmate, exportSpotsAsDots, exportTracksOnly, useSpotIDsAsLabels)
setDisplayMinAndMax(impLabels)
impLabels.show()
ij.IJ.save(impLabels, path)