# fiji-hdf5-drag-and-drop

Drag and drop HDF5 files (`.h5`, `.hdf5`, `.hdf`) onto Fiji to open them as virtual images through the bundled
**HDF5/N5/Zarr/OME-NGFF** reader.

## Install

1. Download `hdf5-drag-and-drop-v1.0.0.zip` from the [Releases](../../releases) page.
2. Extract the archive into your Fiji installation folder - the folder that contains `scripts`, `jars` and `plugins`
   (for example `C:\PortableApps\Fiji`). Merge/overwrite when prompted.
3. If you already have `scripts/Plugins/AutoRun/AutoRun.ijm`, delete it. This package starts the handler with
   `HDF5_Drag_and_Drop_AutoRun.ijm`, and both files would launch it.
4. Restart Fiji.

The archive installs two files:

```
scripts/Plugins/Drag_and_Drop_HDF5.groovy
scripts/Plugins/AutoRun/HDF5_Drag_and_Drop_AutoRun.ijm
```

## Usage

Drop one or more HDF5 files anywhere onto the Fiji main window. A dialog asks for the dataset path (default `images`);
the same path is used for every file in the drop.

- **Set default** (next to OK) stores the current name as the default for future drops. It is persisted in
  `IJ_Prefs.txt` as `.hdf5dropwindow.dataset`.
- Everything else is untouched: dropping TIFFs, folders, images, etc. is handled exactly as stock Fiji does.

## Requirements

- A recent Fiji. The `HDF5/N5/Zarr/OME-NGFF ...` command comes from the `n5-ij` library bundled with Fiji.
- Turn off **Edit > Options > ImageJ2 > "Use SCIFIO when opening files"** if it is enabled. With SCIFIO on, dropping
  ordinary folders can log `No appropriate format found` from stock Fiji's legacy file opener. That is Fiji
  behaviour, not caused by this package.

## Uninstall

Delete the two files listed under Install and restart Fiji.

## How it works

- `scripts/Plugins/AutoRun/HDF5_Drag_and_Drop_AutoRun.ijm` runs at startup and launches the Groovy script. Fiji
  executes every file in `scripts/Plugins/AutoRun/` when it launches.
- The script wraps the drag-and-drop targets of the Fiji main window, handles drops that contain HDF5 files, and
  forwards everything else to the original handler.
- The `.groovy` filename produces the menu command `Drag and Drop HDF5`, which the AutoRun script calls - don't rename
  the two files.

## License

BSD-3-Clause. See [LICENSE](LICENSE).
