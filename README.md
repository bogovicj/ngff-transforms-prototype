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

<details>
<summary><b>The associated metadata</b></summary>

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
</details>

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


<details>
<summary><b>The associated metadata</b></summary>
  
```json
{
  "spaces": [
    {
      "name": "um",
      "axes": [
        { "type": "space", "label": "y", "unit": "micrometer", "discrete": false },
        { "type": "space", "label": "z", "unit": "micrometer", "discrete": false }
      ]
    },
    {
      "name": "crop-offset",
      "axes": [
        { "type": "space", "label": "cj", "unit": "pixels", "discrete": false },
        { "type": "space", "label": "ci", "unit": "pixels", "discrete": false }
      ]
    },
    {
      "name": "crop-um",
      "axes": [
        { "type": "space", "label": "cy", "unit": "micrometer", "discrete": false },
        { "type": "space", "label": "cz", "unit": "micrometer", "discrete": false }
      ]
    }
  ],
  "transformations": [
    {
      "scale": [ 2.2, 1.1 ],
      "type": "scale",
      "name": "to-um",
      "input_space": "",
      "output_space": "um"
    },
    {
      "scale": [ 2.2, 1.1 ],
      "type": "scale",
      "name": "crop-to-um",
      "input_space": "crop-offset",
      "output_space": "crop-um"
    },
    {
      "translation": [ 10, 12 ],
      "type": "translation",
      "name": "offset",
      "input_space": "",
      "output_space": "crop-offset"
    }
  ]
}
```
  
</details>

### Multiscale

This example has two 2d multiscale-datasets.

* Run [this `MultiscaleExample` code](https://github.com/bogovicj/ngff-transforms-prototype/blob/main/src/main/java/org/janelia/saalfeldlab/ngff/examples/MultiscaleExample.java).
* See [story 8](https://github.com/ome/ngff/issues/84#issuecomment-1026844181).

The example code produces two multiscale groups:
* `/multiscales/sample` - downsampling performed by sampling
* `/multiscales/avg`  - downsampling performed by averaging

Both have a single "space" : 
* "physical" - the space in microns the full resolution and all downsampled versions relate to

<details>
<summary><b>The associated multiscale metadata in `/multiscales/avg`</b></summary>

```json
  {
  "multiscales": [
    {
      "version": "0.5-prototype",
      "name": "ms_avg",
      "type": "averaging",
      "metadata": null,
      "datasets": [
        {
          "path": "/multiscales/avg/s0",
          "coordinateTransformations": [
            {
              "scale": [ 2.2, 3.3 ],
              "type": "scale",
              "name": "s0-to-physical",
              "input_space": "",
              "output_space": "physical"
            }
          ]
        },
        {
          "path": "/multiscales/avg/s1",
          "coordinateTransformations": [
            {
              "transformations": [
                { "scale": [ 4.4, 6.6 ], "type": "scale" },
                { "translation": [ 1.1, 1.65 ], "type": "translation" }
              ],
              "type": "sequence",
              "name": "s1-to-physical",
              "input_space": "",
              "output_space": "physical"
            }
          ]
        },
        {
          "path": "/multiscales/avg/s2",
          "coordinateTransformations": [
            {
              "transformations": [
                { "scale": [ 8.8, 13.2 ], "type": "scale" },
                { "translation": [ 3.3, 4.95 ], "type": "translation" }
              ],
              "type": "sequence",
              "name": "s2-to-physical",
              "input_space": "",
              "output_space": "physical"
            }
          ]
        }
      ],
      "spaces": [
        {
          "name": "physical",
          "axes": [
            { "type": "space", "label": "x", "unit": "um", "discrete": false },
            { "type": "space", "label": "y", "unit": "um", "discrete": false }
          ]
        }
      ]
    }
  ]
}
```
</details>
  
<details>
<summary><b>The single-scale metadata in `/multiscales/avg/s2`</b></summary>

```json
{
  "spaces": [
    {
      "name": "physical",
      "axes": [
        { "type": "space", "label": "x", "unit": "um", "discrete": false },
        { "type": "space", "label": "y", "unit": "um", "discrete": false }
      ]
    }
  ],
  "coordinateTransformations": [
    {
      "transformations": [
        { "scale": [ 8.8, 13.2 ], "type": "scale" },
        { "translation": [ 3.3, 4.95 ], "type": "translation", }
      ],
      "type": "sequence",
      "name": "s2-to-physical",
      "input_space": "",
      "output_space": "physical"
    }
  ]
}
```
</details>
