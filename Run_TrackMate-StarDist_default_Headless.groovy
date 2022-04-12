#@ ImagePlus imp

import fiji.plugin.trackmate.Model
import fiji.plugin.trackmate.Settings
import fiji.plugin.trackmate.TrackMate
import fiji.plugin.trackmate.stardist.StarDistDetectorFactory
import fiji.plugin.trackmate.features.FeatureFilter
import fiji.plugin.trackmate.tracking.LAPUtils
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory
import fiji.plugin.trackmate.action.LabelImgExporter

int target_channel = 1 // starts from 1
int frameGap = 1
int linkingMax = 4
int closingMax = 4
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

// Configure spot filter
filter1_spot = new FeatureFilter('AREA', 15.84, true)
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