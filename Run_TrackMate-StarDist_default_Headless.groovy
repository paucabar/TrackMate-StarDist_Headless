#@ ImagePlus imp
#@ File (style = "directory", label = "Output folder") outputFolder

import fiji.plugin.trackmate.Model
import fiji.plugin.trackmate.Settings
import fiji.plugin.trackmate.TrackMate
import fiji.plugin.trackmate.stardist.StarDistDetectorFactory
import fiji.plugin.trackmate.features.FeatureFilter
import fiji.plugin.trackmate.tracking.LAPUtils
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory
import fiji.plugin.trackmate.action.LabelImgExporter

int target_channel = 4 // 1-based (1 is the first channel)
int frameGap = 1
double linkingMax = 4
double closingMax = 4
int minDuration = 2

// Swap Z and T dimensions if T=1
dims = imp.getDimensions() // default order: XYCZT
if (dims[4] == 1) {
	imp.setDimensions( dims[2,4,3] )
}

// Setup settings for TrackMate
settings = new Settings(imp)

// Configure StarDist default detector
settings.detectorFactory = new StarDistDetectorFactory()
settings.detectorSettings['TARGET_CHANNEL'] = target_channel
println settings.detectorSettings

// Add ALL the feature analyzers known to TrackMate, via
// providers.
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

// 

path = new File(outputFolder, 'labels.tif').getAbsolutePath()
def impLabels = LabelImgExporter.createLabelImagePlus(trackmate, false, true)
//impLabels.show()
ij.IJ.save(impLabels, path)

////////////////////////////////////////////////////////////

// Try to print 10 spots and their features
int countdown = 10
for (def spot : model.getSpots().iterable(true)) {
    println(spot.echo())
    countdown--
  if (countdown == 0)
    break
}