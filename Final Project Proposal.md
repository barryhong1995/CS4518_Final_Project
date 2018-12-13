# GROUP PROJECT PROPOSAL
#### GROUP MEMBERS
- Craig Bursey
- Hung Hong
- Hannah Jauris

### Problem Statement (What we plan to do)
Our project will perform image classification using the Tensorflow InceptionV3 model to identify several of the most prominent features in an image, rather than just one. The users of the app would take pictures with their phone camera, which are then processed by the deep inference model and stored on a database, along with their top image classifications, for later viewing. When the user views their images, they will be able to select one image and view others similar to that one, based on how the deep inference model classified it.

### Proposed Solution (How we plan to do it)
Our app will have a UI designed to give the user functionality to both take images and view images. To take images, our app will access the camera using implicit intents to allow the user to take images which will then be shown in our application. When the user takes a picture, it will be run through the Tensorflow InceptionV3 model to identify the major components in the image. The user will be able to choose if this inference occurs on-device or on the cloud. The user will then be able to choose to save their image to a database, where it can be viewed later. 

When the user later wants to view images, they will be provided with a list of all of the images on the database so far. If there is one particular image the user likes, they can select an option to view more images similar to that image. The database would again be queried to receive only images classified as having similar components as that image.

For identifying multiple elements from an image, we are considering two different approaches:
-	Analyze the image as a whole and use the top three inference results to classify different elements in the image, but would not provide a way to determine features such as the general location of each element.
-	Divide the image into equal parts and analyze each part to classify different elements at each part. This could potentially provide more accuracy if the model can identify objects potentially cut-off by the image division.
