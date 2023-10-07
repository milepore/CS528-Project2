# CS528-Project2
Project Group 
Yang, Zhiwei; Lepore, Michael; Liu, Yuyuan; Wu, Kangjian; Yu, Bo

Things to do:

[x] Handle Rotation Properly(Lepore, Michael)
* Update CrimeDetailFragment::processImage to create the bitmap already rotated - see BitmapUtils for info

[] Pick different kinds of mlkit processing(Liu, Yuyuan)
* Add check boxes for each kind of ML processing
* Make sure that the various checks only allow a single box to be selected
* Update CrimeDetailFragment::setupProcessor to create the right processor (and if not facial detection, blank out the text)

[x] Support more than a single image(Wu, Kangjian)
* Update the record to have 4 images and an image count
* Probably need to update the version of the DB to v4 and add a migration going from v3->v4
* Update CrimeDetailFragment to have an array of images and an array of GraphicOverlays
* Update CrimeDetailFragment::processImage to use the right image and graphic overlay based on the index

[x] Face Contour Processor Setup(Lepore, Michael)
* Update the face detector processor with the options to do contour detection only if the box is checked

[x] Mesh Detection Processing(Yu, Bo)
* Create processor and google.mlkit.vision.demo.kotlin classes

[] Selfie segmentation processing(Yang, Zhiwei)
* Create processor and google.mlkit.vision.demo.kotlin classes

[] Refactor code to single package (Lepore, Michael)
* Get rid of any of the mlkit we aren't using
