# Cartilage_Toluidineblue

Script for semi-automated articular cartilage and growthplate analysis on Toluidine Blue Slides.

 Designed for mouse knee coronal section, but can be use in other animals (rat, guinea pig, rabbit....) whith image containing all medial OR lateral plateau (only one plateau in the image)
 See Sample image for example.
 Contain two automated parts (BT1 and BT2) separated by short manual processing on each image

 <b>Data collected :</b>
 - Articular cartilage plateau lenght in µm
 - Mean uncalcified articular cartilage thickness
 - Mean calcified articular cartilage thickness
 - Mean total articular cartilage thickness
 - Mean growth plate thickness (approximative)
 
<b> Must be adapted to your images:</b>
 - pixel size : see BT1.groovy
 - maybe Stain Vector : see BT1.groovy
 
 <b>IMPORTANT NOTE:</b> I also tried to retrieve information from proteoglycan loss with "color destained" using different cutoff value of stain intensity/RGB vectors but it was no successfull during validation on 20 slides stained in the same batch and with lot of normalization parameters, including vector intensity in the growthplate.
 Indeed, very small variation in slide thickness give too much variation in the color intensity. 

 **************************************************************************
 <b>Tutorial</b>
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
