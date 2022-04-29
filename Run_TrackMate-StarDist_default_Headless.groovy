#@ ImagePlus imp
#@ Integer (label="Target Channel", value=4, max=4, min=1, style="slider") targetChannel
#@ Integer (label="Max Frame Gap [frames]", value=1) frameGap
#@ Double (label="Linking Max Distance [calibrated]", value=4, persist=false) linkingMax
#@ Double (label="Gap Closing Max Distance [calibrated]", value=4, persist=false) closingMax
#@ Integer (label="Min Track Duration [rames]", value=2) minDuration
#@ File (style = "directory", label = "Output folder") outputFolder


import fiji.plugin.trackmate.Model
import fiji.plugin.trackmate.Settings
import fiji.plugin.trackmate.TrackMate
import fiji.plugin.trackmate.stardist.StarDistDetectorFactory
import fiji.plugin.trackmate.features.FeatureFilter
import fiji.plugin.trackmate.tracking.LAPUtils
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory
import fiji.plugin.trackmate.action.LabelImgExporter
import ij.IJ


//int target_channel = 4 // 1-based (1 is the first channel)
//int frameGap = 1
//double linkingMax = 4
//double closingMax = 4
//int minDuration = 2


// Swap Z and T dimensions if T=1
dims = imp.getDimensions() // default order: XYCZT
if (dims[4] == 1) {
	imp.setDimensions( dims[2,4,3] )
}

// Setup settings for TrackMate
settings = new Settings(imp)

// Configure StarDist default detector
settings.detectorFactory = new StarDistDetectorFactory()
settings.detectorSettings['TARGET_CHANNEL'] = targetChannel
println settings.detectorSettings

// add ALL the feature analyzers known to TrackMate, via providers
settings.addAllAnalyzers()

// Configure spot filter
filter1_spot = new FeatureFilter('AREA', 4.86, true)
filter2_spot = new FeatureFilter('MEAN_INTENSITY_CH3', 12.51, true) // green
settings.addSpotFilter(filter1_spot)
settings.addSpotFilter(filter2_spot)
println settings.spotFilters

// Configure tracker
settings.trackerFactory = new SparseLAPTrackerFactory()
settings.trackerSettings = LAPUtils.getDefaultLAPSettingsMap()
settings.trackerSettings['MAX_FRAME_GAP']  = frameGap
settings.trackerSettings['LINKING_MAX_DISTANCE']  = linkingMax
settings.trackerSettings['GAP_CLOSING_MAX_DISTANCE']  = closingMax
println settings.trackerSettings

// Configure track filter
filter1_track = new FeatureFilter('TRACK_DURATION', minDuration, true)
settings.addTrackFilter(filter1_track)
println settings.trackFilters

// Run TrackMate and store data into Model
model = new Model()
trackmate = new TrackMate(model, settings)

println trackmate.checkInput()
println trackmate.process()
println trackmate.getErrorMessage()

println model.getSpots().getNSpots(true)
println model.getTrackModel().nTracks(true)

// set the label image to display LUT properly
def impLabels = LabelImgExporter.createLabelImagePlus(trackmate, false, true)
setDisplayMinAndMax(impLabels)
impLabels.show()

// save label imge
path = new File(outputFolder, 'labels.tif').getAbsolutePath()
ij.IJ.save(impLabels, path)


def setDisplayMinAndMax(imageStack) {
	int nFrames = imageStack.getNFrames()
	println nFrames
	int maxStack = 0
	for (i in 1..nFrames) {
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

////////////////////////////////////////////////////////////

// Try to print 10 spots and their features
//int countdown = 10
//for (def spot : model.getSpots().iterable(true)) {
//    println(spot.echo())
//    countdown--
//  if (countdown == 0)
//    break
//}