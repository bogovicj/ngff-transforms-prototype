# ngff-transforms-prototype
A prototype implementation of NGFF transforms

## Examples

### Crop

This example has two 2d datasets, one of which is a cropped version (strict subset) of the other. 

* Run [this code](https://github.com/bogovicj/ngff-transforms-prototype/blob/main/src/main/java/org/janelia/saalfeldlab/ngff/examples/CropExample.java).
* See [story 2](https://github.com/ome/ngff/issues/84#issue-1116712463).

In addition to "array space", there are:
* "um" : physical space of complete image
* "crop-offset" : the crop transformed to the whole in pixel units
* "crop-um" : the crop  transformed to the the whole in physical units

Editing [these lines](https://github.com/bogovicj/ngff-transforms-prototype/blob/main/src/main/java/org/janelia/saalfeldlab/ngff/examples/CropExample.java#L67-L70) enables changing how the volumes are displayed. To view both volumes overlayed in pixel units:

```java
final String imgSpace = "";
final String cropSpace = "crop-offset";
show( zarr, imgSpace, cropSpace );
```

To view both volumes overlayed in physical (um) units:
```java
final String imgSpace = "um";
final String cropSpace = "crop-um";
show( zarr, imgSpace, cropSpace );
```
