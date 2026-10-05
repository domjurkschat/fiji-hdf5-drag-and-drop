# fiji-hdf5-drag-and-drop

This package enables the automatic opening of HDF5 files (`.h5`, `.hdf5`, `.hdf`) as virtual stacks via drag and drop onto the main Fiji window. 
The heavy lifting is handled by the `HDF5/N5/Zarr/OME-NGFF` reader that comes bundled with modern Fiji versions.

## Installation

1. Download `hdf5-drag-and-drop-v1.1.0.zip` from the [Releases](../../releases) page.
2. Extract the archive into your Fiji installation folder. Merge when prompted.
3. Restart Fiji.

The archive installs two files:

```
scripts/Plugins/Drag_and_Drop_HDF5.groovy
scripts/Plugins/AutoRun/HDF5_Drag_and_Drop_AutoRun.ijm
```

## Usage

Drop one or more HDF5 files anywhere onto the Fiji main window. A dialog will appear asking for the dataset path (the same path is used for every file in the drop).
- The default dataset name can be set via the `Set default` button. Everything else is untouched: dropping TIFFs, folders, images, etc., is handled exactly as stock Fiji does.
- The native `HDF5/N5/Zarr/OME-NGFF` dataset browser can be opened with the `Browse...` button. In the case of multiple files, usage of the browser is tied to the first file (all other files won't be opened).

Note that a harmless `No appropriate format found` warning may be observed if SCIFIO is enabled (via `Edit > Options > ImageJ2 > "Use SCIFIO when opening files"`). 
This is Fiji behaviour and not caused by this package.

## Requirements

A modern version of Fiji bundled with the `HDF5/N5/Zarr/OME-NGFF ...` reader.
