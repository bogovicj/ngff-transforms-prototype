First, sorry for not replying sooner, I wanted to have more done on the transform spec before replying so I'd
have something concrete to share and suggest.

## Array metadata and duplication

I agree with @d-v-b and feel strongly that spatial metadata should (must!) be stored with their respective arrays.

Re suggestion 2: ("un-consolidating" the spatial metadata at the group level). I don't have strong opinions
here, and @constantinpape 's point:

> tools currently supporting ome.zarr are built with "consolidated" metadata at the group level in mind, 

is a fine reason to not do it. I suppose my vote is to keep the status quo there, but keeping an eye out
for any pain points caused by the duplication of metadata.


@jbms says:

> In general it just seems more natural to me to attach coordinate transformations to a named "view", rather
> than to the array itself, since we can have arbitrarily many views but could attach just a single coordinate
> transformation to the array itself and then we also have the potential ambiguity of whether we want to refer
> to the "raw" array or the transformed array.

I agree with this, and as you say, my hope for "spaces" is to address this.  Specifically, every "space" is a
"view" on an array, and can be referenced separately, as they all have (unique) names. To steal one of your
examples, if we have an array with a "native resolution", but is also aligned to an atlas we'd have the spaces:

<details>

<summary>example metadata</summary>

```json
"spaces" : [
    {
        "name" : "/my/array",
        "axes" : [
            {"name" : "dim_0", "type" : "array" },
            {"name" : "dim_1", "type" : "array" }
        ]
    },
    {
        "name" : "physical",
        "axes" : [
            {"name" : "x", "type" : "space", "unit" : "nanometers" },
            {"name" : "y", "type" : "space", "unit" : "nanometers" }
        ]
    },
    {
        "name" : "atlas",
        "axes" : [
            {"name" : "x", "type" : "space", "unit" : "nanometers" },
            {"name" : "y", "type" : "space", "unit" : "nanometers" }
        ]
    }
],
"coordinateTransformations" : 
[
    {
        "type" : "scale",
        "scale" : [ 3, 3, 12 ]
        "inputSpace" : "/my/array",
        "outputSpace" : "physical",
    },
    {
        "type" : "affine",
        "affine" : [ 1.1, 0.1, 0.02 -20.1, 0.12, 0.8, 0.09, 5.1, 0.01 0.2, 0.75, 0.1 ]
        "inputSpace" : "physical",
        "outputSpace" : "atlas",
    },
]
```

</details>

Given these spaces (and the transformations between them), any consuming software can access the array's "coordinate system", 
or in the data's physical space. The default "view" gives you the "raw" array in discrete, unitless
coordinates.



## Discrete vs continuous

> To me it seems most natural to represent that by specifying the downsample factors 
> and offsets relative to the base resolution, i.e. in terms of base resolution voxels
> rather than some physical coordinate space, but I can see advantages to both approaches.

I agree that both are useful, and we should make both possible. Here's how I envision


<details>

<summary>relative to the base array</summary>

```json
{
    "multiscales": [
        {
            "version": "0.4",
            "coordinateSystems" : [
                {
                    "name" : "world",
                    "axes": [
                        {"name": "z", "type": "space", "unit": "micrometer"},
                        {"name": "y", "type": "space", "unit": "micrometer"},
                        {"name": "x", "type": "space", "unit": "micrometer"}
                    ]
                }
            ],
            "datasets": [
                {
                    "path": "0",
                    "coordinateTransformations": [{"type": "scale", "scale": [0.15, 0.15, 5.0], "inputSpace" : "0", "outputSpace" : "world"}]
                },
                {
                    "path": "1",
                    "coordinateTransformations": [{"type": "scale", "scale": [2.0, 2.0, 2.0], "inputSpace" : "1", "outputSpace" : "0"}]
                },
                {
                    "path": "2",
                    "coordinateTransformations": [{"type": "scale", "scale": [4.0, 4.0, 4.0], "inputSpace" : "2", "outputSpace" : "0"}]
                }
            ],
            "type": "gaussian",
            "metadata": {                                       
                "method": "skimage.transform.pyramid_gaussian", 
                "version": "0.16.1",
                "args": "[true]",
                "kwargs": {"multichannel": true}
            }
        }
    ]
}
```

</details>


<details>

<summary>relative to the world coordinates</summary>

```json
{
    "multiscales": [
        {
            "version": "0.4",
            "spaces" : [
                {
                    "name" : "world",
                    "axes": [
                        {"name": "z", "type": "space", "unit": "micrometer"},
                        {"name": "y", "type": "space", "unit": "micrometer"},
                        {"name": "x", "type": "space", "unit": "micrometer"}
                    ]
                }
            ],
            "datasets": [
                {
                    "path": "0",
                    "coordinateTransformations": [{"type": "scale", "scale": [5.0, 0.15, 0.15], "inputSpace" : "0", "outputSpace" : "world"}]
                },
                {
                    "path": "1",
                    "coordinateTransformations": [{"type": "scale", "scale": [10.0, 0.3, 0.3], "inputSpace" : "1", "outputSpace" : "world"}]
                },
                {
                    "path": "2",
                    "coordinateTransformations": [{"type": "scale", "scale": [20.0, 0.6, 0.6], "inputSpace" : "2", "outputSpace" : "world"}]
                }
            ],
            "type": "gaussian",
            "metadata": {                                       
                "method": "skimage.transform.pyramid_gaussian", 
                "version": "0.16.1",
                "args": "[true]",
                "kwargs": {"multichannel": true}
            }
        }
    ]
}
```

</details>

