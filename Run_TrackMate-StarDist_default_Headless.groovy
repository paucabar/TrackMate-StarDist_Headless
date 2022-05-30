/*
 * Use default StarDist detector in TrackMate to generate 
 * 3D labels by tracking 2D labels. Perform 3D analysis
 * with MorphoLibJ
 * 
 * Author: Pau Carrillo Barberà
 * BIOTECMED, Universitat de València
 * IGC, University of Edinburgh
 * 
 * Last Modification: May 2022
 */

#@ ImagePlus imp
#@ UpdateService updateService
#@ UIService ui
#@ Integer (label="Target Channel [StarDist]", value=4, max=4, min=1, style="slider") targetChannel
#@ Integer (label="Measure Channel", value=3, max=4, min=1, style="slider") measureChannel
#@ Double (label="MinSpotArea [calibrated]", value=20.0) minSpotArea
#@ Double (label="MaxSpotArea [calibrated]", value=130.0) maxSpotArea
#@ Double (label="Mean Intensity Mean", value=45.5) minIntensityMean
#@ Integer (label="Max Frame Gap [frames]", value=1) frameGap
#@ Double (label="Linking Max Distance [calibrated]", value=4, persist=false) linkingMax
#@ Double (label="Gap Closing Max Distance [calibrated]", value=4, persist=false) closingMax
#@ Integer (label="Min Track Duration [frames]", value=2) minDuration
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
import inra.ijpb.plugins.AnalyzeRegions3D
import ij.plugin.Duplicator
import ij.measure.ResultsTable

// check update sites
boolean checkStarDist = isUpdateSiteActive("StarDist");
boolean checkCSBDeep = isUpdateSiteActive("CSBDeep");
boolean checkTM_SD = isUpdateSiteActive("TrackMate-StarDist");
boolean checkMorphoLibJ = isUpdateSiteActive("IJPB-plugins");

// exit if any update site is missing
boolean checkAll = checkStarDist & checkCSBDeep & checkTM_SD & checkMorphoLibJ
if (!checkAll) {
	return
}

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
def spotFeatureMeanCh = "MEAN_INTENSITY_CH$measureChannel" as String
filter1_spot = new FeatureFilter('AREA', minSpotArea, true)
filter2_spot = new FeatureFilter('AREA', maxSpotArea, false)
filter3_spot = new FeatureFilter(spotFeatureMeanCh, minIntensityMean, true)
settings.addSpotFilter(filter1_spot)
settings.addSpotFilter(filter2_spot)
settings.addSpotFilter(filter3_spot)
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
//impLabels.show()

// Swap T and Z dimensions
dimLabels = impLabels.getDimensions()
impLabels.setDimensions(dimLabels[2,4,3])
dimLabels = impLabels.getDimensions()

// duplicate label image
def dup = new Duplicator()
def impLabelsDup = dup.run(impLabels, 1, dimLabels[2], 1, dimLabels[3], 1, dimLabels[4]);
setDisplayMinAndMax(impLabelsDup)
impLabelsDup.show()

// save label imge
String title = imp.getShortTitle()
path = new File(outputFolder, "labels_${->title}.tif").getAbsolutePath()
ij.IJ.save(impLabelsDup, path)

// analyze 3D labels with MorphoLibJ
ar3D = new AnalyzeRegions3D()
def table = ar3D.process(impLabelsDup)
table.show("Results")

// save results table
pathResults = new File(outputFolder, "results_${->title}.csv").getAbsolutePath()
table.saveAs(pathResults)

def isUpdateSiteActive (updateSite) {
	checkUpdate = true
	if (! updateService.getUpdateSite(updateSite).isActive()) {
    	ui.showDialog "Please activate the $updateSite update site"
    	checkUpdate = false
	}
	return checkUpdate
}

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