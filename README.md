# ngff-transforms-prototype
A prototype implementation of NGFF transforms

## Examples

### Basic

This example contains one 3d MRI dataset with two transformations, one scaling, one affine, and rougly implements the [coordinate systems described in the slicer wiki.](https://www.slicer.org/wiki/Coordinate_systems#Introduction)

* Run [this code](https://github.com/bogovicj/ngff-transforms-prototype/blob/main/src/main/java/org/janelia/saalfeldlab/ngff/examples/BasicExample.java).
* See [story 1 and 3](https://github.com/ome/ngff/issues/84#issue-1116712463).

In addition to "array space", there are:

* "scanner" : physical space oriented with respect to the scanner
* "LPS" : anatomical physical space ("left - posterior - superior")

The associated metadata:
```json
{                                                                                                                                                                                                          
  "spaces": [
    {   
      "name": "scanner",
      "axes": [
        { "type": "space", "label": "x", "unit": "millimeter", "discrete": false },
        { "type": "space", "label": "y", "unit": "millimeter", "discrete": false },
        { "type": "space", "label": "z", "unit": "millimeter", "discrete": false }
      ]   
    },  
    {   
      "name": "LPS",
      "axes": [
        { "type": "space", "label": "LR", "unit": "millimeter", "discrete": false },
        { "type": "space", "label": "AP", "unit": "millimeter", "discrete": false },
        { "type": "space", "label": "IP", "unit": "millimeter", "discrete": false }
      ]   
    }   
  ],  
  "transformations": [
    {   
      "scale": [ 0.8, 0.8, 2.2 ],
      "type": "scale",
      "name": "to-mm",
      "input_space": "", 
      "output_space": "scanner"
    },  
    {   
      "affine": [ 0.9975, 0.0541, -0.0448, 0, -0.05185, 0.9974, 0.0507, 0, 0.04743, -0.04824, 0.99771, 0 ],
      "type": "affine",
      "name": "scanner-to-anatomical",
      "input_space": "scanner",
      "output_space": "LPS"
    }   
  ]
}

```

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
