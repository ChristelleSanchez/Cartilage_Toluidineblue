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

//keep common part to "CartilageArea" and "AreaGrowthPlate" and create "GrowthPlateArea" annotation
import qupath.lib.roi.RoiTools
import qupath.lib.objects.PathAnnotationObject
import qupath.lib.common.GeneralTools
import org.locationtech.jts.geom.Geometry
import qupath.lib.objects.PathObject
import qupath.lib.objects.PathObjects
import qupath.lib.roi.GeometryTools
import static qupath.lib.gui.scripting.QPEx.*


def AreaGP = getAnnotationObjects().find {it.getPathClass() == getPathClass("AreaGrowthPlate")}
def cart2 = getAnnotationObjects().find {it.getPathClass() == getPathClass("CartilageArea")}
def plane2 = cart2.getROI().getImagePlane()
if (plane2 != AreaGP.getROI().getImagePlane()) {
    println 'Annotations are on different planes!'
    return    
}
def a1 = AreaGP.getROI().getGeometry()
def a3 = cart2.getROI().getGeometry()
def commonA = a1.intersection(a3)
def roiA = GeometryTools.geometryToROI(commonA, plane2)
def GPA = PathObjects.createAnnotationObject(roiA, getPathClass('GrowthPlateArea'))
    addObject(GPA)

//calculate circle diameter to extrapolate lenght, add to table
def server = getCurrentServer()
def cal = server.getPixelCalibration()
double pixelWidth = cal.getPixelWidthMicrons()
double pixelHeight = cal.getPixelHeightMicrons()
def a4 = AreaGP.getROI().getScaledArea(pixelWidth, pixelHeight)
def s = Math.sqrt(a4)
def d11 = s*2
def d = d11/3.1415
def a5 = GPA.getROI().getScaledArea(pixelWidth, pixelHeight)
def dd = a5/d
def GP1 = getAnnotationObjects().find {it.getPathClass() == getPathClass("GrowthPlateArea")}
GP1.getMeasurementList().addMeasurement("GP Length µm", d)
GP1.getMeasurementList().addMeasurement("GP thickness µm", dd)


//set difference between plateau (PlateArea) and UncalcifiedArea and create "CalcifiedArea" annotation
def tissue = getAnnotationObjects().find {it.getPathClass() == getPathClass("PlateArea") }
if (tissue == null)
    println "No plate area"
else {
def calc = getAnnotationObjects().find {it.getPathClass() == getPathClass("UncalcifiedArea")}
def plane1 = tissue.getROI().getImagePlane()
if (plane1 != calc.getROI().getImagePlane()) {
    println 'Annotations are on different planes!'
    return    
}
def g1 = tissue.getROI().getGeometry()
def g2 = calc.getROI().getGeometry()

def difference = g1.difference(g2)
if (difference.isEmpty())
    println "No intersection between areas"
else {
    def roidiff = GeometryTools.geometryToROI(difference, plane1)
    def annot = PathObjects.createAnnotationObject(roidiff, getPathClass('CalcifiedArea'))
    addObject(annot)
     }

//standard plateau lenght according to line
//select annotations class "Plate" dans geometry
def platearea = getAnnotationObjects().find {it.getPathClass() == getPathClass("PlateArea")};
def ca = getAnnotationObjects().find {it.getPathClass() == getPathClass("CalcifiedArea")};
//def server = getCurrentServer()
//def cal = server.getPixelCalibration()
//double pixelWidth = cal.getPixelWidthMicrons()
//double pixelHeight = cal.getPixelHeightMicrons()
def p5 = platearea.getROI().getScaledArea(pixelWidth, pixelHeight);
def p6 = calc.getROI().getScaledArea(pixelWidth, pixelHeight);
def p7 = ca.getROI().getScaledArea(pixelWidth, pixelHeight);
//select annotations class "Plate" dans line
def plateline = getAnnotationObjects().find {it.getPathClass() == getPathClass("Plate") && it.getROI() instanceof qupath.lib.roi.LineROI}
def p4 = plateline.getROI().getLength()
//Scaled in µM with pixelSize is 0.72
//def line = p4*0.72
//Scaled auto with values encoded
def line = p4*pixelWidth
def normalizedPlate = p5/line
def normalizeduncal = p6/line
def normalizedcal = p7/line
//add normalizedvalue in a new column for annotation "Plate"
platearea.getMeasurementList().addMeasurement("Mean total articular cartilage thickness", normalizedPlate)
calc.getMeasurementList().addMeasurement("Mean uncalcified articular cartilage thickness", normalizeduncal)
ca.getMeasurementList().addMeasurement("Mean calcified articular cartilage thickness", normalizedcal)
//add line in a new column for all annotation with the size of the plate
def annotations = getAnnotationObjects()
annotations.each{ annotation -> {
      annotation.getMeasurementList().addMeasurement("Plateau lenght µm", line)
    }
}
}
//Delete unused annotations before saving  
def removal2 = getAnnotationObjects().findAll{it.getPathClass() == getPathClass("CartilageArea")}
removeObjects(removal2, true) 
def removal3 = getAnnotationObjects().findAll{it.getPathClass() == getPathClass("Plate")}
removeObjects(removal3, true) 

//Save all before running save.groovy!!!!