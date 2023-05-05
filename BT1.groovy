/*
April 2023, Qupath version  0.3.2
BSD 3-Clause License
@author Christelle Sanchez
 
 contact: Christelle.sanchez@uliege.be
 University of Liege - Belgium
 **************************************************************************
 Script for semi-automated articular cartilage and growthplate analysis on Toluidine Blue Slides
 Designed for mouse knee coronal section, whith image of medial OR lateral plateau (only one plateau in the image)
 Contain two automated parts (BT1 and BT2) separated by short manual processing on each image

 Data collected :
 - Articular cartilage plateau lenght in µm
 - Mean uncalcified articular cartilage thickness
 - Mean calcified articular cartilage thickness
 - Mean total articular cartilage thickness
 - Mean growth plate thickness (approximative)

 **************************************************************************
 Tutorial
1)	Capture images of the stained slides. Parameters here used Zeiss axio lab.A1 microscope with Axiocam 305 color camera and ZEN software, magnification 10x in .TIF or .PNG. Blank balance adjusted on the first slide, then keep the same parameters, with the plateau as horizontal as possible. All articular cartilage (medial or lateral) must be present in the image, as well as the growth plate. 
2)	Create an empty folder somewhere
3)	Open Qupath, File -> Project -> Create project, and select this folder 
4)	Import images in the project : File -> Project -> Add images 
5)	Delete all unusable images (folded, broken, pleated, really too dark or too light)
6)	Save (File -> save) and close Qupath
7)	Go to the folder, delete the « classifiers » folder in it, and replace it by « classifiers » folder provided here. Also add the folder « scripts » containing the scripts 
8)	Open againg Qupath and the project, without opening any images
9)	Open BT1.groovy script (Automate -> Show script editor -> open)
10)	Apply BT1 to all images in the project (Script editor -> Run -> Run for project)
11)	Save (File -> save)
12) Do manual part:
Open each images one by one to do the manual part :
In Annotations, delete all annotations of GEOMETRY Class « Cartilage » other than  plateau, condyle (i.e : delete meniscus or the growthplate if annoted)
-For each GEOMETRY Cartilage, set class "PlateArea" for the plateau and "Condyle" for the condyle (only one of each maximum) 
-Class « CartilageArea »: keep one if many, corresponding to the growth plate
- If there is less than 5% remaining uncalcified cartilage, delete any existing « PlateArea »
   -	If there is a PlateArea, select it and do « Unlock »
        Modify it with Wand and/or Brush tools: 
1.	Correct quickly the area to remove any meniscus, ligament, osteophyte
2.	Then zoom and correct more precisely – pay attention to add any too destained area/ chondrocytes cytoplasm that would be not present in the geometry. 
3.	Object ->annotations->duplicate annotation
4.	Set class « UncalcifiedArea » for the duplicate and remove the calcified area from it, following the tidemark
5.	Clic on « Line » tool to draw a line to measure the plateau length, then set it class « Plate »

-Select the ellipse "AreaGrowthPlate" and place it in the correct area, without modifying the size : at both intersections, the growth plate must be in the middle of the circle, below the articular cartilage. 
-Adjust if needed the CartilageArea annotation with correspond to the growth plate in the circle (add missing chondrocyte cytoplasm or hole, correct the bone marrow interface
-Open the next image and save the project.
When all images are processed, save and close Qupath, then open again and run the script BT2.groovy Run for project, save then run save.groovy to collect all data in .tsv file. 

 **************************************************************************
 */
//
//request preliminar classes.json - included in the folder - containing: "Cartilage", "Plate", "Condyle", "GrowthPlate", "AreaGrowthPlate", "CartilageArea", "UncalcifiedArea", 'GrowthPlateArea","CalcifiedArea"
//part1 "run for project"
//set of image type 
setImageType('BRIGHTFIELD_OTHER');
//setColorDeconvolution : depend on the stain!!! try "preprocessing" -> "estimate stain vector" to adapt the value, depend on the camera and light
setColorDeconvolutionStains('{"Name" : "Toluidine Blue-mean", "Stain 1" : "Cartilage", "Values 1" : "0.48052 0.83621 0.2643", "Stain 2" : "Bone", "Values 2" : "0.86784 0.46844 0.16555", "Background" : " 233 233 233"}');
//set of pixel size to obtain µm extrapolation (depend on your image file - to be adapted)
setPixelSizeMicrons(0.720000, 0.720000);
//create ellipses for AreaGrowthPlate
import qupath.lib.objects.PathObjects
import qupath.lib.roi.ROIs
import qupath.lib.regions.ImagePlane
//add a circle in the growth plate area ROI, 1100px diameter and attribute the class AreaGrowthPlate to it
int z = 0
int t = 0
def plane = ImagePlane.getPlane(z, t)
def roi = ROIs.createEllipseROI(730, 550, 800, 800, plane)
def sb = PathObjects.createAnnotationObject(roi, getPathClass('AreaGrowthPlate'))
addObject(sb)
//create main annotations derived from cartilage BT segment
resetSelection();
createAnnotationsFromPixelClassifier("CartilageBT", 50000.0, 5.0, "SPLIT")
//create annotations CartilageArea for growthplate
resetSelection();
createAnnotationsFromPixelClassifier("CartilageAreaBT", 100000.0, 1000.0, "SPLIT")
//end of part 1
