import qupath.lib.objects.PathAnnotationObject
import qupath.lib.gui.tools.MeasurementExporter
//Save all results in a .tsv file
 def outputPath = buildFilePath(PROJECT_BASE_DIR, "results")
 mkdirs(outputPath)

// Get the list of all images in the current project
def project = getProject()
def imagesToExport = project.getImageList()

// Separate each measurement value in the output file with a tab ("\t")
def separator = "\t"

// Choose the columns that will be included in the export - all if empty
def columnsToInclude = new String[]{}

// Choose the type of objects that the export will process
def exportType = PathAnnotationObject.class

// Choose your *full* output path:
name = "name" + ".tsv"
def outputPath2 = buildFilePath(outputPath, name)
def outputFile = new File(outputPath2)

// Create the measurementExporter and start the export
def exporter  = new MeasurementExporter()
                  .imageList(imagesToExport)            // Images from which measurements will be exported
                  .separator(separator)                 // Character that separates values
                  .includeOnlyColumns(columnsToInclude) // Columns are case-sensitive
                  .exportType(exportType)               // Type of objects to export
                  .exportMeasurements(outputFile)        // Start the export process

print "Done!"