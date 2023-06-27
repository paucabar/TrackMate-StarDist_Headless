/*
 * Use custom StarDist detector in TrackMate to generate 
 * 3D labels by tracking 2D labels. Perform 3D analysis
 * with MorphoLibJ
 * 
 * Author: Pau Carrillo Barberà
 * BIOTECMED, Universitat de València
 * IGC, University of Edinburgh
 * 
 * Last Modification: October 2022
 */

#@ ImagePlus imp
#@ UpdateService updateService
#@ UIService ui
#@ File(label="StarDist Model", style="open") model_file
#@ Integer (label="Target Channel [StarDist]", value=1, max=4, min=1, style="slider") targetChannel
#@ Integer (label="Measure Channel", value=2, max=4, min=1, style="slider") measureChannel
#@ Double (label="StarDist Score Threshold", value=0.3, min=0.0, max=1.0) scoreThr
#@ Double (label="StarDist Overlap Threshold", value=0.3, min=0.0, max=1.0) overlapThr 
#@ Double (label="MinSpotArea [calibrated]", value=5.0) minSpotArea
#@ Double (label="MaxSpotArea [calibrated]", value=200.0) maxSpotArea
//#@ Double (label="Min Intensity Mean", value=45.5) minIntensityMean
#@ Double (label="Overlap Tracker Scale Factor", value=1.0, min=0.0, max=1.0) scaleFactor
#@ Double (label="Overlap Tracker Min IoU", value=0.2, min=0.0, max=1.0) minIoU
#@ String (label="IoU Calculation", choices={"PRECISE", "FAST"}, style="radioButtonHorizontal") iou_calculation
#@ Integer (label="Min Track Duration [frames]", value=2) minDuration
#@ File (style = "directory", label = "Output folder") outputFolder

import fiji.plugin.trackmate.Model
import fiji.plugin.trackmate.Settings
import fiji.plugin.trackmate.TrackMate
import fiji.plugin.trackmate.stardist.StarDistCustomDetectorFactory
import fiji.plugin.trackmate.features.FeatureFilter
import fiji.plugin.trackmate.tracking.overlap.OverlapTrackerFactory
import fiji.plugin.trackmate.action.LabelImgExporter
import ij.ImagePlus
import ij.IJ
import inra.ijpb.plugins.AnalyzeRegions3D
import inra.ijpb.measure.IntensityMeasures
import ij.plugin.Duplicator
import ij.measure.ResultsTable
import ij.WindowManager
import ij.plugin.Duplicator

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

// Configure StarDist custom detector
settings.detectorFactory = new StarDistCustomDetectorFactory()
settings.detectorSettings['TARGET_CHANNEL'] = targetChannel
settings.detectorSettings['SCORE_THRESHOLD'] = scoreThr
settings.detectorSettings['OVERLAP_THRESHOLD'] = overlapThr
settings.detectorSettings['MODEL_FILEPATH'] = (String)model_file.getAbsolutePath()
println settings.detectorSettings

// add ALL the feature analyzers known to TrackMate, via providers
settings.addAllAnalyzers()

// Configure spot filter
//def spotFeatureMeanCh = "MEAN_INTENSITY_CH$measureChannel" as String
filter1_spot = new FeatureFilter('AREA', minSpotArea, true)
filter2_spot = new FeatureFilter('AREA', maxSpotArea, false)
//filter3_spot = new FeatureFilter(spotFeatureMeanCh, minIntensityMean, true)
settings.addSpotFilter(filter1_spot)
settings.addSpotFilter(filter2_spot)
//settings.addSpotFilter(filter3_spot)
println settings.spotFilters

// Configure tracker
settings.trackerFactory = new OverlapTrackerFactory()
//Uncoment to use default settings
//settings.trackerSettings = settings.trackerFactory.getDefaultSettings()
settings.trackerSettings['SCALE_FACTOR']  = scaleFactor
settings.trackerSettings['MIN_IOU']  = minIoU
settings.trackerSettings['IOU_CALCULATION']  = iou_calculation
println settings.trackerSettings

// Configure track filter
filter1_track = new FeatureFilter('TRACK_DURATION', minDuration, true)
settings.addTrackFilter(filter1_track)
println settings.trackFilters

// Run TrackMate and store data into Model
def model = new Model()
def trackmate = new TrackMate(model, settings)

println trackmate.checkInput()
println trackmate.process()
println trackmate.getErrorMessage()

println model.getSpots().getNSpots(true)
println model.getTrackModel().nTracks(true)

// create label image
boolean exportSpotsAsDots = false
boolean exportTracksOnly = true
boolean useSpotIDsAsLabels = false
ImagePlus impLabels = LabelImgExporter.createLabelImagePlus(trackmate, exportSpotsAsDots, exportTracksOnly, useSpotIDsAsLabels)

// set the label image to display LUT properly
setDisplayMinAndMax(impLabels)
//impLabels.show()

// Swap T and Z dimensions
dimLabels = impLabels.getDimensions()
impLabels.setDimensions(dimLabels[2,4,3])
dimLabels = impLabels.getDimensions()
imp.setDimensions( dims[2,4,3] )

// duplicate label image
def dup = new Duplicator()
def impLabelsDup = dup.run(impLabels, 1, dimLabels[2], 1, dimLabels[3], 1, dimLabels[4]);
setDisplayMinAndMax(impLabelsDup)
impLabelsDup.show()

// duplicate measure channel
def impMeasure = dup.run(imp, measureChannel, measureChannel, 1, dimLabels[3], 1, dimLabels[4]);
//impMeasure.show()

// save label imge
String title = imp.getShortTitle()
path = new File(outputFolder, "labels_${->title}.tif").getAbsolutePath()
ij.IJ.save(impLabelsDup, path)

// analyze 3D labels with MorphoLibJ
ar3D = new AnalyzeRegions3D()
def table = ar3D.process(impLabelsDup)
table.show("Results")

// intensity measures with MorphoLibJ
intMeas = new IntensityMeasures(impMeasure, impLabelsDup)
intTable = intMeas.getMean()
intTable.show("Intensity Results")

// save results table
pathResults = new File(outputFolder, "results_${->title}.csv").getAbsolutePath()
pathIntensityResults = new File(outputFolder, "results_intensity_${->title}.csv").getAbsolutePath()
table.saveAs(pathResults)
intTable.saveAs(pathIntensityResults)

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